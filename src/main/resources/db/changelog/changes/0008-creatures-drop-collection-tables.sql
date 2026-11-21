--liquibase formatted sql

-- Этап 2, существа (contract): старые таблицы коллекций больше не нужны, данные — в jsonb (0007).
-- Rollback восстанавливает таблицы из jsonb.

--changeset pg-migration:0008-drop-creature_armor_types
DROP TABLE creature_armor_types;
--rollback CREATE TABLE creature_armor_types (creature_id integer NOT NULL REFERENCES creatures (id), armor_types varchar(255) COLLATE ci NOT NULL, sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (creature_id, sort_order));
--rollback INSERT INTO creature_armor_types (creature_id, armor_types, sort_order) SELECT c.id, x.v, x.n - 1 FROM creatures c CROSS JOIN LATERAL jsonb_array_elements_text(c.armor_types) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0008-drop-creature_immunity_states
DROP TABLE creature_immunity_states;
--rollback CREATE TABLE creature_immunity_states (creature_id integer NOT NULL REFERENCES creatures (id), immunity_states smallint NOT NULL CHECK (immunity_states BETWEEN 0 AND 16), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (creature_id, sort_order));
--rollback INSERT INTO creature_immunity_states (creature_id, immunity_states, sort_order) SELECT c.id, array_position(ARRAY['BLINDED','CHARMED','DYING','EVASIONS','DEAFENED','EXHAUSTION','FRIGHTENED','GRAPPLED','INCAPACITATED','INVISIBLE','PARALYZED','PETRIFIED','POISONED','PRONE','RESTRAINED','STUNNED','UNCONSCIOUS'], x.v) - 1, x.n - 1 FROM creatures c CROSS JOIN LATERAL jsonb_array_elements_text(c.immunity_states) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0008-drop-creature_immunity_damages
DROP TABLE creature_immunity_damages;
--rollback CREATE TABLE creature_immunity_damages (creature_id integer NOT NULL REFERENCES creatures (id), immunity_damages smallint NOT NULL CHECK (immunity_damages BETWEEN 0 AND 24), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (creature_id, sort_order));
--rollback INSERT INTO creature_immunity_damages (creature_id, immunity_damages, sort_order) SELECT c.id, array_position(ARRAY['FAIR','COLD','LIGHTNING','POISON','ACID','SOUND','NECTOTIC','PSYCHIC','BLUDGEONING','PIERCING','SLASHING','PHYSICAL','NO_NOSILVER','NO_DAMAGE','RADIANT','NO_ADMANTIT','PHYSICAL_MAGIC','PIERCING_GOOD','MAGIC','DARK','FORCE','METAL_WEAPON','VORPAL_SWORD','NECROTIC','THUNDER'], x.v) - 1, x.n - 1 FROM creatures c CROSS JOIN LATERAL jsonb_array_elements_text(c.immunity_damages) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0008-drop-creature_resistance_damages
DROP TABLE creature_resistance_damages;
--rollback CREATE TABLE creature_resistance_damages (creature_id integer NOT NULL REFERENCES creatures (id), resistance_damages smallint NOT NULL CHECK (resistance_damages BETWEEN 0 AND 24), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (creature_id, sort_order));
--rollback INSERT INTO creature_resistance_damages (creature_id, resistance_damages, sort_order) SELECT c.id, array_position(ARRAY['FAIR','COLD','LIGHTNING','POISON','ACID','SOUND','NECTOTIC','PSYCHIC','BLUDGEONING','PIERCING','SLASHING','PHYSICAL','NO_NOSILVER','NO_DAMAGE','RADIANT','NO_ADMANTIT','PHYSICAL_MAGIC','PIERCING_GOOD','MAGIC','DARK','FORCE','METAL_WEAPON','VORPAL_SWORD','NECROTIC','THUNDER'], x.v) - 1, x.n - 1 FROM creatures c CROSS JOIN LATERAL jsonb_array_elements_text(c.resistance_damages) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0008-drop-creature_vulnerability_damages
DROP TABLE creature_vulnerability_damages;
--rollback CREATE TABLE creature_vulnerability_damages (creature_id integer NOT NULL REFERENCES creatures (id), vulnerability_damages smallint NOT NULL CHECK (vulnerability_damages BETWEEN 0 AND 24), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (creature_id, sort_order));
--rollback INSERT INTO creature_vulnerability_damages (creature_id, vulnerability_damages, sort_order) SELECT c.id, array_position(ARRAY['FAIR','COLD','LIGHTNING','POISON','ACID','SOUND','NECTOTIC','PSYCHIC','BLUDGEONING','PIERCING','SLASHING','PHYSICAL','NO_NOSILVER','NO_DAMAGE','RADIANT','NO_ADMANTIT','PHYSICAL_MAGIC','PIERCING_GOOD','MAGIC','DARK','FORCE','METAL_WEAPON','VORPAL_SWORD','NECROTIC','THUNDER'], x.v) - 1, x.n - 1 FROM creatures c CROSS JOIN LATERAL jsonb_array_elements_text(c.vulnerability_damages) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0008-drop-creature_habitates
DROP TABLE creature_habitates;
--rollback CREATE TABLE creature_habitates (creature_id integer NOT NULL REFERENCES creatures (id), habitates varchar(255) COLLATE ci NOT NULL, sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (creature_id, sort_order));
--rollback INSERT INTO creature_habitates (creature_id, habitates, sort_order) SELECT c.id, x.v, x.n - 1 FROM creatures c CROSS JOIN LATERAL jsonb_array_elements_text(c.habitates) WITH ORDINALITY AS x(v, n);
