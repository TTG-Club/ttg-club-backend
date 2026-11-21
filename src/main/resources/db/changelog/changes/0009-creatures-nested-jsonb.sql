--liquibase formatted sql

-- Этап 2, существа: вложенные части, которые живут только внутри существа (спасброски, навыки,
-- умения, заклинательство), — из отдельных таблиц в jsonb-массивы объектов в creatures.
-- Expand: новые колонки + перенос данных в порядке id (как их отдавал @OrderBy("id")).
-- Имена полей — как у полей Java-классов (SavingThrow, Skill, CreatureFeat, Spellcater).
-- Строки без существа (creature_id IS NULL — хвосты старых правок) не переносятся.
-- Старые таблицы удаляет 0010.

--changeset pg-migration:0009-creatures-saving_throws-jsonb
ALTER TABLE creatures ADD COLUMN saving_throws jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET saving_throws = COALESCE((
    SELECT jsonb_agg(jsonb_strip_nulls(jsonb_build_object(
               'ability', t.ability, 'bonus', t.bonus, 'additionalBonus', t.additional_bonus)) ORDER BY t.id)
    FROM creature_saving_throws t WHERE t.creature_id = c.id), '[]'::jsonb);
--rollback ALTER TABLE creatures DROP COLUMN saving_throws;

--changeset pg-migration:0009-creatures-skills-jsonb
ALTER TABLE creatures ADD COLUMN skills jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET skills = COALESCE((
    SELECT jsonb_agg(jsonb_strip_nulls(jsonb_build_object(
               'type', t.type, 'bonus', t.bonus, 'additionalBonus', t.additional_bonus)) ORDER BY t.id)
    FROM creature_skills t WHERE t.creature_id = c.id), '[]'::jsonb);
--rollback ALTER TABLE creatures DROP COLUMN skills;

--changeset pg-migration:0009-creatures-feats-jsonb
ALTER TABLE creatures ADD COLUMN feats jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET feats = COALESCE((
    SELECT jsonb_agg(jsonb_strip_nulls(jsonb_build_object(
               'name', t.name, 'englishName', t.english_name, 'description', t.description,
               'markdown', COALESCE(t.markdown, false))) ORDER BY t.id)
    FROM creature_feats t WHERE t.creature_id = c.id), '[]'::jsonb);
--rollback ALTER TABLE creatures DROP COLUMN feats;

--changeset pg-migration:0009-creatures-spellcasters-jsonb
ALTER TABLE creatures ADD COLUMN spellcasters jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET spellcasters = COALESCE((
    SELECT jsonb_agg(jsonb_strip_nulls(jsonb_build_object(
               'level', t.level, 'dc', t.dc, 'attack', t.attack, 'spellAbility', t.spell_ability,
               'slot1', t.slot1, 'slot2', t.slot2, 'slot3', t.slot3, 'slot4', t.slot4, 'slot5', t.slot5,
               'slot6', t.slot6, 'slot7', t.slot7, 'slot8', t.slot8, 'slot9', t.slot9)) ORDER BY t.id)
    FROM creature_spellcaster t WHERE t.creature_id = c.id), '[]'::jsonb);
--rollback ALTER TABLE creatures DROP COLUMN spellcasters;
