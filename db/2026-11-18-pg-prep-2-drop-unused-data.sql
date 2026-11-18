-- Очистка перед переездом на PostgreSQL, часть 2: удаление неиспользуемых данных.
--
-- Этап 0 переезда на PostgreSQL. Основание — db/pg-migration/inventory/FINDINGS.md
-- (инвентаризация прод-дампа от 2026-11-18).
--
-- Удаляет таблицы и колонки, которых нет в сущностях, но в которых есть данные.
-- Бэкенд их не читает и не пишет. Запускать после части 1 и только после
-- полного дампа: восстановить удалённое можно только из него.
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
-- 0. (ВЫКЛЮЧЕНО, решение по контенту) 23 особенности, у которых тип есть только
--    в options.option_type, а в option_types записей нет. Сейчас их нет ни в одном
--    списке на сайте: 18 воззваний колдуна (id 188-205, в основном UA) и 5 боевых
--    приёмов (id 453-457). Шаг 3 удаляет колонку, и тип у них будет потерян.
--    Чтобы вернуть их в списки, раскомментируйте INSERT до шага 3.
-- =====================================================================================
-- INSERT INTO option_types (option_id, option_type)
-- SELECT o.id, o.option_type FROM options o
-- WHERE o.option_type IS NOT NULL
--   AND NOT EXISTS (SELECT 1 FROM option_types t WHERE t.option_id = o.id);

-- Что потеряется без INSERT (посмотреть до шага 3):
SET @sql = (SELECT IF(COUNT(*) > 0, 'SELECT o.id, o.english_name, o.option_type FROM options o WHERE o.option_type IS NOT NULL AND NOT EXISTS (SELECT 1 FROM option_types t WHERE t.option_id = o.id AND t.option_type = o.option_type)', 'SELECT ''options.option_type: колонки уже нет'' AS info')
    FROM information_schema.columns WHERE table_schema = DATABASE()
      AND table_name = 'options' AND column_name = 'option_type');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================
-- 1. Таблицы, которых нет в сущностях (строк на 2026-11-18).
--    verification_token здесь НЕТ: это сущность club.dnd5.portal.repository.VerificationToken
--    (лежит вне пакета model, первая инвентаризация её пропустила), таблица используется.
--    skills 1365 + creatures_skills 1388, bonus_saving_throws 938 — старые версии
--      навыков и спасбросков существ (сейчас creature_skills / creature_saving_throws);
--    places 117, background_personalization_feature 139, backgrounds_equipments 59,
--    creatures_immunity_states 12 (сейчас creature_immunity_states), site_references 9,
--    archetypes_spells 2 (сейчас archetype_spell).
-- =====================================================================================
SET @old_fk_checks = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `places`;
DROP TABLE IF EXISTS `skills`;
DROP TABLE IF EXISTS `creatures_skills`;
DROP TABLE IF EXISTS `bonus_saving_throws`;
DROP TABLE IF EXISTS `background_personalization_feature`;
DROP TABLE IF EXISTS `backgrounds_equipments`;
DROP TABLE IF EXISTS `creatures_immunity_states`;
DROP TABLE IF EXISTS `site_references`;
DROP TABLE IF EXISTS `archetypes_spells`;
SET FOREIGN_KEY_CHECKS = @old_fk_checks;

-- =====================================================================================
-- 2. Колонки с данными, которых нет в сущностях.
--    НЕ трогаем колонки, которые читает представление full_text_search (глобальный
--    поиск): classes.alt_name, archetypes.alt_name, hero_class_traits.alt_name /
--    english_name, hero_class_feats.alt_name / english_name. Их нужно перенести в PG.
--
--    classes.bone_hits, classes.skills          — старые текстовые поля, 32 строки
--    creature_actions.img (5455), creature_feats.img (3386), creatures.img (1)
--                                               — иконки Foundry, сущности их не читают
--    spells.material_component                  — старый флаг, все 1006 строк
--    taverna_prefixes.genitive_male / _female   — 184 строки, генератор таверн их не читает
--    archetypes.hero_class_id (+FK)             — дубль class_id; расходится в одной строке
--                                                 (id 97 Ancestors: 3 вместо 9), источник — class_id
-- =====================================================================================
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `classes` DROP COLUMN `bone_hits`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'classes' AND column_name = 'bone_hits');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `classes` DROP COLUMN `skills`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'classes' AND column_name = 'skills');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `creature_actions` DROP COLUMN `img`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'creature_actions' AND column_name = 'img');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `creature_feats` DROP COLUMN `img`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'creature_feats' AND column_name = 'img');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `creatures` DROP COLUMN `img`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'creatures' AND column_name = 'img');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `spells` DROP COLUMN `material_component`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'spells' AND column_name = 'material_component');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `taverna_prefixes` DROP COLUMN `genitive_male`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'taverna_prefixes' AND column_name = 'genitive_male');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `taverna_prefixes` DROP COLUMN `genitive_female`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'taverna_prefixes' AND column_name = 'genitive_female');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `archetypes` DROP FOREIGN KEY `hero_class_id`', 'DO 0') FROM information_schema.table_constraints
    WHERE table_schema = DATABASE() AND table_name = 'archetypes' AND constraint_name = 'hero_class_id'
      AND constraint_type = 'FOREIGN KEY');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `archetypes` DROP COLUMN `hero_class_id`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'archetypes' AND column_name = 'hero_class_id');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================
-- 3. options.option_type — после решения по шагу 0.
-- =====================================================================================
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `options` DROP INDEX `option_type_index`', 'DO 0') FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'options' AND index_name = 'option_type_index');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SET @sql = (SELECT IF(COUNT(*) > 0, 'ALTER TABLE `options` DROP COLUMN `option_type`', 'DO 0') FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'options' AND column_name = 'option_type');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =====================================================================================
-- Итоговая проверка: 0 строк.
-- =====================================================================================
SELECT table_name, column_name FROM information_schema.columns
WHERE table_schema = DATABASE() AND (
    table_name IN ('places', 'skills', 'creatures_skills', 'bonus_saving_throws',
                   'background_personalization_feature', 'backgrounds_equipments',
                   'creatures_immunity_states', 'site_references', 'archetypes_spells')
    OR (table_name = 'classes' AND column_name IN ('bone_hits', 'skills'))
    OR (table_name IN ('creature_actions', 'creature_feats', 'creatures') AND column_name = 'img')
    OR (table_name = 'spells' AND column_name = 'material_component')
    OR (table_name = 'taverna_prefixes' AND column_name LIKE 'genitive\_%')
    OR (table_name = 'archetypes' AND column_name = 'hero_class_id')
    OR (table_name = 'options' AND column_name = 'option_type'));

SET SQL_SAFE_UPDATES = @old_safe_updates;
