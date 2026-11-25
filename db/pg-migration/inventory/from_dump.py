"""Инвентаризация по mysqldump-файлу, без MySQL-сервера.

Читает CREATE TABLE и INSERT из дампа и пишет в каталог те же TSV, что Dump.java
снимает с живой базы (meta + проверки), чтобы compare.py собрал из них report.md.

Использование:
  python -I from_dump.py <дамп.sql> <expected-mysql.json> <expected-pg.json> <каталог вывода>

Дамп читается потоково: в памяти держатся только ключи, нужные для проверки FK.
"""
import collections

import json
import os
import re
import sys

dump_path, exp_path, exp_pg_path, out_dir = sys.argv[1:5]
os.makedirs(out_dir, exist_ok=True)
exp = json.load(open(exp_path, encoding='utf-8'))
exp_pg = json.load(open(exp_pg_path, encoding='utf-8'))

DEFAULT_COLL = {'utf8mb3': 'utf8mb3_general_ci', 'utf8': 'utf8mb3_general_ci',
                'utf8mb4': 'utf8mb4_0900_ai_ci', 'latin1': 'latin1_swedish_ci'}
TEXT_TYPES = ('varchar', 'char', 'text', 'tinytext', 'mediumtext', 'longtext')
KEY_COLS = ('english_name', 'url', 'url_name', 'email', 'username', 'name')
NULL = None


def write(name, header, rows):
    with open(os.path.join(out_dir, name + '.tsv'), 'w', encoding='utf-8', newline='') as f:
        f.write('\t'.join(header) + '\n')
        for r in rows:
            f.write('\t'.join('<NULL>' if v is None else str(v).replace('\t', ' ').replace('\n', ' ')
                              .replace('\r', ' ') for v in r) + '\n')


# ---------- токенизатор значений INSERT ----------
TOKEN = re.compile(r"""
    (?P<null>NULL)
  | (?P<str>(?:_binary\s+|_utf8mb4\s*|_utf8mb3\s*)?'(?:[^'\\]|\\.|'')*')
  | (?P<hex>0x[0-9A-Fa-f]*)
  | (?P<bits>b'[01]*')
  | (?P<num>[-+]?[0-9][0-9.eE+-]*)
  | (?P<open>\()
  | (?P<close>\))
""", re.X | re.S)
UNESC = {'0': '\0', "'": "'", '"': '"', 'b': '\b', 'n': '\n', 'r': '\r', 't': '\t', 'Z': '\x1a', '\\': '\\',
         '%': '\\%', '_': '\\_'}
ESC_RE = re.compile(r"\\(.)|''", re.S)


def unescape(s):
    body = s[s.index("'") + 1:-1]
    if '\\' not in body and "''" not in body:
        return body
    return ESC_RE.sub(lambda m: "'" if m.group(0) == "''" else UNESC.get(m.group(1), m.group(1)), body)


def parse_rows(values_sql):
    row = None
    for m in TOKEN.finditer(values_sql):
        k = m.lastgroup
        if k == 'open':
            row = []
        elif k == 'close':
            yield row
            row = None
        elif row is None:
            continue
        elif k == 'null':
            row.append(NULL)
        elif k == 'str':
            row.append(unescape(m.group()))
        elif k == 'bits':
            row.append(str(int(m.group()[2:-1] or '0', 2)))
        else:
            row.append(m.group())


# ---------- разбор CREATE TABLE ----------
COL_RE = re.compile(r"^\s+`([^`]+)`\s+(.*?)(,)?$")
CREATE_RE = re.compile(r"^CREATE TABLE `([^`]+)`")


def parse_create(lines):
    name = CREATE_RE.match(lines[0]).group(1)
    cols, keys, fks = [], [], []
    tail = lines[-1]
    for ln in lines[1:-1]:
        m = COL_RE.match(ln)
        if m:
            defn = m.group(2)
            ctype = re.match(r"(\w+(?:\((?:[^()']|'[^']*')*\))?(?:\s+unsigned)?)", defn).group(1)
            cs = re.search(r"CHARACTER SET (\w+)", defn)
            co = re.search(r"COLLATE (\w+)", defn)
            dflt = re.search(r"DEFAULT ('(?:[^']|'')*'|\S+)", defn)
            cols.append({'name': m.group(1), 'type': ctype, 'nullable': 'NO' if 'NOT NULL' in defn else 'YES',
                         'cs': cs.group(1) if cs else None, 'coll': co.group(1) if co else None,
                         'default': dflt.group(1).rstrip(',') if dflt else None,
                         'extra': 'auto_increment' if 'AUTO_INCREMENT' in defn else ''})
            continue
        s = ln.strip().rstrip(',')
        m = re.match(r"PRIMARY KEY \((.*)\)", s)
        if m:
            keys.append(('PRIMARY', 0, [c.strip('` ') for c in re.findall(r"`[^`]+`", m.group(1))]))
            continue
        m = re.match(r"(UNIQUE KEY|KEY|FULLTEXT KEY) `([^`]+)` \((.*)\)", s)
        if m:
            keys.append((m.group(2), 0 if m.group(1) == 'UNIQUE KEY' else 1,
                         [c.strip('` ') for c in re.findall(r"`[^`]+`", m.group(3))]))
            continue
        m = re.match(r"CONSTRAINT `([^`]+)` FOREIGN KEY \((.*?)\) REFERENCES `([^`]+)` \((.*?)\)(.*)", s)
        if m:
            ondel = re.search(r"ON DELETE (CASCADE|SET NULL|NO ACTION|RESTRICT)", m.group(5))
            fks.append({'name': m.group(1), 'cols': [c.strip('` ') for c in m.group(2).split(',')],
                        'ref': m.group(3), 'refcols': [c.strip('` ') for c in m.group(4).split(',')],
                        'delete': ondel.group(1) if ondel else 'RESTRICT'})
    engine = re.search(r"ENGINE=(\w+)", tail)
    charset = re.search(r"DEFAULT CHARSET=(\w+)", tail)
    coll = re.search(r"COLLATE=(\w+)", tail)
    ai = re.search(r"AUTO_INCREMENT=(\d+)", tail)
    charset = charset.group(1) if charset else None
    tcoll = coll.group(1) if coll else DEFAULT_COLL.get(charset, charset)
    for c in cols:
        if c['cs'] or c['coll'] or c['type'].lower().startswith(TEXT_TYPES) or c['type'].lower().startswith('enum'):
            c['coll'] = c['coll'] or (DEFAULT_COLL.get(c['cs']) if c['cs'] else tcoll)
    return {'name': name, 'cols': cols, 'keys': keys, 'fks': fks, 'engine': engine.group(1) if engine else None,
            'coll': tcoll, 'auto_increment': ai.group(1) if ai else None}


# ---------- что проверять ----------
fk_specs = {}  # (child, col, parent, refcol) -> источник
for f in exp['fks']:
    if ',' not in f['cols']:
        fk_specs[(f['table'], f['cols'], f['ref'], f['refcols'])] = 'mapping'
enum_ranges = {}
for t, v in exp_pg['tables'].items():
    for c, d in v['columns'].items():
        m = re.search(r'smallint check \(\(\w+ between (\d+) and (\d+)\)\)', d)
        if m:
            enum_ranges[(t, c)] = (int(m.group(1)), int(m.group(2)))

tables = {}
counts = {}
parent_keys = collections.defaultdict(set)      # (table, col) -> значения
child_vals = collections.defaultdict(collections.Counter)  # (table, col) -> значения
res = collections.defaultdict(list)
pairs = {'classes_spells': set(), 'spells_hero_class': set()}
views, triggers, routines = [], [], []

# Колонки, по которым нужны ключи родителей/детей, станут известны после разбора всех CREATE.
# Поэтому сначала быстрый проход только по схеме.
with open(dump_path, encoding='utf-8', errors='replace') as fh:
    buf = None
    for line in fh:
        if line.startswith('CREATE TABLE'):
            buf = [line.rstrip('\n')]
        elif buf is not None:
            buf.append(line.rstrip('\n'))
            if line.startswith(')'):
                t = parse_create(buf)
                tables[t['name']] = t
                buf = None
        elif 'VIEW `' in line and 'CREATE' in line:
            m = re.search(r"VIEW `([^`]+)`", line)
            if m and m.group(1) not in [v[0] for v in views]:
                views.append((m.group(1), line.strip()[:200]))
        elif re.search(r"\bTRIGGER `", line):
            m = re.search(r"TRIGGER `([^`]+)` (BEFORE|AFTER) (INSERT|UPDATE|DELETE) ON `([^`]+)`", line)
            if m:
                triggers.append((m.group(1), m.group(4), m.group(2), m.group(3)))
        elif re.match(r"(/\*!50003 )?CREATE.*\b(PROCEDURE|FUNCTION)\b `", line):
            m = re.search(r"(PROCEDURE|FUNCTION) `([^`]+)`", line)
            routines.append((m.group(2), m.group(1)))

for t in tables.values():
    for f in t['fks']:
        if len(f['cols']) == 1:
            fk_specs.setdefault((t['name'], f['cols'][0], f['ref'], f['refcols'][0]), 'db')
need_parent = collections.defaultdict(set)
need_child = collections.defaultdict(set)
for (ct, cc, pt, pc) in fk_specs:
    need_child[ct].add(cc)
    need_parent[pt].add(pc)

# ---------- проход по данным ----------
def check_table(t, rows):
    cols = [c['name'] for c in t['cols']]
    idx = {c: i for i, c in enumerate(cols)}
    name = t['name']
    counts[name] = len(rows)
    for c in need_parent.get(name, ()):
        if c in idx:
            parent_keys[(name, c)].update(r[idx[c]] for r in rows if r[idx[c]] is not None)
    for c in need_child.get(name, ()):
        if c in idx:
            child_vals[(name, c)].update(r[idx[c]] for r in rows if r[idx[c]] is not None)
    if name in pairs:
        a, b = ('spells_id', 'hero_class_id') if name == 'classes_spells' else ('spell_id', 'hero_class_id')
        if a in idx and b in idx:
            pairs[name] = {(r[idx[a]], r[idx[b]]) for r in rows}
    has_pk = any(k[0] == 'PRIMARY' for k in t['keys'])
    if not has_pk and len(cols) <= 4:
        cnt = collections.Counter(tuple(r) for r in rows)
        d = [n for n in cnt.values() if n > 1]
        res['dups'].append((name, len(d), sum(n - 1 for n in d)))
    for c in t['cols']:
        cn, ct = c['name'], c['type'].lower()
        i = idx[cn]
        if cn in KEY_COLS:
            vals = [r[i] for r in rows if r[i] is not None]
            cnt = collections.Counter(v.strip().lower() for v in vals)
            d = [n for n in cnt.values() if n > 1]
            res['cidup'].append((f'{name}.{cn}', len(d), sum(n - 1 for n in d)))
            res['ws'].append((f'{name}.{cn}', sum(v != v.strip() for v in vals), sum(v == '' for v in vals)))
        if ct.startswith(('datetime', 'date', 'timestamp')):
            res['zerodate'].append((f'{name}.{cn}', sum(1 for r in rows if r[i] and r[i].startswith('0000'))))
        if (name, cn) in enum_ranges:
            lo, hi = enum_ranges[(name, cn)]
            bad = sum(1 for r in rows if r[i] is not None and not (lo <= int(float(r[i])) <= hi))
            res['enumrange'].append((f'{name}.{cn}', bad))
        if ct.startswith(TEXT_TYPES):
            res['nul'].append((f'{name}.{cn}', sum(1 for r in rows if r[i] and '\0' in r[i])))
        if (name, cn) in (('json_storage', 'json_data'), ('revision_history', 'snapshot')):
            vals = [r[i] for r in rows if r[i] is not None]
            invalid = 0
            for v in vals:
                try:
                    json.loads(v)
                except ValueError:
                    invalid += 1
            lens = [len(v.encode('utf-8')) for v in vals]
            res['json'].append((f'{name}.{cn}', len(vals), invalid, max(lens, default=0),
                                round(sum(lens) / len(lens)) if lens else 0))


INSERT_RE = re.compile(r"^INSERT INTO `([^`]+)` (?:\([^)]*\) )?VALUES ")
current, rows = None, []
with open(dump_path, encoding='utf-8', errors='replace') as fh:
    for line in fh:
        m = INSERT_RE.match(line)
        if not m:
            continue
        name = m.group(1)
        if name != current:
            if current is not None:
                check_table(tables[current], rows)
            current, rows = name, []
        rows.extend(parse_rows(line[m.end():]))
    if current is not None:
        check_table(tables[current], rows)
for name, t in tables.items():
    if name not in counts:
        check_table(t, [])

# ---------- висячие ссылки ----------
for (ct, cc, pt, pc), src in sorted(fk_specs.items()):
    if ct not in tables:
        continue
    if pt not in tables:
        res['orphan'].append((f'{ct}.{cc} -> {pt} (таблицы нет)', sum(child_vals[(ct, cc)].values())))
        continue
    keys = parent_keys[(pt, pc)]
    res['orphan'].append((f'{ct}.{cc} -> {pt}', sum(n for v, n in child_vals[(ct, cc)].items() if v not in keys)))

# ---------- запись TSV в формате Dump.java ----------
head = open(dump_path, encoding='utf-8', errors='replace').read(2000)
ver = re.search(r"Server version\s+(\S+)", head)
db = re.search(r"Database: (\S+)", head)
write('server', ['v', 'sql_mode', 'cs', 'coll', 'lctn', 'tz', 'db'],
      [(ver.group(1) if ver else '?', '(нет в дампе)', '?', '?', '?', '?', db.group(1) if db else '?')])
schema_cs = collections.Counter(t['coll'] for t in tables.values()).most_common(1)
write('schema', ['SCHEMA_NAME', 'DEFAULT_CHARACTER_SET_NAME', 'DEFAULT_COLLATION_NAME'],
      [(db.group(1) if db else '?', '(по таблицам)', schema_cs[0][0] if schema_cs else '?')])
write('tables', ['TABLE_NAME', 'TABLE_TYPE', 'ENGINE', 'TABLE_COLLATION', 'TABLE_ROWS', 'DATA_LENGTH',
                 'INDEX_LENGTH', 'AUTO_INCREMENT', 'CREATE_TIME', 'UPDATE_TIME'],
      [(t['name'], 'BASE TABLE', t['engine'], t['coll'], counts.get(t['name'], 0), '', '', t['auto_increment'],
        '', '') for t in tables.values()] + [(v[0], 'VIEW', None, None, '', '', '', None, '', '') for v in views])
write('columns', ['TABLE_NAME', 'COLUMN_NAME', 'ORDINAL_POSITION', 'COLUMN_TYPE', 'IS_NULLABLE', 'COLUMN_DEFAULT',
                  'CHARACTER_SET_NAME', 'COLLATION_NAME', 'COLUMN_KEY', 'EXTRA'],
      [(t['name'], c['name'], i + 1, c['type'], c['nullable'], c['default'], c['cs'], c['coll'], '', c['extra'])
       for t in tables.values() for i, c in enumerate(t['cols'])])
write('fks', ['TABLE_NAME', 'CONSTRAINT_NAME', 'COLUMN_NAME', 'REFERENCED_TABLE_NAME', 'REFERENCED_COLUMN_NAME',
              'UPDATE_RULE', 'DELETE_RULE'],
      [(t['name'], f['name'], c, f['ref'], rc, '', f['delete'])
       for t in tables.values() for f in t['fks'] for c, rc in zip(f['cols'], f['refcols'])])
write('indexes', ['TABLE_NAME', 'INDEX_NAME', 'NON_UNIQUE', 'SEQ_IN_INDEX', 'COLUMN_NAME', 'INDEX_TYPE'],
      [(t['name'], k[0], k[1], i + 1, c, 'BTREE') for t in tables.values() for k in t['keys'] for i, c in enumerate(k[2])])
write('views', ['TABLE_NAME', 'VIEW_DEFINITION'], views)
write('triggers', ['TRIGGER_NAME', 'EVENT_OBJECT_TABLE', 'ACTION_TIMING', 'EVENT_MANIPULATION'], triggers)
write('routines', ['ROUTINE_NAME', 'ROUTINE_TYPE'], routines)
write('events', ['EVENT_NAME', 'STATUS'], [])
write('checks', ['CONSTRAINT_NAME', 'CHECK_CLAUSE'], [])
write('rowcounts', ['tbl', 'cnt'], sorted(counts.items()))
hdr = {'orphan': ['fk', 'orphans'], 'dups': ['tbl', 'dup_groups', 'extra_rows'],
       'cidup': ['col', 'groups_ci', 'extra_rows'], 'ws': ['col', 'edge_spaces', 'empty_str'],
       'zerodate': ['col', 'zero_dates'], 'enumrange': ['col', 'out_of_range'], 'nul': ['col', 'with_nul'],
       'json': ['col', 'total', 'invalid_json', 'max_bytes', 'avg_bytes']}
for k, h in hdr.items():
    write(f'{k}__all', h, res[k])
a, b = pairs['classes_spells'], pairs['spells_hero_class']
write('spells_classes_divergence',
      ['classes_spells_rows', 'spells_hero_class_rows', 'only_in_classes_spells', 'only_in_spells_hero_class'],
      [(counts.get('classes_spells', 0), counts.get('spells_hero_class', 0), len(a - b), len(b - a))])
print(f'таблиц: {len(tables)}, строк: {sum(counts.values())}, представлений: {len(views)}, '
      f'триггеров: {len(triggers)}, процедур: {len(routines)}')
