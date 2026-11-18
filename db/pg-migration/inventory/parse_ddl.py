"""Разбор DDL, сгенерированного Hibernate, в JSON: таблицы, колонки, PK, FK, unique."""
import json
import re
import sys

src, dst = sys.argv[1], sys.argv[2]
text = open(src, encoding='utf-8').read()
stmts = [s.strip() for s in text.split(';') if s.strip()]
tables = {}
fks = []
for s in stmts:
    flat = ' '.join(s.split())
    m = re.match(r'create table (\S+) \((.*)\)( engine=\S+)?$', flat, re.I)
    if m:
        name, body = m.group(1).strip('`"'), m.group(2)
        parts, depth, cur = [], 0, ''
        for ch in body:
            if ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
            if ch == ',' and depth == 0:
                parts.append(cur.strip()); cur = ''
            else:
                cur += ch
        parts.append(cur.strip())
        cols, pk, uniq = {}, [], []
        for p in parts:
            pl = p.lower()
            if pl.startswith('primary key'):
                pk = [c.strip(' `"') for c in re.search(r'\((.*)\)', p).group(1).split(',')]
            elif pl.startswith('constraint') or pl.startswith('unique') or pl.startswith('check'):
                uniq.append(p)
            else:
                cm = re.match(r'([`"]?[\w]+[`"]?)\s+(.*)', p)
                cols[cm.group(1).strip('`"')] = cm.group(2)
        tables[name] = {'columns': cols, 'pk': pk, 'constraints': uniq}
        continue
    m = re.match(r'alter table (?:if exists )?(\S+) add constraint (\S+) foreign key \((.*?)\) references (\S+)(?: \((.*?)\))?', flat, re.I)
    if m:
        fks.append({'table': m.group(1).strip('`"'), 'name': m.group(2), 'cols': m.group(3).replace(' ', ''),
                    'ref': m.group(4).strip('`"'), 'refcols': (m.group(5) or 'id').replace(' ', '')})
json.dump({'tables': tables, 'fks': fks}, open(dst, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
print(len(tables), 'tables,', sum(len(t['columns']) for t in tables.values()), 'columns,', len(fks), 'fks')
