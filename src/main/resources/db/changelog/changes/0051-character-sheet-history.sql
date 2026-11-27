--liquibase formatted sql

-- История удалённых листов (как в core-api): удаление мягкое — лист скрывается из активных, документ
-- остаётся, и его можно восстановить. deleted_at — когда удалили: по нему история подрезается до
-- последних N листов (самые старые удаляются физически).

--changeset character-sheet:0051-character_sheets-deleted
ALTER TABLE character_sheets ADD COLUMN deleted_at timestamp(6) with time zone;
--rollback ALTER TABLE character_sheets DROP COLUMN deleted_at;

--changeset character-sheet:0051-ix_character_sheets_user_deleted
CREATE INDEX ix_character_sheets_user_deleted ON character_sheets (user_id, deleted_at);
--rollback DROP INDEX ix_character_sheets_user_deleted;
