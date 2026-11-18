# Переезд с MySQL на PostgreSQL

| этап | что | где |
|---|---|---|
| 0 | инвентаризация схемы и данных, очистка MySQL | [inventory/](inventory/README.md), `db/2026-11-18-pg-prep-*.sql` |
| 1 | перенос 1:1: схема в Liquibase, данные из дампа, `ddl-auto=validate` | этот файл, [baseline/](baseline/), [load/](load/) |
| 2 | вложенные коллекции → `jsonb` вместо join-таблиц | позже |

## Этап 1: как устроено

**Схема** — `src/main/resources/db/changelog`. Приложение применяет её при старте
(Spring Boot + Liquibase), Hibernate только сверяет маппинг (`ddl-auto=validate`).
Начальные файлы `0001`–`0005` сгенерированы [baseline/gen_baseline.py](baseline/gen_baseline.py)
из DDL Hibernate с поправками по реальной MySQL:

- **сравнение строк без учёта регистра и диакритики**, как `_ci` в MySQL (`Goblin` = `goblin`,
  `ё` = `е`; `й` остаётся отдельной буквой) — недетерминированная ICU-collation `ci`
  (`und-u-ks-level1`) на строковых колонках. Без неё ломаются все детальные страницы:
  URL `goblin` ищется как `englishName = 'goblin'`, а в базе `Goblin`. Требует
  **PostgreSQL 18+** (на 18 с такой collation работает `LIKE`);
- ширина колонок и `NULL` — как в MySQL, где маппинг строже базы;
- уникальность — только та, что есть в MySQL;
- индексы MySQL плюс индексы на всех FK (PostgreSQL сам их не создаёт);
- представление `full_text_search` (глобальный поиск) и 6 колонок, которые читает только оно.

**Порядок элементов в списках.** В MySQL коллекции читались без `ORDER BY`, и порядок
задавал индекс выборки. В PostgreSQL порядок без `ORDER BY` не гарантирован, поэтому:

- коллекции сущностей (`@OneToMany` по FK) — `@OrderBy("id")`;
- коллекции с отдельной таблицей (`@ElementCollection`, `@ManyToMany`, `@OneToMany` с
  `@JoinTable`) — `@OrderColumn(name = "sort_order")`; загрузчик заполняет `sort_order`
  так, как MySQL отдавал строки (по составному ключу владельца, иначе по `id`, иначе по
  порядку строк).

Запросы `SELECT DISTINCT … JOIN …` без `ORDER BY` получили явный порядок, совпадающий с
тем, что давал MySQL (подклассы и расы у заклинания, категории правил).

**Ключевые слова.** Колонки `start`, `end`, `order`, `rank`, `condition`… — в кавычках
(`hibernate.auto_quote_keyword=true`).

**NULL в сортировке** — первыми (`hibernate.order_by.default_null_ordering=first`), как
MySQL при сортировке по возрастанию (книги без года в фильтрах — в начале списка).

## Известные отличия от MySQL

- **Сортировка по убыванию** колонки с `NULL`: MySQL ставил `NULL` в конец, теперь —
  в начало. Касается только пользовательской сортировки по убыванию в списках
  (предыстории, книги, предметы, сокровища, особенности), где сортируют по названию без `NULL`.
- **Равные ключи сортировки** (например, книги одного года в фильтрах): MySQL отдавал их
  в произвольном порядке, PostgreSQL отдаёт в своём. Состав списков тот же.
- **Хвостовые пробелы в сравнении.** MySQL (`PAD SPACE`) считал `'Charisma '` равным
  `'Charisma'`, PostgreSQL — нет. Существующие данные очищены первой частью очистки;
  новые значения с пробелами по краям из мастерской будут искаться только точно.

**Данные** — [load/dump_to_pg.py](load/dump_to_pg.py) превращает mysqldump в SQL с `COPY`.
До записи файла проверяет всё, на чём упала бы загрузка: `NOT NULL`, длину строк,
значения enum, диапазоны чисел, уникальность PK, внешние ключи (с учётом `ci`). Однозначные
исправления делает сам и печатает отчёт.

## Порядок переезда

1. Выполнить обе части очистки MySQL (`db/2026-11-18-pg-prep-1-*.sql`, `-2-*.sql`).
2. Создать пустую базу PostgreSQL 18+. Пользователю для загрузки нужно право менять
   `session_replication_role` (суперпользователь или
   `GRANT SET ON PARAMETER session_replication_role TO ...`).
3. Свежий дамп MySQL: `mysqldump --single-transaction --skip-triggers <база> > dump.sql`.
4. Создать схему: запустить приложение на пустой базе (Liquibase применит changelog;
   `validate` должен пройти) и остановить его.
5. Сгенерировать и загрузить данные:
   ```
   python -I db/pg-migration/load/dump_to_pg.py dump.sql src/main/resources/db/changelog/changes data.sql
   psql -v ON_ERROR_STOP=1 -d <база> -f data.sql
   ```
   Без `psql` — `java -cp <classpath с драйвером PG> db/pg-migration/load/LoadPg.java data.sql`
   (подключение из `SPRING_DATASOURCE_*`).
6. Запустить приложение на PostgreSQL и сверить API с MySQL-версией.

## Как пересобрать baseline

Нужно, если меняется маппинг **до** первого применения changelog на проде. После — только
новые файлы в `changes/`.

```
mvn -o compile dependency:build-classpath -Dmdep.outputFile=db/pg-migration/inventory/out/cp.txt
# в cp.txt первой строкой добавить target/classes
java -cp @db/pg-migration/inventory/out/cp.txt db/pg-migration/inventory/SchemaGen.java target/classes \
     db/pg-migration/inventory/expected/expected-pg.sql org.hibernate.dialect.PostgreSQLDialect \
     db/pg-migration/inventory/expected/collection-tables.json
python -I db/pg-migration/inventory/from_dump.py <дамп> ... out      # метаданные MySQL
python -I db/pg-migration/baseline/gen_baseline.py db/pg-migration/inventory/expected/expected-pg.sql \
       db/pg-migration/inventory/out <дамп> src/main/resources/db/changelog
```
