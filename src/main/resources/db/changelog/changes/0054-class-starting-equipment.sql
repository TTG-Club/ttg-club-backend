--liquibase formatted sql

-- Стартовое снаряжение класса структурой (как startingEquipment в core-api, но с вариантами
-- внутри строки): массив строк, у строки — варианты «а) … или б) …», у варианта — предметы
-- (запись справочника, свой предмет или выбор из категории) и монеты. NULL — не заполнено,
-- тогда показывается текст equipment, который остаётся без изменений.

--changeset pg-migration:0054-classes-starting_equipment
ALTER TABLE classes ADD COLUMN starting_equipment jsonb;
--rollback ALTER TABLE classes DROP COLUMN starting_equipment;
