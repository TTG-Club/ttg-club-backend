--liquibase formatted sql

-- Этап 2, классы и расы: то, что живёт только внутри записи и не правится мастерской по id,
-- — из отдельных таблиц в jsonb. Expand: колонки + перенос в прежнем порядке. Таблицы удаляет 0016.
--   classes.primary_abilities, classes.available_skills — списки enum (по sort_order);
--   classes.level_definitions, archetypes.level_definitions — ячейки заклинаний по уровням
--     (по id), вместе с вложенными trackers;
--   race_features.skills — навыки особенности расы (по sort_order).
-- Остаются таблицами: колонки таблицы класса (feature level definitions — мастерская правит по id),
-- умения классов и архетипов, особенности, имена и бонусы рас, подрасы, языки.

--changeset pg-migration:0015-classes-primary_abilities-jsonb
ALTER TABLE classes ADD COLUMN primary_abilities jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE classes c SET primary_abilities = COALESCE((
    SELECT jsonb_agg(t.ability ORDER BY t.sort_order) FROM class_primary_abilities t WHERE t.class_id = c.id), '[]'::jsonb);
--rollback ALTER TABLE classes DROP COLUMN primary_abilities;

--changeset pg-migration:0015-classes-available_skills-jsonb
ALTER TABLE classes ADD COLUMN available_skills jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE classes c SET available_skills = COALESCE((
    SELECT jsonb_agg(t.skill ORDER BY t.sort_order) FROM class_available_skills t WHERE t.class_id = c.id), '[]'::jsonb);
--rollback ALTER TABLE classes DROP COLUMN available_skills;

--changeset pg-migration:0015-classes-level_definitions-jsonb
ALTER TABLE classes ADD COLUMN level_definitions jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE classes c SET level_definitions = COALESCE((
    SELECT jsonb_agg(jsonb_strip_nulls(jsonb_build_object(
               'level', ld.level, 'slot1', ld.slot1, 'slot2', ld.slot2, 'slot3', ld.slot3, 'slot4', ld.slot4,
               'slot5', ld.slot5, 'slot6', ld.slot6, 'slot7', ld.slot7, 'slot8', ld.slot8, 'slot9', ld.slot9,
               'trackers', COALESCE((SELECT jsonb_agg(jsonb_build_object('name', tr.name, 'value', tr."value",
                                                                         'type', tr.type, 'rest', tr.rest) ORDER BY tr.id)
                                     FROM trackers tr WHERE tr.level_def_id = ld.id), '[]'::jsonb))) ORDER BY ld.id)
    FROM level_definitions ld WHERE ld.hero_class_id = c.id), '[]'::jsonb);
--rollback ALTER TABLE classes DROP COLUMN level_definitions;

--changeset pg-migration:0015-archetypes-level_definitions-jsonb
ALTER TABLE archetypes ADD COLUMN level_definitions jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE archetypes a SET level_definitions = COALESCE((
    SELECT jsonb_agg(jsonb_strip_nulls(jsonb_build_object(
               'level', ld.level, 'slot1', ld.slot1, 'slot2', ld.slot2, 'slot3', ld.slot3, 'slot4', ld.slot4,
               'slot5', ld.slot5, 'slot6', ld.slot6, 'slot7', ld.slot7, 'slot8', ld.slot8, 'slot9', ld.slot9,
               'trackers', COALESCE((SELECT jsonb_agg(jsonb_build_object('name', tr.name, 'value', tr."value",
                                                                         'type', tr.type, 'rest', tr.rest) ORDER BY tr.id)
                                     FROM trackers tr WHERE tr.level_def_id = ld.id), '[]'::jsonb))) ORDER BY ld.id)
    FROM level_definitions ld WHERE ld.archetype_id = a.id), '[]'::jsonb);
--rollback ALTER TABLE archetypes DROP COLUMN level_definitions;

--changeset pg-migration:0015-race_features-skills-jsonb
ALTER TABLE race_features ADD COLUMN skills jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE race_features f SET skills = COALESCE((
    SELECT jsonb_agg(t.skills ORDER BY t.sort_order) FROM race_feature_skill_type t WHERE t.feature_id = f.id), '[]'::jsonb);
--rollback ALTER TABLE race_features DROP COLUMN skills;
