--liquibase formatted sql

-- Этап 2, существа: действия — из creature_actions + creatures_actions в jsonb-массив creatures.actions.
-- У каждого существа своя копия. В старых данных одно действие могли разделять несколько существ
-- (до 19), но мастерская при сохранении всё равно создавала действия заново, так что правка общего
-- действия до других существ не доходила; копии сохраняют это поведение.
-- Порядок — по sort_order связи; тип действия (EnumType.ORDINAL) — в имя константы ActionType.
-- Старые таблицы удаляет 0012.

--changeset pg-migration:0011-creatures-actions-jsonb
ALTER TABLE creatures ADD COLUMN actions jsonb NOT NULL DEFAULT '[]'::jsonb;
UPDATE creatures c SET actions = COALESCE((
    SELECT jsonb_agg(jsonb_strip_nulls(jsonb_build_object(
               'name', a.name, 'englishName', a.english_name, 'description', a.description,
               'actionType', (ARRAY['ACTION','REACTION','LEGENDARY','BONUS','LAIR','MYSTICAL'])[a.action_type + 1],
               'markdown', COALESCE(a.markdown, false))) ORDER BY ca.sort_order)
    FROM creatures_actions ca JOIN creature_actions a ON a.id = ca.actions_id
    WHERE ca.creature_id = c.id), '[]'::jsonb);
-- фильтр «легендарные действия»: actions @> '[{"actionType":"LEGENDARY"}]'
CREATE INDEX ix_creatures_actions_gin ON creatures USING gin (actions jsonb_path_ops);
--rollback DROP INDEX ix_creatures_actions_gin; ALTER TABLE creatures DROP COLUMN actions;
