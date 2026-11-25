--liquibase formatted sql

--changeset pg-migration:0001-collation-ci
--comment: Сравнение строк как _ci в MySQL: без учёта регистра и диакритики (ё = е, é = e), й остаётся отдельной буквой. Нужен PostgreSQL 18+ (LIKE с недетерминированной collation).
CREATE COLLATION IF NOT EXISTS ci (provider = icu, locale = 'und-u-ks-level1', deterministic = false);
--rollback DROP COLLATION IF EXISTS ci;
