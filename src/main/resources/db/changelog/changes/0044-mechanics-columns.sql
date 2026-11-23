--liquibase formatted sql

-- Механика для листа персонажа (порт модели core-api): типизированный блок mechanics и список
-- active_effects (Active Effects VTTG) у классов, архетипов, их умений, особенностей классов (options),
-- рас и разновидностей, умений рас, предысторий, черт; у магических предметов — только mechanics.
-- Колонки необязательны: NULL — механика не заполнена. Заполнение из уже структурированных
-- колонок — 0045. Модель: club.dnd5.portal.model.mechanics, club.dnd5.portal.model.effect.

--changeset pg-migration:0044-classes-mechanics
alter table classes add column mechanics jsonb;
--rollback alter table classes drop column mechanics;

--changeset pg-migration:0044-classes-active_effects
alter table classes add column active_effects jsonb;
--rollback alter table classes drop column active_effects;

--changeset pg-migration:0044-archetypes-mechanics
alter table archetypes add column mechanics jsonb;
--rollback alter table archetypes drop column mechanics;

--changeset pg-migration:0044-archetypes-active_effects
alter table archetypes add column active_effects jsonb;
--rollback alter table archetypes drop column active_effects;

--changeset pg-migration:0044-hero_class_traits-mechanics
alter table hero_class_traits add column mechanics jsonb;
--rollback alter table hero_class_traits drop column mechanics;

--changeset pg-migration:0044-hero_class_traits-active_effects
alter table hero_class_traits add column active_effects jsonb;
--rollback alter table hero_class_traits drop column active_effects;

--changeset pg-migration:0044-hero_class_feats-mechanics
alter table hero_class_feats add column mechanics jsonb;
--rollback alter table hero_class_feats drop column mechanics;

--changeset pg-migration:0044-hero_class_feats-active_effects
alter table hero_class_feats add column active_effects jsonb;
--rollback alter table hero_class_feats drop column active_effects;

--changeset pg-migration:0044-options-mechanics
alter table options add column mechanics jsonb;
--rollback alter table options drop column mechanics;

--changeset pg-migration:0044-options-active_effects
alter table options add column active_effects jsonb;
--rollback alter table options drop column active_effects;

--changeset pg-migration:0044-races-mechanics
alter table races add column mechanics jsonb;
--rollback alter table races drop column mechanics;

--changeset pg-migration:0044-races-active_effects
alter table races add column active_effects jsonb;
--rollback alter table races drop column active_effects;

--changeset pg-migration:0044-race_features-mechanics
alter table race_features add column mechanics jsonb;
--rollback alter table race_features drop column mechanics;

--changeset pg-migration:0044-race_features-active_effects
alter table race_features add column active_effects jsonb;
--rollback alter table race_features drop column active_effects;

--changeset pg-migration:0044-backgrounds-mechanics
alter table backgrounds add column mechanics jsonb;
--rollback alter table backgrounds drop column mechanics;

--changeset pg-migration:0044-backgrounds-active_effects
alter table backgrounds add column active_effects jsonb;
--rollback alter table backgrounds drop column active_effects;

--changeset pg-migration:0044-traits-mechanics
alter table traits add column mechanics jsonb;
--rollback alter table traits drop column mechanics;

--changeset pg-migration:0044-traits-active_effects
alter table traits add column active_effects jsonb;
--rollback alter table traits drop column active_effects;

--changeset pg-migration:0044-artifactes-mechanics
alter table artifactes add column mechanics jsonb;
--rollback alter table artifactes drop column mechanics;
