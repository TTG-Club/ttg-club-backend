--liquibase formatted sql

-- Листы персонажей: документ листа целиком в jsonb, имя дублируется колонкой для списка.
-- version — оптимистичная блокировка автосохранения (два открытых окна не затирают друг друга).

--changeset pg-migration:0043-table-character_sheets
CREATE TABLE character_sheets (
    id char(36) collate ci not null,
    user_id bigint not null,
    name varchar(100) collate ci not null,
    data jsonb not null,
    version bigint not null default 0,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id)
);
--rollback DROP TABLE character_sheets;

--changeset pg-migration:0043-fk_character_sheets_user_id
ALTER TABLE character_sheets ADD CONSTRAINT fk_character_sheets_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
--rollback ALTER TABLE character_sheets DROP CONSTRAINT fk_character_sheets_user_id;

--changeset pg-migration:0043-ix_character_sheets_user_id
CREATE INDEX ix_character_sheets_user_id ON character_sheets (user_id);
--rollback DROP INDEX ix_character_sheets_user_id;
