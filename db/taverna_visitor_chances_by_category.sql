-- Шансы посетителей по УРОВНЮ ОБСЛУЖИВАНИЯ (категории заведения) — по трём таблицам
-- из референса: «Посетители дешёвого/обычного/дорогого заведения».
--
-- В отличие от taverna_visitor_chances_new_types.sql (там шансы привязаны к ТИПУ
-- заведения), здесь строки привязаны к КАТЕГОРИИ и не зависят от типа:
-- taverna_type = NULL. Метод visitorWeight трактует null-тип как «подходит к любому
-- типу», а null-категорию — как «к любой категории», поэтому эти строки складываются
-- с типовыми: итоговый вес посетителя = (типовые шансы) + (шансы его класса).
--
-- Веса = число граней к20 из референса, умноженное на 4. ×4 нужно, чтобы вложенный
-- к4 в слоте «Важный МП» (обычное заведение) дал целые числа; заодно класс заметно
-- доминирует над типовыми весами (обычно 3–8), т.е. состав гостей определяется
-- прежде всего уровнем обслуживания.
--
-- Соответствие имён референс → taverna_visitors:
--   «Городской страж»       → Стражник (уже есть)
--   «МП искатель приключений»→ Искатель приключений (уже есть)
--   «Обыватель», «Военная элита» — в справочнике отсутствуют, добавляются ниже.
--   «Другое» (слот 20) — не отдельный посетитель, пропущено.
--
-- ВАЖНО: требуется, чтобы столбец taverna_visitor_chances.taverna_type допускал NULL
-- (в сущности VisitorChance поле nullable). Если в схеме стоит NOT NULL — снимите его
-- (ALTER TABLE ... MODIFY taverna_type VARCHAR(...) NULL) либо размножьте строки по типам.

-- 1. Недостающие посетители из референса (идемпотентно — только если ещё нет).
INSERT INTO taverna_visitors (name)
SELECT s.name
FROM (
    SELECT 'Обыватель'     AS name
    UNION ALL SELECT 'Военная элита'
) AS s
WHERE NOT EXISTS (
    SELECT 1 FROM taverna_visitors v WHERE v.name = s.name
);

-- 2. Чистим прежние строки этого сида, чтобы повторный прогон не плодил дубли.
DELETE FROM taverna_visitor_chances
WHERE taverna_type IS NULL
  AND taverna_category IN ('CHEAP', 'ORDINARY', 'EXPENSIVE', 'ELITE');

-- 3. Шансы по классам заведения. Посетители берутся по ИМЕНИ, поэтому строки с
--    несуществующими именами просто не вставятся.
INSERT INTO taverna_visitor_chances (taverna_type, taverna_category, chance, visitor_id)
SELECT s.taverna_type, s.taverna_category, s.chance, v.id
FROM taverna_visitors v
JOIN (
    -- Дешёвое заведение (к20)
    SELECT NULL AS taverna_type, 'CHEAP' AS taverna_category, 8  AS chance, 'Стражник'             AS name -- 1–2  Городской страж
    UNION ALL SELECT NULL, 'CHEAP', 16, 'Нищий'                 -- 3–6
    UNION ALL SELECT NULL, 'CHEAP', 4,  'Бард'                  -- 7
    UNION ALL SELECT NULL, 'CHEAP', 20, 'Подозрительный тип'    -- 8–12
    UNION ALL SELECT NULL, 'CHEAP', 24, 'Обыватель'             -- 13–18
    UNION ALL SELECT NULL, 'CHEAP', 8,  'Искатель приключений'  -- 19–20

    -- Обычное заведение (к20; слот 20 — «Важный МП», вложенный к4)
    UNION ALL SELECT NULL, 'ORDINARY', 8,  'Подозрительный тип'   -- 1–2
    UNION ALL SELECT NULL, 'ORDINARY', 16, 'Торговец'             -- 3–6
    UNION ALL SELECT NULL, 'ORDINARY', 4,  'Бард'                 -- 7
    UNION ALL SELECT NULL, 'ORDINARY', 36, 'Обыватель'            -- 8–16
    UNION ALL SELECT NULL, 'ORDINARY', 8,  'Искатель приключений' -- 17–18
    UNION ALL SELECT NULL, 'ORDINARY', 4,  'Стражник'             -- 19  Городской страж
    UNION ALL SELECT NULL, 'ORDINARY', 1,  'Жрец'                 -- 20 → к4=1
    UNION ALL SELECT NULL, 'ORDINARY', 1,  'Военная элита'        -- 20 → к4=2
    UNION ALL SELECT NULL, 'ORDINARY', 1,  'Дворянин'             -- 20 → к4=3

    -- Дорогое заведение (к20)
    UNION ALL SELECT NULL, 'EXPENSIVE', 4,  'Подозрительный тип'   -- 1
    UNION ALL SELECT NULL, 'EXPENSIVE', 20, 'Торговец'             -- 2–6
    UNION ALL SELECT NULL, 'EXPENSIVE', 4,  'Бард'                 -- 7
    UNION ALL SELECT NULL, 'EXPENSIVE', 32, 'Дворянин'            -- 8–15
    UNION ALL SELECT NULL, 'EXPENSIVE', 4,  'Военная элита'        -- 16
    UNION ALL SELECT NULL, 'EXPENSIVE', 4,  'Жрец'                 -- 17
    UNION ALL SELECT NULL, 'EXPENSIVE', 8,  'Искатель приключений' -- 18–19

    -- Элитное заведение: в референсе отдельной таблицы нет — переиспользуем
    -- распределение «дорогого» (верхняя ступень), чтобы уровень ELITE тоже влиял.
    UNION ALL SELECT NULL, 'ELITE', 4,  'Подозрительный тип'
    UNION ALL SELECT NULL, 'ELITE', 20, 'Торговец'
    UNION ALL SELECT NULL, 'ELITE', 4,  'Бард'
    UNION ALL SELECT NULL, 'ELITE', 32, 'Дворянин'
    UNION ALL SELECT NULL, 'ELITE', 4,  'Военная элита'
    UNION ALL SELECT NULL, 'ELITE', 4,  'Жрец'
    UNION ALL SELECT NULL, 'ELITE', 8,  'Искатель приключений'
) AS s ON v.name = s.name;
