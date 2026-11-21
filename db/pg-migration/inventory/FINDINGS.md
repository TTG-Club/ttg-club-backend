# Этап 0: итоги инвентаризации прод-БД

Источник: дамп `ttg2014` от 2026-11-18 (MySQL 8.0.46, 142 таблицы, 205 тыс. строк),
сверка с маппингом сущностей (`expected/expected-mysql.sql`, 122 таблицы до правки `HeroClass`, 121 после).
Полный сырой отчёт пересобирается командой
`python -I from_dump.py <дамп> expected/expected-mysql.json expected/expected-pg.json out`
и затем `python -I compare.py out expected/expected-mysql.json report.md`.

**Проверено на сервере отдельно (2026-11-18).** В дамп это не попадает, поэтому
выполнено вручную:
- триггеров нет (`SHOW TRIGGERS` вернул 0 строк);
- процедур нет (`SHOW PROCEDURE STATUS` вернул 0 строк);
- `sql_mode = ONLY_FULL_GROUP_BY, STRICT_TRANS_TABLES, NO_ZERO_IN_DATE, NO_ZERO_DATE,
  ERROR_FOR_DIVISION_BY_ZERO, NO_ENGINE_SUBSTITUTION` — **строгий режим**.

**Исправление (2026-11-18).** Первая версия инвентаризации сканировала сущности только
в пакете `model` и пропустила `club.dnd5.portal.repository.VerificationToken`. Таблица
`verification_token` (718 строк) используется и **не** удаляется; вторая часть очистки
исправлена. Цифры ниже даны с учётом этой сущности.

## Сводка

| | |
|---|---|
| таблиц в БД / в маппинге | 142 / 122 |
| лишних таблиц | 20 (с данными 9) |
| отсутствующих таблиц и колонок | 0 |
| лишних колонок | 49 |
| расхождений типов | 103 (почти все безвредны, см. ниже) |
| представлений | 1 (`full_text_search`, используется поиском) |
| висячих ссылок | 4 строки |
| collation | 7 разных, 10 таблиц в `latin1` |

## 1. Блокеры: связующие таблицы с лишними обязательными колонками

Hibernate не заполняет эти колонки, а в БД они `NOT NULL` без значения по умолчанию.
Триггеров, которые бы их заполняли, нет, `sql_mode` строгий. Значит, любая вставка
через JPA в эти таблицы падает с ошибкой `Field '…' doesn't have a default value`.

| таблица | лишняя колонка | что происходит |
|---|---|---|
| `options` | `option_type` | **Вероятный действующий баг.** Сущность `Option` с 2021 года хранит типы в `option_types` и колонку не пишет, а создание особенности в мастерской (`POST /api/v1/workshop/options`, на проде с июля) вставляет строку в `options`. Признак — пропуски в id через один (525, 527, 529, 531): неудачная вставка в InnoDB тоже расходует автоинкремент. В журнале правок нет ни одного создания особенности |
| `classes_spells` | `сharacter_class_id` (**первая буква — кириллическая «с»**) | таблица **пустая** (0 строк). Связь `HeroClass.spells` фактически не работает |
| `artifactes_cust_classes` | `magic_thing_id`, входит в PK | во всех 137 строках равна `magic_item_id`. Мастерская эту связь не заполняет, поэтому сейчас ничего не падает |
| `artifactes_weapons` | `magic_thing_id` + **FK с `id` строки на `artifactes.id`** | 141 строка, `magic_thing_id = magic_item_id`. FK на `id` выполняется случайно: автоинкремент пока не обогнал id предметов |

**Проверить баг с `options`:** создать тестовую особенность в мастерской или найти в
логах бэкенда `Field 'option_type' doesn't have a default value`. Чинится до переезда
одной командой: `ALTER TABLE options MODIFY option_type varchar(255) NULL`.

**Связь «класс ↔ заклинание».** `classes_spells` пустая, все данные в
`spells_hero_class` (2672 строки). Источник истины один, так что при переезде
`HeroClass.spells` нужно сделать `mappedBy = "heroClass"`, а `classes_spells` удалить.

**До переезда:** удалить или сделать `NULL`-able колонки `magic_thing_id`,
`сharacter_class_id`, `options.option_type` и FK `artifactes_weapons.id → artifactes`.

## 2. Мусор от `ddl-auto=update`

**Лишние колонки (49).** Основные группы:
- `id` с автоинкрементом в связующих таблицах: `artifactes_armors`, `artifactes_weapons`,
  `classes_spells`, `creatures_actions`, `creatures_languages`, `creatures_races`,
  `user_role`, `spell_damage_type`, `trait_*`, `class_*`, `god_domains` и др.
  Они безвредны, но составной PK по паре колонок в PG лучше этих `id`.
- Хвосты переименований: `classes.bone_hits`, `classes.skills`, `*.img` (5 таблиц),
  `*.alt_name`, `archetypes.hero_class_id` (+FK), `level_definitions.slot0/spells/mastery_bonus`,
  `spells.material_component`, `treasures.description`, `invitation.unique_identifier`,
  `taverna_prefixes.genitive_*`.
- `action_damage` с колонками `` `1` `` и `` `2` `` `NOT NULL` и FK на `creature_actions_data`
  (таблица пустая).

**Лишние таблицы (20).** В коде бэкенда ни одна из них не упоминается. Во фронте слова `articles`/`updates`
встречаются только в разделе новостей, и к этим таблицам он не обращается.

| с данными | строк |
|---|---|
| `skills` + `creatures_skills` | 1365 / 1388 |
| `bonus_saving_throws` | 938 |
| `background_personalization_feature` | 139 |
| `places` | 117 |
| `backgrounds_equipments` | 59 |
| `creatures_immunity_states` | 12 |
| `site_references` | 9 |
| `archetypes_spells` | 2 |

Пустые: `action_data`, `action_data_types`, `archives`, `articles`, `class_perosonolizations`,
`creature_actions_data`, `creature_spellcaster_spells`, `creatures_spells`, `magic_books`,
`random_creatures`, `updates`.

Это старые версии связей (`skills`/`bonus_saving_throws` до `creature_skills`/
`creature_saving_throws`) и выключенные фичи. **Предложение:** перед удалением сделать
дамп этих таблиц в архив, затем удалить одним скриптом. В PostgreSQL их не переносить.

## 3. Представление `full_text_search`

Его не создаёт Hibernate, и на нём построен глобальный поиск
(`SearchRepository`, нативный SQL). Определение — `UNION` по заклинаниям, классам,
умениям, архетипам, расам и т.д. с `concat`/`replace`/`lower`.

- В PG его нужно создать **отдельным changeset'ом Liquibase**. Синтаксис почти
  совместим: убрать обратные кавычки; `concat` и `replace` есть в PG.
- `findByIndex` делает `OFFSET` без `ORDER BY`: порядок и в MySQL не гарантирован,
  а в PG почти наверняка будет другим. Нужно добавить сортировку.
- Это хороший кандидат на материализованное представление с триграммным индексом.

## 4. Кодировки и collation

- В схеме 7 разных collation: `utf8mb3_general_ci` (90 таблиц), `utf8mb4_0900_ai_ci` (35),
  `latin1_swedish_ci` (10), `utf8mb3_bin` (4) и по одной `utf8mb3_unicode_ci`,
  `utf8mb4_bin`, `utf8mb4_unicode_ci`.
- В `latin1`-колонках лежат только ASCII-коды enum'ов и URL. **Кириллица не
  повреждена**, конвертация в UTF-8 безопасна.
- `utf8mb3` не хранит 4-байтные символы (эмодзи). После переезда в PG это ограничение
  исчезнет само.
- Колонки `_bin` (`artifactes`, `rules`, `spells_times`, `menu`) сравниваются с учётом
  регистра уже сейчас, так что для них PG поведение не изменит.

## 5. Регистр и пробелы (поведение `_ci` → PG)

Дублей ключей, которые различаются **только** регистром или пробелами, нет:
`english_name` в справочниках, `books.source`, `users.email` и `users.username` чистые.
Переход на регистрозависимое сравнение в PG `findBy…` не сломает.

**Почистить до переезда:**
- **Пробелы по краям** в `name`/`english_name`: ~400 строк, больше всего в
  `hero_class_feats.name` (136), `creature_actions.english_name` (68), `artifactes.name` (21),
  `places.name` (19). В MySQL `=` игнорирует хвостовые пробелы (`PAD SPACE`), в PG — нет.
- **Точные дубли `english_name`**: `archetypes` (20, ожидаемо — одинаковые архетипы
  у разных классов), `races` (4), `tokens` (4), `race_features` (1). Уникальный индекс
  по этим колонкам в PG без чистки не создать.

## 6. Висячие ссылки

| связь | значение | строк |
|---|---|---|
| `artifactes.source → books` | `ToA` | 1 |
| `equipments.source → books` | `ToA` | 2 |
| `screens.source → books` | `GoS` | 1 |

Нужно завести книги `ToA` и `GoS` или поправить `source`. Иначе FK в PG не создастся,
а при переносе с проверкой ключей упадёт вставка.

FK из маппинга, которых нет в БД (`bookmarks`, `dm_screens`, `user_*` → `users`,
`*.source → books`), висячих строк не имеют. Создать их в PG безопасно.

## 7. JSON в тексте: `json_storage.json_data`

48 из 3896 записей — **невалидный JSON** (46 существ и 2 заклинания, Foundry V11):

- **24 записи обрезаны ровно на 32 767 символах.** Похоже на лимит где-то на пути записи
  (импорт или поле формы). Эти экспорты повреждены и в MySQL.
- **24 записи** содержат неэкранированные `"` внутри HTML (`<div class="rd__b">`).

`revision_history.snapshot`: 41 запись, все валидны.

Перенос в `jsonb` возможен только после пересборки или удаления 48 битых записей.
Поскольку `json_storage` — кэш экспорта для Foundry, их проще перегенерировать.

## 8. Типы: что важно для PG

Расхождения типов почти все безвредны:
- `varchar` в БД против `enum(...)` в маппинге — это особенность диалекта Hibernate,
  в PG будет `varchar` + `CHECK`;
- `int` против `tinyint`;
- `longtext`/`text` против `varchar(255)` — в БД шире, данные не обрежутся.

Что нужно учесть:
- **ORDINAL-enum'ы** (19 колонок) хранятся как `int`. Значений вне диапазона нет, так
  что на этапе 2 перевод в имена пройдёт чисто.
- **`users.id`, `roles.id`, `user_role.*`, `youtube_videos.user_id`** в БД `int`,
  а маппинг ждёт `bigint` (`Long`). В PG делать `bigint`.
- **`bookmarks.uuid` и `parent_id`** — `varchar(255)`, а в PG должен быть `uuid`.
  Невалидные значения проверить перед переносом.
- **`menu`**, единственная таблица на `utf8mb4_bin`, — без PK.

## 9. Таблицы без PK с данными

`spell_savingthrows` (455), `spell_heal_type` (36), `menu` (33), `creatures_immunity_states` (12).
Полных дублей в связующих таблицах нет, так что составной PK в PG можно создавать
без чистки.

## Открытые вопросы

1. Подтвердить баг с созданием особенностей (`options.option_type`, см. п. 1).
2. Используется ли где-то вне бэкенда `places` (117 строк).
3. Поиск (`full_text_search`) сейчас работает под `ONLY_FULL_GROUP_BY` — PG так же строг
   к `GROUP BY`, так что запросы, которые проходят на проде, пройдут и в PG.

## Скрипты очистки (до этапа 1)

Порядок: выкатить код → свежий полный дамп (архив) → часть 1 → решение по 23
особенностям → часть 2.

**Код.** `HeroClass.spells` теперь `mappedBy = "heroClass"`, а у `Spell.heroClass`
явно заданы имена колонок `spells_hero_class` (иначе Hibernate ждал бы `spells_id`
вместо `spell_id` и на `ddl-auto=update` добавил бы колонку). Если скрипт выполнить
раньше выкатки, старый код при перезапуске пересоздаст пустую `classes_spells` с
правильными колонками — это безвредно, но лучше выкатить код первым.

**[Часть 1](../../2026-11-18-pg-prep-1-safe-cleanup.sql) — без потери данных:**
1. `options.option_type` → `NULL` (исправление бага; можно выполнить сразу и отдельно).
2. `ToA` → `TOA`, `GoS` → `GOS` в `artifactes`, `equipments`, `screens`.
3. Обрезка пробельных символов по краям `name`/`english_name` в 36 колонках (у 11 страниц меняется URL).
4. `magic_thing_id` из `artifactes_cust_classes` / `artifactes_weapons` + FK с `id` строки;
   PK `artifactes_cust_classes` пересоздаётся по `(magic_item_id, cust_classes_id)`.
5. `action_damage`: мусорные колонки, ссылка на пустую таблицу, недостающий PK.
6. 12 пустых таблиц, включая `classes_spells` (удаляются, только если пустые).
7. 6 пустых колонок (удаляются, только если пустые).

**[Часть 2](../../2026-11-18-pg-prep-2-drop-unused-data.sql) — удаление неиспользуемых данных:**
- 9 таблиц с данными, которых нет в сущностях;
- 8 колонок с данными, которых нет в сущностях, и `archetypes.hero_class_id` с его FK;
- `options.option_type`. Перед этим решить, возвращать ли в списки 23 особенности
  (блок `INSERT` в начале скрипта выключен).

**Не в скриптах, руками:**
- 48 битых записей `json_storage` — исходные данные экспорта, не кэш, пересобрать
  автоматически нельзя;
- точные дубли `english_name` (`races` 4, `tokens` 4, `race_features` 1) — разобрать
  до создания уникальных индексов в PG.

**Что остаётся после обеих частей** (проверено симуляцией на метаданных дампа: 121
таблица против 121 в маппинге, отсутствующих таблиц и колонок нет):
- 20 суррогатных `id` в связующих таблицах — безвредны, уйдут на этапе 2 вместе с
  переводом коллекций в jsonb;
- 6 колонок, которые читает `full_text_search` (`classes.alt_name`,
  `archetypes.alt_name`, `hero_class_traits.alt_name/english_name`,
  `hero_class_feats.alt_name/english_name`). В PG их нужно добавить к начальной схеме,
  потому что в сущностях их нет;
- 9 FK из маппинга, которых нет в БД (`user_id → users`, `source → books`) — висячих
  строк по ним нет, Liquibase создаст их в PG.

SQL-синтаксис скриптов на MySQL не прогонялся (локального сервера нет): перед продом
нужно выполнить их на копии базы.
