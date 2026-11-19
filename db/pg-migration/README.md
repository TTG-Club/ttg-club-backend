# Переезд с MySQL на PostgreSQL

| этап | что | где |
|---|---|---|
| 0 | инвентаризация схемы и данных, очистка MySQL | [inventory/](inventory/README.md), `db/2026-11-18-pg-prep-*.sql` |
| 1 | перенос 1:1: схема в Liquibase, данные из дампа, `ddl-auto=validate` | этот файл, [baseline/](baseline/), [load/](load/) |
| 2 | вложенные коллекции → `jsonb` вместо join-таблиц, фильтры на QueryDSL | [ниже](#этап-2-jsonb-и-querydsl), `changes/0006`+ |
| 3 | тексты: HTML → разметка core-api (`{@roll}`, `{@b}`, `{@item …\|url:…}`…) | [ниже](#этап-3-тексты-в-формате-core-api), план |

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
4. Создать схему **до метки `data-load`** (схема 1:1 с MySQL, в неё пишет загрузчик):
   ```
   java -cp <classpath приложения> db/pg-migration/load/LiquibaseUpdate.java data-load
   ```
5. Сгенерировать и загрузить данные:
   ```
   python -I db/pg-migration/load/dump_to_pg.py dump.sql src/main/resources/db/changelog/changes data.sql
   psql -v ON_ERROR_STOP=1 -d <база> -f data.sql
   ```
   Без `psql` — `java -cp <classpath с драйвером PG> db/pg-migration/load/LoadPg.java data.sql`
   (подключение из `SPRING_DATASOURCE_*`).
6. Запустить приложение: Liquibase применит changeset'ы после метки (этап 2 — перенос
   данных в `jsonb`), `validate` должен пройти. Сверить API с MySQL-версией.

**Не запускать приложение на пустой базе до загрузки данных**: оно применит все changeset'ы,
включая удаление таблиц коллекций, и загружать будет некуда.

## Как пересобрать baseline

**С этапа 2 baseline (`0001`–`0005`) заморожен**: маппинг ушёл вперёд (коллекции существ —
в `jsonb`), и генерация из сущностей дала бы схему, в которую загрузчик не сможет писать.
Менять схему — только новыми файлами в `changes/`. Скрипты ниже — для истории.

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

## Этап 2: jsonb и QueryDSL

Вложенные коллекции, которые не живут отдельно от владельца, переезжают из join-таблиц в
`jsonb`-колонки владельца. Порядок для каждой сущности — expand → migrate → contract:

1. changeset добавляет `jsonb`-колонку и переносит в неё данные из старой таблицы
   в порядке `sort_order`; значения, хранившиеся числом (`EnumType.ORDINAL`), переводятся
   в имена констант enum; для фильтруемых колонок — GIN-индекс `jsonb_path_ops`;
2. маппинг: `@JdbcTypeCode(SqlTypes.JSON)` на том же поле (`List<Enum>`), DTO и API не меняются;
3. отдельный changeset удаляет старую таблицу (rollback восстанавливает её из `jsonb`).

**Фильтры** — QueryDSL (форк OpenFeign 7.x, как на core-api), `QuerydslPredicateExecutor`
у репозитория, построитель условий на сущность (`service/bestiary/CreaturePredicateBuilder`).
Условия по `jsonb` — функция `jsonb_contains` (`config/jpa/JsonbFunctionContributor`,
оператор `@>`, значение — параметром запроса, работает по GIN); по связям — `any()`
(EXISTS-подзапрос) вместо `JOIN … DISTINCT`.

| сущность | в jsonb | changeset |
|---|---|---|
| `Creature` | `armorTypes`, `immunityStates`, `immunityDamages`, `resistanceDamages`, `vulnerabilityDamages`, `habitates` | `0007`, `0008` |
| `Creature` | `savingThrows`, `skills`, `feats`, `spellcasters` (объекты без `id`, сериализуются по полям) | `0009`, `0010` |
| `Creature` | `actions` — у каждого существа своя копия (раньше до 19 существ делили одно действие, но мастерская при сохранении всё равно создавала новое); `sharedUsageCount` в API больше не заполняется | `0011`, `0012` |

Остаются связями (не `jsonb`): то, что разделяется между записями или имеет свою страницу —
языки, теги (`CreatureRace`), книга, логово.

## Этап 3: тексты в формате core-api

Сейчас описания (заклинания, существа, классы, расы, предметы…) хранятся HTML-ом, местами
с частичной разметкой (`{@atk mw}`, `{@hit 4}`, `{@damage 1к6}`), которую бэкенд переводит
в HTML при отдаче (`MarkdownUtil`). На core-api описания — строки с собственной разметкой
тегами `{@…}`: `{@roll формула}`, `{@b}`, `{@i}`, `{@u}`, `{@spoiler}`, `{@br}`, ссылки
`{@item название|url:…}`, `{@spell …}`. Рендерер — `core-app/app/features/articles/model/markup.ts`,
разбор на бэкенде — `VttgMarkupConverter`.

План (по сущностям, как этап 2):
1. Конвертер HTML → разметка core-api с отчётом о том, что не переводится однозначно
   (произвольные теги, таблицы, стили); прогон по копии базы, ручной разбор остатка.
2. Рендерер разметки во фронтенде старого сайта (порт из core-app) — до переключения данных.
3. Changeset на сущность: перевод текстов; бэкенд отдаёт разметку, `MarkdownUtil` и флаг
   `markdown` у действий и умений уходят.
4. Мастерская — редактор разметки вместо HTML.
