--liquibase formatted sql

-- Этап 2, оставшиеся списки enum: из отдельных таблиц в jsonb (expand). Таблицы удаляет 0018.
--   backgrounds.skills, gods.domains, equipments.types, taverna_visitors.habitat_types,
--   traits.abilities, traits.skills — в прежнем порядке (sort_order; у снаряжения порядка не было —
--   по имени типа, как отдавал первичный ключ MySQL). Фильтруемые колонки — с GIN-индексом.
-- user_excluded_sources (настройки пользователя) остаётся таблицей.

--changeset pg-migration:0017-backgrounds-skills-jsonb
ALTER TABLE backgrounds ADD COLUMN skills jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE backgrounds o SET skills = COALESCE((
    SELECT jsonb_agg(t.skills ORDER BY t.sort_order) FROM background_skill_type t WHERE t.background_id = o.id), '[]'::jsonb);
CREATE INDEX ix_backgrounds_skills_gin ON backgrounds USING gin (skills jsonb_path_ops);
--rollback DROP INDEX ix_backgrounds_skills_gin; ALTER TABLE backgrounds DROP COLUMN skills;

--changeset pg-migration:0017-gods-domains-jsonb
ALTER TABLE gods ADD COLUMN domains jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE gods o SET domains = COALESCE((
    SELECT jsonb_agg(t.domains ORDER BY t.sort_order) FROM god_domains t WHERE t.god_id = o.id), '[]'::jsonb);
CREATE INDEX ix_gods_domains_gin ON gods USING gin (domains jsonb_path_ops);
--rollback DROP INDEX ix_gods_domains_gin; ALTER TABLE gods DROP COLUMN domains;

--changeset pg-migration:0017-equipments-types-jsonb
ALTER TABLE equipments ADD COLUMN types jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE equipments o SET types = COALESCE((
    SELECT jsonb_agg(t.type ORDER BY t.type) FROM equipments_types t WHERE t.equipment_id = o.id), '[]'::jsonb);
CREATE INDEX ix_equipments_types_gin ON equipments USING gin (types jsonb_path_ops);
--rollback DROP INDEX ix_equipments_types_gin; ALTER TABLE equipments DROP COLUMN types;

--changeset pg-migration:0017-taverna_visitors-habitat_types-jsonb
ALTER TABLE taverna_visitors ADD COLUMN habitat_types jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE taverna_visitors o SET habitat_types = COALESCE((
    SELECT jsonb_agg(t.habitat_types ORDER BY t.sort_order) FROM taverna_visitor_habitates t WHERE t.visitor_id = o.id), '[]'::jsonb);
--rollback ALTER TABLE taverna_visitors DROP COLUMN habitat_types;

--changeset pg-migration:0017-traits-abilities-jsonb
ALTER TABLE traits ADD COLUMN abilities jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE traits o SET abilities = COALESCE((
    SELECT jsonb_agg(t.ability ORDER BY t.sort_order) FROM trait_abilities t WHERE t.trait_id = o.id), '[]'::jsonb);
CREATE INDEX ix_traits_abilities_gin ON traits USING gin (abilities jsonb_path_ops);
--rollback DROP INDEX ix_traits_abilities_gin; ALTER TABLE traits DROP COLUMN abilities;

--changeset pg-migration:0017-traits-skills-jsonb
ALTER TABLE traits ADD COLUMN skills jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE traits o SET skills = COALESCE((
    SELECT jsonb_agg(t.skill ORDER BY t.sort_order) FROM trait_skills t WHERE t.trait_id = o.id), '[]'::jsonb);
--rollback ALTER TABLE traits DROP COLUMN skills;
