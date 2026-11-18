--liquibase formatted sql

-- Этап 2, существа: списки enum из отдельных таблиц -> jsonb-массивы имён в creatures.
-- Expand: новые колонки + перенос данных в порядке sort_order. Старые таблицы удаляет 0008.
-- Значения, хранившиеся числом (EnumType.ORDINAL), переводятся в имена по порядку констант enum.

--changeset pg-migration:0007-creatures-armor_types-jsonb
ALTER TABLE creatures ADD COLUMN armor_types jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET armor_types = COALESCE(
    (SELECT jsonb_agg(t.armor_types ORDER BY t.sort_order) FROM creature_armor_types t WHERE t.creature_id = c.id),
    '[]'::jsonb);
--rollback ALTER TABLE creatures DROP COLUMN armor_types;

--changeset pg-migration:0007-creatures-immunity_states-jsonb
ALTER TABLE creatures ADD COLUMN immunity_states jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET immunity_states = COALESCE(
    (SELECT jsonb_agg((ARRAY['BLINDED','CHARMED','DYING','EVASIONS','DEAFENED','EXHAUSTION','FRIGHTENED','GRAPPLED','INCAPACITATED','INVISIBLE','PARALYZED','PETRIFIED','POISONED','PRONE','RESTRAINED','STUNNED','UNCONSCIOUS'])[t.immunity_states + 1] ORDER BY t.sort_order) FROM creature_immunity_states t WHERE t.creature_id = c.id),
    '[]'::jsonb);
CREATE INDEX ix_creatures_immunity_states_gin ON creatures USING gin (immunity_states jsonb_path_ops);
--rollback DROP INDEX ix_creatures_immunity_states_gin; ALTER TABLE creatures DROP COLUMN immunity_states;

--changeset pg-migration:0007-creatures-immunity_damages-jsonb
ALTER TABLE creatures ADD COLUMN immunity_damages jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET immunity_damages = COALESCE(
    (SELECT jsonb_agg((ARRAY['FAIR','COLD','LIGHTNING','POISON','ACID','SOUND','NECTOTIC','PSYCHIC','BLUDGEONING','PIERCING','SLASHING','PHYSICAL','NO_NOSILVER','NO_DAMAGE','RADIANT','NO_ADMANTIT','PHYSICAL_MAGIC','PIERCING_GOOD','MAGIC','DARK','FORCE','METAL_WEAPON','VORPAL_SWORD','NECROTIC','THUNDER'])[t.immunity_damages + 1] ORDER BY t.sort_order) FROM creature_immunity_damages t WHERE t.creature_id = c.id),
    '[]'::jsonb);
CREATE INDEX ix_creatures_immunity_damages_gin ON creatures USING gin (immunity_damages jsonb_path_ops);
--rollback DROP INDEX ix_creatures_immunity_damages_gin; ALTER TABLE creatures DROP COLUMN immunity_damages;

--changeset pg-migration:0007-creatures-resistance_damages-jsonb
ALTER TABLE creatures ADD COLUMN resistance_damages jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET resistance_damages = COALESCE(
    (SELECT jsonb_agg((ARRAY['FAIR','COLD','LIGHTNING','POISON','ACID','SOUND','NECTOTIC','PSYCHIC','BLUDGEONING','PIERCING','SLASHING','PHYSICAL','NO_NOSILVER','NO_DAMAGE','RADIANT','NO_ADMANTIT','PHYSICAL_MAGIC','PIERCING_GOOD','MAGIC','DARK','FORCE','METAL_WEAPON','VORPAL_SWORD','NECROTIC','THUNDER'])[t.resistance_damages + 1] ORDER BY t.sort_order) FROM creature_resistance_damages t WHERE t.creature_id = c.id),
    '[]'::jsonb);
CREATE INDEX ix_creatures_resistance_damages_gin ON creatures USING gin (resistance_damages jsonb_path_ops);
--rollback DROP INDEX ix_creatures_resistance_damages_gin; ALTER TABLE creatures DROP COLUMN resistance_damages;

--changeset pg-migration:0007-creatures-vulnerability_damages-jsonb
ALTER TABLE creatures ADD COLUMN vulnerability_damages jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET vulnerability_damages = COALESCE(
    (SELECT jsonb_agg((ARRAY['FAIR','COLD','LIGHTNING','POISON','ACID','SOUND','NECTOTIC','PSYCHIC','BLUDGEONING','PIERCING','SLASHING','PHYSICAL','NO_NOSILVER','NO_DAMAGE','RADIANT','NO_ADMANTIT','PHYSICAL_MAGIC','PIERCING_GOOD','MAGIC','DARK','FORCE','METAL_WEAPON','VORPAL_SWORD','NECROTIC','THUNDER'])[t.vulnerability_damages + 1] ORDER BY t.sort_order) FROM creature_vulnerability_damages t WHERE t.creature_id = c.id),
    '[]'::jsonb);
CREATE INDEX ix_creatures_vulnerability_damages_gin ON creatures USING gin (vulnerability_damages jsonb_path_ops);
--rollback DROP INDEX ix_creatures_vulnerability_damages_gin; ALTER TABLE creatures DROP COLUMN vulnerability_damages;

--changeset pg-migration:0007-creatures-habitates-jsonb
ALTER TABLE creatures ADD COLUMN habitates jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET habitates = COALESCE(
    (SELECT jsonb_agg(t.habitates ORDER BY t.sort_order) FROM creature_habitates t WHERE t.creature_id = c.id),
    '[]'::jsonb);
CREATE INDEX ix_creatures_habitates_gin ON creatures USING gin (habitates jsonb_path_ops);
--rollback DROP INDEX ix_creatures_habitates_gin; ALTER TABLE creatures DROP COLUMN habitates;
