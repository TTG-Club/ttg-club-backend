--liquibase formatted sql

--changeset pg-migration:0001-collation-ci
--comment: Регистронезависимое сравнение строк, как _ci в MySQL. Нужен PostgreSQL 18+ (LIKE с недетерминированной collation).
CREATE COLLATION IF NOT EXISTS ci (provider = icu, locale = 'und-u-ks-level2', deterministic = false);
--rollback DROP COLLATION IF EXISTS ci;
