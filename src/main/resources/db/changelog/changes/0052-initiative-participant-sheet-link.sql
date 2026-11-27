--liquibase formatted sql

-- Игрок трекера инициативы, собранный из листа персонажа (как в core-api): sheet_link хранит
-- привязку к листу — id листа, источник (свой или сохранённый чужой), токен ссылки и id
-- сохранённой записи. По ней трекер пишет хиты из боя обратно в лист. NULL — игрок заведён вручную.

--changeset initiative:0052-initiative_participant-sheet_link
ALTER TABLE initiative_participant ADD COLUMN sheet_link jsonb;
--rollback ALTER TABLE initiative_participant DROP COLUMN sheet_link;
