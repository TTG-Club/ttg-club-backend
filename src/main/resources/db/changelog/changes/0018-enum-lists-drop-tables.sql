--liquibase formatted sql

-- Этап 2, оставшиеся списки enum (contract): таблицы больше не нужны, данные — в jsonb (0017).
-- Rollback восстанавливает таблицы из jsonb.

--changeset pg-migration:0018-drop-background_skill_type
DROP TABLE background_skill_type;
--rollback CREATE TABLE background_skill_type (background_id integer NOT NULL REFERENCES backgrounds (id), skills varchar(255) COLLATE ci NOT NULL CHECK (skills IN ('ATHLETICS','ACROBATICS','SLEIGHT_OF_HAND','STEALTH','ARCANA','HISTORY','INVESTIGATION','NATURE','RELIGION','ANIMAL_HANDLING','INSIGHT','MEDICINE','PERCEPTION','SURVIVAL','DECEPTION','INTIMIDATION','PERFORMANCE','PERSUASION')), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (background_id, sort_order));
--rollback INSERT INTO background_skill_type (background_id, skills, sort_order) SELECT o.id, x.v, x.n - 1 FROM backgrounds o CROSS JOIN LATERAL jsonb_array_elements_text(o.skills) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0018-drop-god_domains
DROP TABLE god_domains;
--rollback CREATE TABLE god_domains (god_id integer NOT NULL REFERENCES gods (id), domains varchar(255) COLLATE ci CHECK (domains IN ('ARCANA','DEATH','FORGE','GRAVE','KNOWLEDGE','LIFE','LIGHT','NATURE','NONE','ORDER','TEMPEST','TRICKERY','WAR','STORM','DECEPTION','REPOSE','UNDEFINE')), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (god_id, sort_order));
--rollback INSERT INTO god_domains (god_id, domains, sort_order) SELECT o.id, x.v, x.n - 1 FROM gods o CROSS JOIN LATERAL jsonb_array_elements_text(o.domains) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0018-drop-equipments_types
DROP TABLE equipments_types;
--rollback CREATE TABLE equipments_types (equipment_id integer NOT NULL REFERENCES equipments (id), type varchar(255) COLLATE ci NOT NULL CHECK (type IN ('ADVENTURING_GEAR','EAT_DRINK','TOOL','SET','CONTAINER','FOCUSING','DRUID_FOCUS','HOLY_SYMBOL','POISON','CLOTHES','GAME_SET','ARTISANS_TOOLS','MUSICAL_INSTRUMENTS','THROWABLE','LIGHT','ARMOR','LIGHT_ARMOR','MEDIUM_ARMOR','HEAVY_ARMOR','WEAPON','SIMPLE_WEAPON','MARTIAL_WEAPON','EXOTIC_WEAPON','MELE_WAPON','RANGE_WAPON','FIRE_WEAPON','AMMUNITION','SHIELD','TRADE_GOOD','MOUNT','VEHICLE_WATER','VEHICLE_LAND','VEHICLE_AIR','VEHICLE_SPELLJAMMER','SIEGE_EQUIPMENT')), PRIMARY KEY (equipment_id, type));
--rollback INSERT INTO equipments_types (equipment_id, type) SELECT o.id, x.v FROM equipments o CROSS JOIN LATERAL jsonb_array_elements_text(o.types) AS x(v);

--changeset pg-migration:0018-drop-taverna_visitor_habitates
DROP TABLE taverna_visitor_habitates;
--rollback CREATE TABLE taverna_visitor_habitates (visitor_id integer NOT NULL REFERENCES taverna_visitors (id), habitat_types varchar(255) COLLATE ci NOT NULL CHECK (habitat_types IN ('ATHLETICS','ACROBATICS','SLEIGHT_OF_HAND','STEALTH','ARCANA','HISTORY','INVESTIGATION','NATURE','RELIGION','ANIMAL_HANDLING','INSIGHT','MEDICINE','PERCEPTION','SURVIVAL','DECEPTION','INTIMIDATION','PERFORMANCE','PERSUASION')), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (visitor_id, sort_order));
--rollback INSERT INTO taverna_visitor_habitates (visitor_id, habitat_types, sort_order) SELECT o.id, x.v, x.n - 1 FROM taverna_visitors o CROSS JOIN LATERAL jsonb_array_elements_text(o.habitat_types) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0018-drop-trait_abilities
DROP TABLE trait_abilities;
--rollback CREATE TABLE trait_abilities (trait_id integer NOT NULL REFERENCES traits (id), ability varchar(255) COLLATE ci NOT NULL CHECK (ability IN ('STRENGTH','DEXTERITY','CONSTITUTION','INTELLIGENCE','WISDOM','CHARISMA','CHOICE','ONE','CHOICE_UNIQUE','CHOICE_DOUBLE')), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (trait_id, sort_order));
--rollback INSERT INTO trait_abilities (trait_id, ability, sort_order) SELECT o.id, x.v, x.n - 1 FROM traits o CROSS JOIN LATERAL jsonb_array_elements_text(o.abilities) WITH ORDINALITY AS x(v, n);

--changeset pg-migration:0018-drop-trait_skills
DROP TABLE trait_skills;
--rollback CREATE TABLE trait_skills (trait_id integer NOT NULL REFERENCES traits (id), skill varchar(255) COLLATE ci NOT NULL CHECK (skill IN ('ATHLETICS','ACROBATICS','SLEIGHT_OF_HAND','STEALTH','ARCANA','HISTORY','INVESTIGATION','NATURE','RELIGION','ANIMAL_HANDLING','INSIGHT','MEDICINE','PERCEPTION','SURVIVAL','DECEPTION','INTIMIDATION','PERFORMANCE','PERSUASION')), sort_order integer NOT NULL CHECK (sort_order >= 0), PRIMARY KEY (trait_id, sort_order));
--rollback INSERT INTO trait_skills (trait_id, skill, sort_order) SELECT o.id, x.v, x.n - 1 FROM traits o CROSS JOIN LATERAL jsonb_array_elements_text(o.skills) WITH ORDINALITY AS x(v, n);
