--liquibase formatted sql

-- Этап 2, заклинания: время сотворения, типы урона и лечения, спасброски — из отдельных таблиц
-- в jsonb-массивы в spells. Expand: колонки + перенос в прежнем порядке (время — по id,
-- списки — по sort_order) + GIN-индексы для фильтров. Старые таблицы удаляет 0014.
-- Связь с классами (spells_hero_class) остаётся таблицей: на неё опираются страницы классов.

--changeset pg-migration:0013-spells-times-jsonb
ALTER TABLE spells ADD COLUMN times jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE spells s SET times = COALESCE((
    SELECT jsonb_agg(jsonb_strip_nulls(jsonb_build_object(
               'number', t.number, 'unit', t.unit, 'condition', t."condition")) ORDER BY t.id)
    FROM spells_times t WHERE t.spell_id = s.id), '[]'::jsonb);
CREATE INDEX ix_spells_times_gin ON spells USING gin (times jsonb_path_ops);
--rollback DROP INDEX ix_spells_times_gin; ALTER TABLE spells DROP COLUMN times;

--changeset pg-migration:0013-spells-damage_type-jsonb
ALTER TABLE spells ADD COLUMN damage_type jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE spells s SET damage_type = COALESCE((
    SELECT jsonb_agg(t.damage_type ORDER BY t.sort_order) FROM spell_damage_type t WHERE t.spell_id = s.id), '[]'::jsonb);
CREATE INDEX ix_spells_damage_type_gin ON spells USING gin (damage_type jsonb_path_ops);
--rollback DROP INDEX ix_spells_damage_type_gin; ALTER TABLE spells DROP COLUMN damage_type;

--changeset pg-migration:0013-spells-heal_type-jsonb
ALTER TABLE spells ADD COLUMN heal_type jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE spells s SET heal_type = COALESCE((
    SELECT jsonb_agg(t.heal_type ORDER BY t.sort_order) FROM spell_heal_type t WHERE t.spell_id = s.id), '[]'::jsonb);
CREATE INDEX ix_spells_heal_type_gin ON spells USING gin (heal_type jsonb_path_ops);
--rollback DROP INDEX ix_spells_heal_type_gin; ALTER TABLE spells DROP COLUMN heal_type;

--changeset pg-migration:0013-spells-savingthrows-jsonb
ALTER TABLE spells ADD COLUMN savingthrows jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE spells s SET savingthrows = COALESCE((
    SELECT jsonb_agg(t.ability ORDER BY t.sort_order) FROM spell_savingthrows t WHERE t.spell_id = s.id), '[]'::jsonb);
CREATE INDEX ix_spells_savingthrows_gin ON spells USING gin (savingthrows jsonb_path_ops);
--rollback DROP INDEX ix_spells_savingthrows_gin; ALTER TABLE spells DROP COLUMN savingthrows;
