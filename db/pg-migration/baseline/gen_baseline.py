"""Генерирует начальный changelog Liquibase для PostgreSQL (этап 1, перенос 1:1).

Основа — DDL, который Hibernate строит по сущностям (inventory/expected/expected-pg.sql).
Поверх него переносится то, что есть в MySQL и от чего зависит поведение приложения:

- регистронезависимое сравнение строк (_ci в MySQL) -> недетерминированная ICU-collation
  `ci` (PostgreSQL 18+: с ней работают =, LIKE, ORDER BY и уникальные индексы);
- ширина колонок, если в MySQL она больше, чем в маппинге (text вместо varchar(255));
- NULL, где MySQL его допускает (маппинг местами строже базы);
- уникальность — только та, что есть в MySQL (Hibernate ставит unique на целевую
  колонку @OneToMany с join-таблицей, а в данных там повторы);
- индексы MySQL на колонках маппинга и индексы на всех FK (PostgreSQL их сам не создаёт);
- колонки и представление для глобального поиска (full_text_search).

Использование:
  python -I gen_baseline.py <expected-pg.sql> <inventory out/> <дамп MySQL> <каталог changelog>
"""
import csv
import json
import os
import re
import sys

sys.stdout.reconfigure(encoding='utf-8')
exp_sql_path, inv_dir, dump_path, out_dir = sys.argv[1:5]
os.makedirs(os.path.join(out_dir, 'changes'), exist_ok=True)
AUTHOR = 'pg-migration'


def tsv(name):
    with open(os.path.join(inv_dir, name + '.tsv'), encoding='utf-8') as f:
        return list(csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE))


my_cols = {}
for r in tsv('columns'):
    my_cols.setdefault(r['TABLE_NAME'], {})[r['COLUMN_NAME']] = r
my_idx = {}
for r in tsv('indexes'):
    my_idx.setdefault(r['TABLE_NAME'], {}).setdefault(r['INDEX_NAME'], {'unique': r['NON_UNIQUE'] == '0', 'cols': []})
    my_idx[r['TABLE_NAME']][r['INDEX_NAME']]['cols'].append(r['COLUMN_NAME'])

# ---------- разбор DDL Hibernate ----------
text = open(exp_sql_path, encoding='utf-8').read()
quoted = set(re.findall(r'"(\w+)"', text))
stmts = [' '.join(s.split()) for s in text.split(';') if s.strip()]


def q(name):
    return f'"{name}"' if name in quoted else name


def split_top(body):
    parts, depth, cur, in_str = [], 0, '', False
    for ch in body:
        if ch == "'":
            in_str = not in_str
        if not in_str:
            if ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
            elif ch == ',' and depth == 0:
                parts.append(cur.strip())
                cur = ''
                continue
        cur += ch
    parts.append(cur.strip())
    return parts


tables, fks = [], []
for s in stmts:
    m = re.match(r'create table (\S+) \((.*)\)$', s)
    if m:
        name = m.group(1).strip('"')
        cols, pk, extra = [], [], []
        for p in split_top(m.group(2)):
            pl = p.lower()
            if pl.startswith('primary key'):
                pk = [c.strip(' "') for c in re.search(r'\((.*)\)', p).group(1).split(',')]
            elif pl.startswith(('unique', 'constraint', 'check')):
                extra.append(p)
            else:
                cm = re.match(r'("?\w+"?)\s+(.*)', p)
                cols.append((cm.group(1).strip('"'), cm.group(2)))
        tables.append({'name': name, 'cols': cols, 'pk': pk, 'extra': extra})
        continue
    m = re.match(r'alter table if exists (\S+) add constraint (\S+) foreign key \((.*?)\) references (\S+)(?: \((.*?)\))?', s)
    if m:
        fks.append({'table': m.group(1).strip('"'), 'name': m.group(2), 'cols': [c.strip(' "') for c in m.group(3).split(',')],
                    'ref': m.group(4).strip('"'),
                    'refcols': [c.strip(' "') for c in m.group(5).split(',')] if m.group(5) else None})
# «references books» без списка колонок означает первичный ключ таблицы
pk_of = {t['name']: t['pk'] for t in tables}
for f in fks:
    if f['refcols'] is None:
        f['refcols'] = pk_of[f['ref']]

# ---------- корректировки по MySQL ----------
STRING_TYPES = ('varchar', 'char', 'text')
TEXTY_MYSQL = ('text', 'tinytext', 'mediumtext', 'longtext')
notes = {'collation': 0, 'widened': [], 'nullable': [], 'unique_dropped': [], 'extra_cols': []}


def is_ci(table, col):
    c = my_cols.get(table, {}).get(col)
    return bool(c and c['COLLATION_NAME'] not in ('<NULL>', '') and c['COLLATION_NAME'].endswith('_ci'))


def mysql_unique(table, cols):
    return any(i['unique'] and i['cols'] == cols for i in my_idx.get(table, {}).values())


# collation у FK-колонки должна совпадать с родителем
ci_cols = set()
for t in tables:
    for c, d in t['cols']:
        if d.split()[0].split('(')[0].lower() in STRING_TYPES and is_ci(t['name'], c):
            ci_cols.add((t['name'], c))
changed = True
while changed:
    changed = False
    for f in fks:
        for c, rc in zip(f['cols'], f['refcols']):
            a, b = (f['table'], c) in ci_cols, (f['ref'], rc) in ci_cols
            if a != b:
                ci_cols.add((f['table'], c))
                ci_cols.add((f['ref'], rc))
                changed = True

for t in tables:
    new_cols = []
    for c, d in t['cols']:
        m = re.match(r'(\w+(?:\(\d+(?:,\d+)?\))?(?: precision)?)(.*)$', d)
        typ, rest = m.group(1), m.group(2)
        if typ.lower() == 'text':
            typ = 'text'
        base = typ.split('(')[0].lower()
        mc = my_cols.get(t['name'], {}).get(c)
        if mc and base in STRING_TYPES:
            mt = mc['COLUMN_TYPE'].lower()
            if mt.startswith(TEXTY_MYSQL) and base != 'text':
                notes['widened'].append(f"{t['name']}.{c}: {typ} -> text (MySQL {mt})")
                typ = 'text'
            else:
                mm = re.match(r'(?:var)?char\((\d+)\)', mt)
                pm = re.match(r'\w+\((\d+)\)', typ)
                # char(n) не расширяем: это UUID с @JdbcTypeCode(CHAR), валидатор ждёт char
                if mm and pm and base != 'char' and int(mm.group(1)) > int(pm.group(1)):
                    notes['widened'].append(f"{t['name']}.{c}: {typ} -> varchar({mm.group(1)})")
                    typ = f'varchar({mm.group(1)})'
        if (t['name'], c) in ci_cols:
            typ += ' collate ci'
            notes['collation'] += 1
        if ' unique' in rest and not mysql_unique(t['name'], [c]):
            rest = rest.replace(' unique', '')
            notes['unique_dropped'].append(f"{t['name']}.{c}")
        if 'not null' in rest and mc and mc['IS_NULLABLE'] == 'YES' and c not in t['pk']:
            rest = rest.replace(' not null', '')
            notes['nullable'].append(f"{t['name']}.{c}")
        new_cols.append((c, typ + rest))
    t['cols'] = new_cols
    kept = []
    for e in t['extra']:
        m = re.match(r'unique \((.*)\)', e)
        if m and not mysql_unique(t['name'], [x.strip(' "') for x in m.group(1).split(',')]):
            notes['unique_dropped'].append(f"{t['name']}({m.group(1)})")
            continue
        kept.append(e)
    t['extra'] = kept

# колонки, которых нет в сущностях, но которые читает full_text_search
SEARCH_COLS = [('classes', 'alt_name'), ('archetypes', 'alt_name'), ('hero_class_traits', 'alt_name'),
               ('hero_class_traits', 'english_name'), ('hero_class_feats', 'alt_name'),
               ('hero_class_feats', 'english_name')]
by_name = {t['name']: t for t in tables}
for tn, c in SEARCH_COLS:
    if c not in dict(by_name[tn]['cols']):
        by_name[tn]['cols'].append((c, 'varchar(255) collate ci' if is_ci(tn, c) else 'varchar(255)'))
        notes['extra_cols'].append(f'{tn}.{c}')

# ---------- индексы ----------
indexes, seen = [], set()
for t in tables:
    colset = dict(t['cols'])
    covered = {tuple(t['pk'])} if t['pk'] else set()
    for e in t['extra']:
        m = re.match(r'unique \((.*)\)', e)
        if m:
            covered.add(tuple(x.strip(' "') for x in m.group(1).split(',')))
    for c, d in t['cols']:
        if ' unique' in d:
            covered.add((c,))
    for iname, i in sorted(my_idx.get(t['name'], {}).items()):
        cols = tuple(i['cols'])
        if iname == 'PRIMARY' or not all(c in colset for c in cols):
            continue
        if any(colset[c].split()[0] == 'text' for c in cols):
            continue  # btree по длинному тексту не нужен (в MySQL это были FULLTEXT/префиксные индексы)
        if cols in covered or (t['name'], cols) in seen:
            continue
        seen.add((t['name'], cols))
        indexes.append((t['name'], cols, i['unique']))
for f in fks:
    t = by_name[f['table']]
    cols = tuple(f['cols'])
    lead = {k[:len(cols)] for _, k, _ in [(x[0], x[1], x[2]) for x in indexes if x[0] == f['table']]}
    if (t['pk'] and tuple(t['pk'][:len(cols)]) == cols) or cols in lead or (f['table'], cols) in seen:
        continue
    seen.add((f['table'], cols))
    indexes.append((f['table'], cols, False))

# ---------- представление full_text_search ----------
view_line = None
with open(dump_path, encoding='utf-8') as fh:
    for line in fh:
        if line.startswith('/*!50001 VIEW `full_text_search`'):
            view_line = line
            break
body = view_line.split(' AS ', 1)[1].rsplit('*/', 1)[0]
body = body.replace('`', '')
body = body.replace('(hct.architype = 0)', '(hct.architype = false)')
branches = re.split(r'\bunion\b', body)
out_br = []
for br in branches:
    m = re.match(r'\s*select (.*?) from (.*)$', br.strip(), re.S)
    items = split_top(m.group(1))
    new_items = []
    for it in items:
        e, alias = re.match(r'(.*) AS (\w+)$', it.strip(), re.S).groups()
        new_items.append(f'({e}) collate ci AS {alias}')
    out_br.append('SELECT ' + ',\n       '.join(new_items) + '\nFROM ' + m.group(2).strip())
view_sql = 'CREATE OR REPLACE VIEW full_text_search AS\n' + '\nUNION\n'.join(out_br)

# ---------- запись changelog ----------
HDR = '--liquibase formatted sql\n\n'


def write(name, content):
    with open(os.path.join(out_dir, 'changes', name), 'w', encoding='utf-8', newline='\n') as f:
        f.write(HDR + content)


write('0001-collation.sql', f"""--changeset {AUTHOR}:0001-collation-ci
--comment: Регистронезависимое сравнение строк, как _ci в MySQL. Нужен PostgreSQL 18+ (LIKE с недетерминированной collation).
CREATE COLLATION IF NOT EXISTS ci (provider = icu, locale = 'und-u-ks-level2', deterministic = false);
--rollback DROP COLLATION IF EXISTS ci;
""")

parts = []
for t in tables:
    lines = [f'    {q(c)} {d}' for c, d in t['cols']]
    if t['pk']:
        lines.append(f"    primary key ({', '.join(q(c) for c in t['pk'])})")
    lines += [f'    {e}' for e in t['extra']]
    parts.append(f"--changeset {AUTHOR}:0002-table-{t['name']}\n"
                 f"CREATE TABLE {q(t['name'])} (\n" + ',\n'.join(lines) + '\n);\n'
                 f"--rollback DROP TABLE {q(t['name'])};\n")
write('0002-tables.sql', '\n'.join(parts))

parts = []
for f in fks:
    parts.append(f"--changeset {AUTHOR}:0003-fk-{f['name']}\n"
                 f"ALTER TABLE {q(f['table'])} ADD CONSTRAINT {f['name']} FOREIGN KEY ({', '.join(q(c) for c in f['cols'])}) "
                 f"REFERENCES {q(f['ref'])} ({', '.join(q(c) for c in f['refcols'])});\n"
                 f"--rollback ALTER TABLE {q(f['table'])} DROP CONSTRAINT {f['name']};\n")
write('0003-foreign-keys.sql', '\n'.join(parts))

parts = []
for tn, cols, uniq in indexes:
    iname = ('ux_' if uniq else 'ix_') + tn + '_' + '_'.join(cols)
    iname = iname[:63]
    parts.append(f"--changeset {AUTHOR}:0004-{iname}\n"
                 f"CREATE {'UNIQUE ' if uniq else ''}INDEX {iname} ON {q(tn)} ({', '.join(q(c) for c in cols)});\n"
                 f"--rollback DROP INDEX {iname};\n")
write('0004-indexes.sql', '\n'.join(parts))

write('0005-view-full-text-search.sql', f"""--changeset {AUTHOR}:0005-view-full-text-search runOnChange:true
--comment: Глобальный поиск (SearchRepository). Строковые колонки приведены к collation ci, иначе UNION не сравнит строки из колонок с разной collation.
{view_sql};
--rollback DROP VIEW IF EXISTS full_text_search;
""")

with open(os.path.join(out_dir, 'db.changelog-master.yaml'), 'w', encoding='utf-8', newline='\n') as f:
    f.write("""# Схема БД. Начальные файлы 0001-0005 сгенерированы db/pg-migration/baseline/gen_baseline.py
# (перенос 1:1 из MySQL). Новые изменения — отдельными файлами в changes/ с префиксом
# следующего номера; уже применённые changeset'ы не редактировать.
databaseChangeLog:
  - includeAll:
      path: changes/
      relativeToChangelogFile: true
""")

json.dump(notes, open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'baseline-notes.json'), 'w', encoding='utf-8'),
          ensure_ascii=False, indent=1)
print(f"таблиц {len(tables)}, FK {len(fks)}, индексов {len(indexes)}, ci-колонок {notes['collation']}, "
      f"расширено {len(notes['widened'])}, NOT NULL снято {len(notes['nullable'])}, "
      f"unique снято {len(notes['unique_dropped'])}, доп. колонок {len(notes['extra_cols'])}")
