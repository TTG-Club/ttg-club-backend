# Этап 0: инвентаризация схемы MySQL

Сравнивает фактическую схему БД с тем, что ожидают сущности Hibernate, и проверяет
данные на то, что сломается при переезде на PostgreSQL. **Только чтение**: сессия
открывается в режиме `READ ONLY`, выполняются только `SELECT` к `information_schema` и
таблицам.

## Запуск

```powershell
.\db\pg-migration\inventory\run.ps1
```

Подключение берётся из `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD`, а если их
нет — из `src/main/resources/application-local.properties`. Нужны JDK 25, Maven
(офлайн-репозиторий) и Python 3.

Результат — `report.md` (в `.gitignore`), сырые выгрузки — `out/`.

### По дампу, без доступа к серверу

```powershell
python -I from_dump.py <дамп.sql> expected\expected-mysql.json expected\expected-pg.json out
python -I compare.py out expected\expected-mysql.json report.md
```

`from_dump.py` разбирает `CREATE TABLE` и `INSERT` из mysqldump и пишет те же TSV, что
снимаются с живой базы. Триггеры и процедуры есть в отчёте только если дамп снят
без `--skip-triggers` и с `--routines`.

Выводы по прод-дампу от 2026-11-18 — в [FINDINGS.md](FINDINGS.md).

## Что внутри

| файл | что делает |
|---|---|
| `SchemaGen.java` | строит DDL из сущностей без подключения к БД, со стратегиями именования Spring Boot 4 |
| `expected/expected-{mysql,pg}.sql` | ожидаемая схема (121 таблица) для MySQL и PostgreSQL; PG-версия — заготовка для начального changelog Liquibase |
| `parse_ddl.py` | DDL → JSON (таблицы, колонки, PK, FK) |
| `meta.sql` | версия, sql_mode, collation, таблицы, колонки, FK, индексы, представления, триггеры, процедуры |
| `gen_checks.py` | генерирует `checks.sql`: точное число строк, висячие FK, дубли в join-таблицах, дубли без учёта регистра, пробелы, нулевые даты, ORDINAL вне диапазона, NUL в тексте, валидность JSON |
| `Dump.java` | выполняет блоки `-- @имя` и пишет каждый в `out/имя.tsv` |
| `from_dump.py` | то же, но по файлу mysqldump, без сервера |
| `compare.py` | сводит всё в `report.md` |
