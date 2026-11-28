--liquibase formatted sql

-- Совместная правка листа персонажа (как в core-api).
-- character_sheet_editors — право другого пользователя править лист: запрос того, кто сохранил лист
-- по ссылке (PENDING), и разрешение владельца (APPROVED). Отказ и отзыв запись удаляют.
-- character_sheet_presence — отметки «лист сейчас открыт у пользователя» для мягкой блокировки:
-- отдельная таблица, чтобы частые отметки не поднимали версию листа.
-- Внешнего ключа на лист нет, как и у character_sheet_saved: записи чистятся явно — при отзыве
-- ссылки, удалении листа и вытеснении его из истории.

--changeset character-sheet:0053-table-character_sheet_editors
CREATE TABLE character_sheet_editors (
    id char(36) collate ci not null,
    sheet_id char(36) collate ci not null,
    user_id bigint not null,
    status varchar(16) not null,
    created_at timestamp(6) with time zone not null,
    decided_at timestamp(6) with time zone,
    primary key (id)
);
--rollback DROP TABLE character_sheet_editors;

--changeset character-sheet:0053-fk_character_sheet_editors_user_id
ALTER TABLE character_sheet_editors ADD CONSTRAINT fk_character_sheet_editors_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
--rollback ALTER TABLE character_sheet_editors DROP CONSTRAINT fk_character_sheet_editors_user_id;

--changeset character-sheet:0053-ux_character_sheet_editors_sheet_user
CREATE UNIQUE INDEX ux_character_sheet_editors_sheet_user ON character_sheet_editors (sheet_id, user_id);
--rollback DROP INDEX ux_character_sheet_editors_sheet_user;

--changeset character-sheet:0053-ix_character_sheet_editors_user_id
CREATE INDEX ix_character_sheet_editors_user_id ON character_sheet_editors (user_id);
--rollback DROP INDEX ix_character_sheet_editors_user_id;

--changeset character-sheet:0053-table-character_sheet_presence
CREATE TABLE character_sheet_presence (
    id char(36) collate ci not null,
    sheet_id char(36) collate ci not null,
    user_id bigint not null,
    seen_at timestamp(6) with time zone not null,
    primary key (id)
);
--rollback DROP TABLE character_sheet_presence;

--changeset character-sheet:0053-fk_character_sheet_presence_user_id
ALTER TABLE character_sheet_presence ADD CONSTRAINT fk_character_sheet_presence_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
--rollback ALTER TABLE character_sheet_presence DROP CONSTRAINT fk_character_sheet_presence_user_id;

--changeset character-sheet:0053-ux_character_sheet_presence_sheet_user
CREATE UNIQUE INDEX ux_character_sheet_presence_sheet_user ON character_sheet_presence (sheet_id, user_id);
--rollback DROP INDEX ux_character_sheet_presence_sheet_user;
