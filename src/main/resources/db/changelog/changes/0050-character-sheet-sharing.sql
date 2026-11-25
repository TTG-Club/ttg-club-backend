--liquibase formatted sql

-- Доступ к листу персонажа по ссылке (как в core-api): share_token — секрет ссылки, null — доступ
-- выключен. character_sheet_saved — чужие листы, сохранённые пользователем по ссылке («Другие листы»).
-- Внешнего ключа на лист нет: после удаления листа или отзыва ссылки запись остаётся и
-- показывается как недоступная, пока пользователь её не уберёт.

--changeset character-sheet:0050-character_sheets-share_token
ALTER TABLE character_sheets ADD COLUMN share_token char(36) collate ci;
--rollback ALTER TABLE character_sheets DROP COLUMN share_token;

--changeset character-sheet:0050-ux_character_sheets_share_token
CREATE UNIQUE INDEX ux_character_sheets_share_token ON character_sheets (share_token);
--rollback DROP INDEX ux_character_sheets_share_token;

--changeset character-sheet:0050-table-character_sheet_saved
CREATE TABLE character_sheet_saved (
    id char(36) collate ci not null,
    user_id bigint not null,
    sheet_id char(36) collate ci not null,
    share_token char(36) collate ci not null,
    name varchar(100) collate ci not null,
    created_at timestamp(6) with time zone not null,
    primary key (id)
);
--rollback DROP TABLE character_sheet_saved;

--changeset character-sheet:0050-fk_character_sheet_saved_user_id
ALTER TABLE character_sheet_saved ADD CONSTRAINT fk_character_sheet_saved_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
--rollback ALTER TABLE character_sheet_saved DROP CONSTRAINT fk_character_sheet_saved_user_id;

--changeset character-sheet:0050-ux_character_sheet_saved_user_sheet
CREATE UNIQUE INDEX ux_character_sheet_saved_user_sheet ON character_sheet_saved (user_id, sheet_id);
--rollback DROP INDEX ux_character_sheet_saved_user_sheet;
