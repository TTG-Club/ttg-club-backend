"""Генерирует checks.sql (только SELECT) для проверки качества данных перед переездом.

Каждый блок `-- @имя` -> отдельный TSV в out/ после прогона Dump.java.
Блок, упавший с ошибкой (например, колонки нет в реальной БД), не мешает остальным.
"""
import json
import sys

exp = json.load(open(sys.argv[1], encoding='utf-8'))
out = []


def block(name, sql):
    out.append(f'-- @{name}\n{sql}\n')


tables = exp['tables']

# 1. Точное число строк во всех ожидаемых таблицах.
block('rowcounts', '\nUNION ALL\n'.join(
    f"SELECT '{t}' AS tbl, COUNT(*) AS cnt FROM `{t}`" for t in sorted(tables)))

# 2. Висячие ссылки: значение FK есть, а строки, на которую оно указывает, нет.
for i, f in enumerate(exp['fks']):
    if ',' in f['cols']:
        continue
    block(f"orphan__{f['table']}__{f['cols']}",
          f"SELECT '{f['table']}.{f['cols']} -> {f['ref']}' AS fk, COUNT(*) AS orphans "
          f"FROM `{f['table']}` c LEFT JOIN `{f['ref']}` p ON p.`{f['refcols']}` = c.`{f['cols']}` "
          f"WHERE c.`{f['cols']}` IS NOT NULL AND p.`{f['refcols']}` IS NULL")

# 3. Полные дубли строк в join- и collection-таблицах (у большинства нет PK).
for t, v in sorted(tables.items()):
    if v['pk'] == ['id'] or (len(v['pk']) == 1 and 'id' in v['pk'][0]):
        continue
    cols = ', '.join(f'`{c}`' for c in v['columns'])
    if len(v['columns']) > 3:
        continue
    block(f'dups__{t}',
          f"SELECT '{t}' AS tbl, COUNT(*) AS dup_groups, COALESCE(SUM(n - 1), 0) AS extra_rows "
          f"FROM (SELECT COUNT(*) n FROM `{t}` GROUP BY {cols} HAVING COUNT(*) > 1) x")

# 4. Дубли, видимые только с учётом регистра/пробелов: в MySQL (_ci) это «одно значение»,
#    в PostgreSQL станут разными, а уникальный индекс по lower(...) не создастся.
key_cols = ('english_name', 'url', 'url_name', 'email', 'username', 'name')
for t, v in sorted(tables.items()):
    for c in v['columns']:
        if c in key_cols:
            block(f'cidup__{t}__{c}',
                  f"SELECT '{t}.{c}' AS col, COUNT(*) AS groups_ci, COALESCE(SUM(n - 1), 0) AS extra_rows "
                  f"FROM (SELECT COUNT(*) n FROM `{t}` WHERE `{c}` IS NOT NULL "
                  f"GROUP BY LOWER(TRIM(`{c}`)) HAVING COUNT(*) > 1) x")
            block(f'ws__{t}__{c}',
                  f"SELECT '{t}.{c}' AS col, "
                  f"SUM(`{c}` <> TRIM(`{c}`)) AS edge_spaces, SUM(`{c}` = '') AS empty_str "
                  f"FROM `{t}`")

# 5. «Нулевые» даты — pgloader и PG их не примут.
for t, v in sorted(tables.items()):
    for c, d in v['columns'].items():
        if d.lower().startswith(('datetime', 'date', 'timestamp')):
            block(f'zerodate__{t}__{c}',
                  f"SELECT '{t}.{c}' AS col, COUNT(*) AS zero_dates FROM `{t}` "
                  f"WHERE CAST(`{c}` AS CHAR) LIKE '0000%'")

# 6. Проверка ORDINAL-enum'ов: значения за пределами диапазона из маппинга.
pg = json.load(open(sys.argv[2], encoding='utf-8'))
import re
for t, v in sorted(pg['tables'].items()):
    for c, d in v['columns'].items():
        m = re.search(r'smallint check \(\(\w+ between (\d+) and (\d+)\)\)', d)
        if m:
            block(f'enumrange__{t}__{c}',
                  f"SELECT '{t}.{c}' AS col, COUNT(*) AS out_of_range FROM `{t}` "
                  f"WHERE `{c}` IS NOT NULL AND (`{c}` < {m.group(1)} OR `{c}` > {m.group(2)})")

# 7. Две независимые join-таблицы одной связи «класс <-> заклинание».
block('spells_classes_divergence', """
SELECT
  (SELECT COUNT(*) FROM classes_spells) AS classes_spells_rows,
  (SELECT COUNT(*) FROM spells_hero_class) AS spells_hero_class_rows,
  (SELECT COUNT(*) FROM classes_spells a WHERE NOT EXISTS
     (SELECT 1 FROM spells_hero_class b WHERE b.spell_id = a.spells_id AND b.hero_class_id = a.hero_class_id)) AS only_in_classes_spells,
  (SELECT COUNT(*) FROM spells_hero_class b WHERE NOT EXISTS
     (SELECT 1 FROM classes_spells a WHERE b.spell_id = a.spells_id AND b.hero_class_id = a.hero_class_id)) AS only_in_spells_hero_class""")

# 8. Размеры JSON-подобных текстов (кандидаты в jsonb) и валидность JSON.
for t, c in (('json_storage', 'json_data'), ('revision_history', 'snapshot')):
    block(f'json__{t}__{c}',
          f"SELECT '{t}.{c}' AS col, COUNT(*) AS total, SUM(JSON_VALID(`{c}`) = 0) AS invalid_json, "
          f"MAX(LENGTH(`{c}`)) AS max_bytes, ROUND(AVG(LENGTH(`{c}`))) AS avg_bytes FROM `{t}`")

# 9. Символы, которые PostgreSQL не примет в text (NUL).
for t, v in sorted(tables.items()):
    for c, d in v['columns'].items():
        if d.lower().startswith(('varchar', 'text', 'mediumtext', 'longtext', 'tinytext')):
            block(f'nul__{t}__{c}',
                  f"SELECT '{t}.{c}' AS col, COUNT(*) AS with_nul FROM `{t}` WHERE INSTR(`{c}`, CHAR(0)) > 0")

open(sys.argv[3], 'w', encoding='utf-8').write('\n'.join(out))
print(len(out), 'check blocks')
