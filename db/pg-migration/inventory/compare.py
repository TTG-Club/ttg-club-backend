"""Сравнивает фактическую схему MySQL (TSV из Dump.java) с ожидаемой схемой из сущностей
и сводит результаты проверок данных в Markdown-отчёт.

Использование: python -I compare.py <каталог с TSV> <expected-mysql.json> <отчёт.md>
"""
import csv
import glob
import json
import os
import re
import sys

out_dir, exp_path, report_path = sys.argv[1], sys.argv[2], sys.argv[3]
csv.field_size_limit(10 ** 9)


def tsv(name):
    path = os.path.join(out_dir, name + '.tsv')
    if not os.path.exists(path):
        return []
    with open(path, encoding='utf-8') as f:
        return list(csv.DictReader(f, delimiter='\t', quoting=csv.QUOTE_NONE))


exp = json.load(open(exp_path, encoding='utf-8'))
exp_tables = exp['tables']
lines = []
w = lines.append


def table(headers, rows):
    if not rows:
        w('_нет_\n')
        return
    w('| ' + ' | '.join(headers) + ' |')
    w('|' + '---|' * len(headers))
    for r in rows:
        w('| ' + ' | '.join(str(x).replace('|', '\\|') for x in r) + ' |')
    w('')


# --- Сервер ---
w('# Инвентаризация схемы MySQL перед переездом на PostgreSQL\n')
srv = tsv('server')
sch = tsv('schema')
if srv:
    s = srv[0]
    w('## Сервер\n')
    table(['параметр', 'значение'], [
        ['версия', s['v']], ['база', s['db']], ['sql_mode', s['sql_mode']],
        ['charset/collation сервера', f"{s['cs']} / {s['coll']}"],
        ['charset/collation схемы', f"{sch[0]['DEFAULT_CHARACTER_SET_NAME']} / {sch[0]['DEFAULT_COLLATION_NAME']}" if sch else '?'],
        ['lower_case_table_names', s['lctn']], ['time_zone', s['tz']]])

# --- Таблицы ---
db_tables = {r['TABLE_NAME']: r for r in tsv('tables')}
counts = {r['tbl']: int(r['cnt']) for r in tsv('rowcounts')}
db_cols = {}
for r in tsv('columns'):
    db_cols.setdefault(r['TABLE_NAME'], {})[r['COLUMN_NAME']] = r

base = {t for t, r in db_tables.items() if r['TABLE_TYPE'] == 'BASE TABLE'}
views = {t for t, r in db_tables.items() if r['TABLE_TYPE'] != 'BASE TABLE'}
extra_tables = sorted(base - set(exp_tables))
missing_tables = sorted(set(exp_tables) - base)

w('## Сводка\n')
total_rows = sum(int(r['TABLE_ROWS'] or 0) for r in db_tables.values() if r['TABLE_ROWS'] not in ('<NULL>', ''))
size = sum(int(r['DATA_LENGTH'] or 0) + int(r['INDEX_LENGTH'] or 0) for r in db_tables.values()
           if r['DATA_LENGTH'] not in ('<NULL>', ''))
table(['метрика', 'значение'], [
    ['таблиц в БД', len(base)], ['представлений', len(views)],
    ['таблиц в маппинге сущностей', len(exp_tables)],
    ['лишних таблиц (нет в сущностях)', len(extra_tables)],
    ['отсутствующих таблиц (есть в сущностях)', len(missing_tables)],
    ['строк (оценка InnoDB)', f'{total_rows:,}'.replace(',', ' ')],
    ['объём данных+индексов', f'{size / 1024 / 1024:.1f} МБ']])

engines = {}
for t in base:
    engines.setdefault(db_tables[t]['ENGINE'], []).append(t)
w('### Движки таблиц\n')
table(['engine', 'таблиц', 'примеры'], [[e, len(ts), ', '.join(sorted(ts)[:8])] for e, ts in sorted(engines.items())])

colls = {}
for t in base:
    colls.setdefault(db_tables[t]['TABLE_COLLATION'], []).append(t)
w('### Collation таблиц\n')
table(['collation', 'таблиц', 'примеры'], [[c, len(ts), ', '.join(sorted(ts)[:8])] for c, ts in sorted(colls.items())])

odd_col_coll = [(r['TABLE_NAME'], r['COLUMN_NAME'], r['COLLATION_NAME'])
                for t in base for r in db_cols.get(t, {}).values()
                if r['COLLATION_NAME'] not in ('<NULL>', db_tables[t]['TABLE_COLLATION'])]
w('### Колонки с collation, отличным от таблицы\n')
table(['таблица', 'колонка', 'collation'], odd_col_coll)

w('## Лишние таблицы (есть в БД, нет в сущностях)\n')
w('Кандидаты на удаление до переезда или на явный перенос, если их читает что-то кроме бэкенда.\n')
table(['таблица', 'строк', 'обновлялась', 'колонки'],
      [[t, counts.get(t, db_tables[t]['TABLE_ROWS']), db_tables[t]['UPDATE_TIME'],
        ', '.join(list(db_cols.get(t, {}))[:10])] for t in extra_tables])

w('## Отсутствующие таблицы (есть в сущностях, нет в БД)\n')
table(['таблица'], [[t] for t in missing_tables])

# --- Колонки ---
def norm(t):
    t = t.lower()
    t = re.sub(r'\s+(not null|null|default .*|auto_increment|check .*|primary key|unique).*$', '', t)
    t = t.replace('integer', 'int').replace('boolean', 'bit').replace('tinyint(1)', 'bit')
    t = re.sub(r'\b(int|bigint|smallint|tinyint)\(\d+\)', r'\1', t)
    t = re.sub(r'datetime\(6\)', 'datetime', t)
    t = t.replace('bit(1)', 'bit')
    return t.split()[0] if t else t


extra_cols, missing_cols, type_diff = [], [], []
for t in sorted(base & set(exp_tables)):
    e, d = exp_tables[t]['columns'], db_cols.get(t, {})
    for c in d:
        if c not in e:
            r = d[c]
            extra_cols.append([t, c, r['COLUMN_TYPE'], r['IS_NULLABLE']])
    for c in e:
        if c not in d:
            missing_cols.append([t, c, e[c]])
        else:
            et, dt = norm(e[c]), norm(d[c]['COLUMN_TYPE'])
            if et != dt and not (et.startswith('enum') and dt.startswith('enum')):
                type_diff.append([t, c, d[c]['COLUMN_TYPE'], e[c].split(' not null')[0]])

w('## Лишние колонки (есть в БД, нет в сущностях)\n')
w('`ddl-auto=update` колонки никогда не удаляет — это хвосты переименований.\n')
table(['таблица', 'колонка', 'тип', 'nullable'], extra_cols)
w('## Отсутствующие колонки\n')
table(['таблица', 'колонка', 'ожидаемый тип'], missing_cols)
w('## Расхождения типов\n')
table(['таблица', 'колонка', 'в БД', 'по маппингу'], type_diff)

# --- PK ---
db_idx = {}
for r in tsv('indexes'):
    db_idx.setdefault(r['TABLE_NAME'], {}).setdefault(r['INDEX_NAME'], []).append(r)
no_pk = sorted(t for t in base if 'PRIMARY' not in db_idx.get(t, {}))
w('## Таблицы без первичного ключа\n')
w('PostgreSQL их примет, но pgloader/логическая репликация и дедупликация без PK неудобны; '
  'для join-таблиц нужен составной PK.\n')
table(['таблица', 'строк'], [[t, counts.get(t, '?')] for t in no_pk])

# --- FK ---
db_fk = {}
for r in tsv('fks'):
    db_fk.setdefault((r['TABLE_NAME'], r['CONSTRAINT_NAME']), []).append(r)
db_fk_sig = {(k[0], ','.join(x['COLUMN_NAME'] for x in v), v[0]['REFERENCED_TABLE_NAME']): k[1] for k, v in db_fk.items()}
exp_fk_sig = {(f['table'], f['cols'], f['ref']) for f in exp['fks']}
w('## Внешние ключи\n')
w(f'В БД: {len(db_fk_sig)}, по маппингу: {len(exp_fk_sig)}.\n')
w('### Ожидаются, но в БД нет\n')
table(['таблица', 'колонки', '→ таблица'], sorted(list(x) for x in exp_fk_sig - set(db_fk_sig)))
w('### Есть в БД, но не из маппинга\n')
table(['таблица', 'колонки', '→ таблица', 'constraint'],
      sorted([*k, v] for k, v in db_fk_sig.items() if k not in exp_fk_sig))

# --- Прочие объекты ---
for name, title in (('views', 'Представления'), ('triggers', 'Триггеры'), ('routines', 'Процедуры и функции'),
                    ('events', 'События'), ('checks', 'CHECK-ограничения')):
    rows = tsv(name)
    w(f'## {title}\n')
    if rows:
        hdr = list(rows[0].keys())
        table(hdr, [[(r[h][:120]) for h in hdr] for r in rows])
    else:
        w('_нет_\n')

# --- Проверки данных ---
def collect(prefix):
    res = []
    for p in sorted(glob.glob(os.path.join(out_dir, prefix + '__*.tsv'))):
        res.extend(tsv(os.path.basename(p)[:-4]))
    return res


def nonzero(rows, keys):
    return [r for r in rows if any(r.get(k) not in (None, '', '0', '<NULL>') for k in keys)]


w('# Проверки данных\n')
w('## Висячие ссылки\n')
table(['связь', 'строк без родителя'], [[r['fk'], r['orphans']] for r in nonzero(collect('orphan'), ['orphans'])])
w('## Дубли в join/collection-таблицах\n')
table(['таблица', 'групп', 'лишних строк'],
      [[r['tbl'], r['dup_groups'], r['extra_rows']] for r in nonzero(collect('dups'), ['extra_rows'])])
w('## Дубли без учёта регистра и пробелов\n')
w('В MySQL (`_ci`) это одно значение; в PostgreSQL станут разными, `findBy…` начнёт находить не то '
  'или ничего.\n')
table(['колонка', 'групп', 'лишних строк'],
      [[r['col'], r['groups_ci'], r['extra_rows']] for r in nonzero(collect('cidup'), ['extra_rows'])])
w('## Пробелы по краям и пустые строки в ключевых колонках\n')
table(['колонка', 'с пробелами по краям', 'пустых строк'],
      [[r['col'], r['edge_spaces'], r['empty_str']] for r in nonzero(collect('ws'), ['edge_spaces', 'empty_str'])])
w('## Нулевые даты\n')
table(['колонка', 'строк'], [[r['col'], r['zero_dates']] for r in nonzero(collect('zerodate'), ['zero_dates'])])
w('## ORDINAL-enum вне диапазона\n')
table(['колонка', 'строк'], [[r['col'], r['out_of_range']] for r in nonzero(collect('enumrange'), ['out_of_range'])])
w('## Символ NUL в тексте\n')
table(['колонка', 'строк'], [[r['col'], r['with_nul']] for r in nonzero(collect('nul'), ['with_nul'])])
w('## Связь «класс ↔ заклинание»: две независимые join-таблицы\n')
div = tsv('spells_classes_divergence')
table(list(div[0].keys()) if div else ['—'], [list(div[0].values())] if div else [])
w('## Кандидаты в jsonb уже сейчас (текст с JSON)\n')
js = collect('json')
table(list(js[0].keys()) if js else ['—'], [list(r.values()) for r in js])

w('## Крупнейшие таблицы\n')
top = sorted(counts.items(), key=lambda x: -x[1])[:20]
table(['таблица', 'строк'], top)

failed = []
w('')
open(report_path, 'w', encoding='utf-8').write('\n'.join(lines))
print('report:', report_path)
