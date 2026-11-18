-- Очистка перед переездом на PostgreSQL, часть 1: без потери данных.
--
-- Этап 0 переезда на PostgreSQL. Основание — db/pg-migration/inventory/FINDINGS.md
-- (инвентаризация прод-дампа от 2026-11-18).
--
-- Исправляет баг вставки в options, приводит ссылки и ключевые строки к виду, в котором
-- PostgreSQL сравнивает их так же, как MySQL, и удаляет только пустые объекты.
-- Ни одна строка с данными не удаляется.
--
-- Перед запуском:
--   1. Сделать полный дамп базы непосредственно перед запуском — это и есть архив.
--   2. Выбрать схему (USE ...), имя базы в скрипте не указано.
--
-- Скрипт можно запускать повторно: каждый шаг проверяет, есть ли ещё что менять.
-- DDL в MySQL коммитится сразу, поэтому скрипт не атомарный; при ошибке исправьте
-- причину и запустите его снова.

SET @old_safe_updates = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- =====================================================================================
-- 1. options.option_type: колонка NOT NULL без значения по умолчанию.
--    Сущность Option её не пишет (типы хранятся в option_types), поэтому в strict-режиме
--    создание особенности в мастерской падает с
--    "Field 'option_type' doesn't have a default value". Данные в колонке сохраняются.
--    Можно запустить отдельно и сразу — это исправление действующего бага.
-- =====================================================================================
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `options` MODIFY `option_type` varchar(255) NULL', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'options' AND column_name = 'option_type');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================
-- 2. Ссылки на книги в другом регистре: ToA -> TOA, GoS -> GOS.
--    MySQL (_ci) считает их равными, и FK выполняется; в PostgreSQL ссылка повиснет.
-- =====================================================================================
UPDATE artifactes SET source = 'TOA' WHERE BINARY source = 'ToA';
UPDATE equipments SET source = 'TOA' WHERE BINARY source = 'ToA';
UPDATE screens SET source = 'GOS' WHERE BINARY source = 'GoS';

-- =====================================================================================
-- 3. Пробельные символы по краям name / english_name (~400 строк).
--    MySQL при сравнении через = игнорирует хвостовые пробелы, PostgreSQL — нет.
--    Кроме пробела встречаются \r, \n, табуляция и неразрывный пробел (U+00A0);
--    \s в регулярках MySQL 8 (ICU) покрывает их все.
--    URL страниц строится из english_name без trim, поэтому у 11 страниц URL изменится.
--    Старые ссылки с хвостовым "_" в MySQL продолжат открываться (сравнение без учёта
--    хвостовых пробелов). Не откроются старые ссылки на страницы с пробелом в начале:
--    screens " Improvised Movement", " Charm of the Eldritch Eye", books " That Old Black Magic".
--    Уникальные индексы (artifactes, equipments, rules, screens, creature_races) после
--    обрезки не нарушаются — проверено по дампу.
-- =====================================================================================
UPDATE `artifactes` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `artifactes` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `backgrounds` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `bookmarks` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `books` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `books` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `classes_feature_level_definitions` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `creature_actions` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `creature_actions` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `creature_feats` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `creature_feats` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `creature_races` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `creatures` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `equipments` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `equipments` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `hero_class_feats` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `hero_class_feats` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `hero_class_traits` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `hero_class_traits` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `options` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `places` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `race_features` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `race_names` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `race_nicknames` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `rules` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `rules` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `screens` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `screens` SET `english_name` = REGEXP_REPLACE(`english_name`, '^\\s+|\\s+$', '') WHERE `english_name` REGEXP '^\\s|\\s$';
UPDATE `spells` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `taverna_atmosphera` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `taverna_dishes` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `taverna_drinks` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `taverna_topics` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `tokens` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `traits` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';
UPDATE `treasures` SET `name` = REGEXP_REPLACE(`name`, '^\\s+|\\s+$', '') WHERE `name` REGEXP '^\\s|\\s$';

-- =====================================================================================
-- 4. artifactes_cust_classes / artifactes_weapons: лишняя колонка magic_thing_id.
--    Во всех строках она равна magic_item_id, Hibernate её не пишет. Связь хранится
--    в magic_item_id, поэтому данные не теряются.
--    В artifactes_weapons ещё и FK с id строки на artifactes.id — выполняется случайно.
-- =====================================================================================
-- Проверка: mismatched должно быть 0. Если нет — остановиться и разобраться.
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT ''artifactes_cust_classes'' AS tbl, COUNT(*) AS mismatched FROM artifactes_cust_classes WHERE magic_thing_id <> magic_item_id', 'SELECT ''artifactes_cust_classes.magic_thing_id: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'artifactes_cust_classes' AND column_name = 'magic_thing_id');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT ''artifactes_weapons'' AS tbl, COUNT(*) AS mismatched FROM artifactes_weapons WHERE magic_thing_id <> magic_item_id', 'SELECT ''artifactes_weapons.magic_thing_id: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'artifactes_weapons' AND column_name = 'magic_thing_id');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `artifactes_cust_classes` DROP FOREIGN KEY `FKlryxyy0p9pltb4uwi3jx2sugl`', 'DO 0') FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'artifactes_cust_classes' AND constraint_name = 'FKlryxyy0p9pltb4uwi3jx2sugl'
      AND constraint_type = 'FOREIGN KEY');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `artifactes_cust_classes` DROP PRIMARY KEY, DROP COLUMN `magic_thing_id`, ADD PRIMARY KEY (`magic_item_id`, `cust_classes_id`)', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'artifactes_cust_classes' AND column_name = 'magic_thing_id');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `artifactes_weapons` DROP FOREIGN KEY `FKf2kn6ortr3ktab48509244fax`', 'DO 0') FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'artifactes_weapons' AND constraint_name = 'FKf2kn6ortr3ktab48509244fax'
      AND constraint_type = 'FOREIGN KEY');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `artifactes_weapons` DROP INDEX `FKf2kn6ortr3ktab48509244fax`', 'DO 0') FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'artifactes_weapons' AND index_name = 'FKf2kn6ortr3ktab48509244fax');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `artifactes_weapons` DROP FOREIGN KEY `FKpxvifbv8624g460us0oys8xlk`', 'DO 0') FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'artifactes_weapons' AND constraint_name = 'FKpxvifbv8624g460us0oys8xlk'
      AND constraint_type = 'FOREIGN KEY');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `artifactes_weapons` DROP COLUMN `magic_thing_id`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'artifactes_weapons' AND column_name = 'magic_thing_id');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================
-- 5. action_damage: мусорные колонки `1`, `2` (NOT NULL) и ссылка на пустую
--    creature_actions_data. Таблица пустая. У неё нет PK, хотя сущность ждёт id.
-- =====================================================================================
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `action_damage` DROP FOREIGN KEY `FKro7p07eqo65vvrs8mssh37qcm`', 'DO 0') FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'action_damage' AND constraint_name = 'FKro7p07eqo65vvrs8mssh37qcm'
      AND constraint_type = 'FOREIGN KEY');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `action_damage` DROP COLUMN `1`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'action_damage' AND column_name = '1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `action_damage` DROP COLUMN `2`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'action_damage' AND column_name = '2');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `action_damage` DROP COLUMN `action_data_id`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'action_damage' AND column_name = 'action_data_id');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE `action_damage` ADD PRIMARY KEY (`id`)', 'DO 0')
    FROM information_schema.table_constraints WHERE table_schema = DATABASE()
      AND table_name = 'action_damage' AND constraint_type = 'PRIMARY KEY');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `creature_actions_data`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'creature_actions_data');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `creature_actions_data`',
    'SELECT ''ПРОПУЩЕНО: creature_actions_data не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================
-- 6. Пустые таблицы, которых нет в сущностях, и пустая classes_spells.
--    classes_spells — вторая join-таблица связи «класс <-> заклинание» с колонкой
--    "сharacter_class_id" (первая буква кириллическая), из-за которой туда ничего
--    не вставлялось. Все связи — в spells_hero_class. В коде HeroClass.spells
--    переведена на mappedBy, чтобы Hibernate её не пересоздавал.
--    Каждая таблица удаляется, только если пустая.
-- =====================================================================================
-- action_data_types
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `action_data_types`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'action_data_types');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `action_data_types`',
    'SELECT ''ПРОПУЩЕНО: action_data_types не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- action_data
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `action_data`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'action_data');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `action_data`',
    'SELECT ''ПРОПУЩЕНО: action_data не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- archives
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `archives`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'archives');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `archives`',
    'SELECT ''ПРОПУЩЕНО: archives не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- articles
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `articles`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'articles');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `articles`',
    'SELECT ''ПРОПУЩЕНО: articles не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- class_perosonolizations
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `class_perosonolizations`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'class_perosonolizations');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `class_perosonolizations`',
    'SELECT ''ПРОПУЩЕНО: class_perosonolizations не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- creature_spellcaster_spells
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `creature_spellcaster_spells`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'creature_spellcaster_spells');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `creature_spellcaster_spells`',
    'SELECT ''ПРОПУЩЕНО: creature_spellcaster_spells не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- creatures_spells
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `creatures_spells`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'creatures_spells');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `creatures_spells`',
    'SELECT ''ПРОПУЩЕНО: creatures_spells не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- magic_books
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `magic_books`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'magic_books');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `magic_books`',
    'SELECT ''ПРОПУЩЕНО: magic_books не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- random_creatures
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `random_creatures`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'random_creatures');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `random_creatures`',
    'SELECT ''ПРОПУЩЕНО: random_creatures не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- updates
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `updates`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'updates');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `updates`',
    'SELECT ''ПРОПУЩЕНО: updates не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- classes_spells
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `classes_spells`', 'DO 0')
    FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'classes_spells');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DROP TABLE IF EXISTS `classes_spells`',
    'SELECT ''ПРОПУЩЕНО: classes_spells не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================
-- 7. Пустые колонки, которых нет в сущностях.
-- =====================================================================================
-- Каждая колонка удаляется, только если в ней нет значений; иначе печатается
-- предупреждение, и колонку нужно перенести во вторую часть.
-- level_definitions.mastery_bonus
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `level_definitions` WHERE `mastery_bonus` IS NOT NULL AND CAST(`mastery_bonus` AS CHAR) <> ''''', 'SELECT ''level_definitions.mastery_bonus: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'level_definitions' AND column_name = 'mastery_bonus');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DO 0', 'SELECT ''ПРОПУЩЕНО: level_definitions.mastery_bonus не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0 AND @n = 0, 'ALTER TABLE `level_definitions` DROP COLUMN `mastery_bonus`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'level_definitions' AND column_name = 'mastery_bonus');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- level_definitions.slot0
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `level_definitions` WHERE `slot0` IS NOT NULL AND CAST(`slot0` AS CHAR) <> ''''', 'SELECT ''level_definitions.slot0: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'level_definitions' AND column_name = 'slot0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DO 0', 'SELECT ''ПРОПУЩЕНО: level_definitions.slot0 не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0 AND @n = 0, 'ALTER TABLE `level_definitions` DROP COLUMN `slot0`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'level_definitions' AND column_name = 'slot0');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- level_definitions.spells
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `level_definitions` WHERE `spells` IS NOT NULL AND CAST(`spells` AS CHAR) <> ''''', 'SELECT ''level_definitions.spells: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'level_definitions' AND column_name = 'spells');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DO 0', 'SELECT ''ПРОПУЩЕНО: level_definitions.spells не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0 AND @n = 0, 'ALTER TABLE `level_definitions` DROP COLUMN `spells`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'level_definitions' AND column_name = 'spells');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- treasures.description
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `treasures` WHERE `description` IS NOT NULL AND CAST(`description` AS CHAR) <> ''''', 'SELECT ''treasures.description: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'treasures' AND column_name = 'description');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DO 0', 'SELECT ''ПРОПУЩЕНО: treasures.description не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0 AND @n = 0, 'ALTER TABLE `treasures` DROP COLUMN `description`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'treasures' AND column_name = 'description');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- spells.img
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `spells` WHERE `img` IS NOT NULL AND CAST(`img` AS CHAR) <> ''''', 'SELECT ''spells.img: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'spells' AND column_name = 'img');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DO 0', 'SELECT ''ПРОПУЩЕНО: spells.img не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0 AND @n = 0, 'ALTER TABLE `spells` DROP COLUMN `img`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'spells' AND column_name = 'img');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- invitation.unique_identifier
SET @n = 0;
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT COUNT(*) INTO @n FROM `invitation` WHERE `unique_identifier` IS NOT NULL AND CAST(`unique_identifier` AS CHAR) <> ''''', 'SELECT ''invitation.unique_identifier: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'invitation' AND column_name = 'unique_identifier');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = IF(@n = 0, 'DO 0', 'SELECT ''ПРОПУЩЕНО: invitation.unique_identifier не пустая'' AS warning');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0 AND @n = 0, 'ALTER TABLE `invitation` DROP COLUMN `unique_identifier`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'invitation' AND column_name = 'unique_identifier');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================
-- Итоговая проверка: каждая строка должна показывать 0.
-- =====================================================================================
SELECT 'source в другом регистре' AS chk, COUNT(*) AS cnt FROM (
    SELECT source FROM artifactes WHERE BINARY source IN ('ToA', 'GoS')
    UNION ALL SELECT source FROM equipments WHERE BINARY source IN ('ToA', 'GoS')
    UNION ALL SELECT source FROM screens WHERE BINARY source IN ('ToA', 'GoS')) x
UNION ALL
SELECT 'пробелы по краям name (spells, creatures, options)', COUNT(*) FROM (
    SELECT name FROM spells WHERE name REGEXP '^\\s|\\s$'
    UNION ALL SELECT name FROM creatures WHERE name REGEXP '^\\s|\\s$'
    UNION ALL SELECT name FROM options WHERE name REGEXP '^\\s|\\s$') y
UNION ALL
SELECT 'оставшиеся объекты', COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND (
        (table_name IN ('artifactes_cust_classes', 'artifactes_weapons') AND column_name = 'magic_thing_id')
        OR (table_name = 'action_damage' AND column_name IN ('1', '2', 'action_data_id'))
        OR (table_name = 'classes_spells'))
UNION ALL
SELECT 'options.option_type всё ещё NOT NULL', COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'options' AND column_name = 'option_type'
      AND is_nullable = 'NO';

SET SQL_SAFE_UPDATES = @old_safe_updates;
