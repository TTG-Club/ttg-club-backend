-- Заготовка сид-данных: шансы посетителей для новых типов заведений
-- (CAFE, RESTAURANT, GAMBLING_DEN).
--
-- chance — относительный вес выбора посетителя для данного типа (как у BEER/INN/HOTEL).
-- Посетители берутся по ИМЕНИ (taverna_visitors.name), поэтому вставка не зависит от их id;
-- строки с несуществующими именами просто не вставятся. Поправьте имена/веса/категории
-- под ваш справочник посетителей при необходимости.
--
-- Колонки taverna_visitor_chances: taverna_type, taverna_category, chance, visitor_id.

INSERT INTO taverna_visitor_chances (taverna_type, taverna_category, chance, visitor_id)
SELECT s.taverna_type, s.taverna_category, s.chance, v.id
FROM taverna_visitors v
JOIN (
    -- CAFE — тихая, дневная публика
    SELECT 'CAFE' AS taverna_type, 'ORDINARY' AS taverna_category, 8 AS chance, 'Торговец' AS name
    UNION ALL SELECT 'CAFE', 'ORDINARY', 6, 'Служитель культа'
    UNION ALL SELECT 'CAFE', 'ORDINARY', 5, 'Дворянин'
    UNION ALL SELECT 'CAFE', 'ORDINARY', 4, 'Аристократ'
    UNION ALL SELECT 'CAFE', 'ORDINARY', 3, 'Чужеземец'
    UNION ALL SELECT 'CAFE', 'ORDINARY', 3, 'Фермер'

    -- RESTAURANT — состоятельные гости
    UNION ALL SELECT 'RESTAURANT', 'EXPENSIVE', 8, 'Аристократ'
    UNION ALL SELECT 'RESTAURANT', 'EXPENSIVE', 7, 'Дворянин'
    UNION ALL SELECT 'RESTAURANT', 'EXPENSIVE', 6, 'Торговец'
    UNION ALL SELECT 'RESTAURANT', 'EXPENSIVE', 3, 'Искатель приключений'
    UNION ALL SELECT 'RESTAURANT', 'EXPENSIVE', 2, 'Служитель культа'

    -- GAMBLING_DEN — азарт и тёмные дела
    UNION ALL SELECT 'GAMBLING_DEN', 'ORDINARY', 8, 'Подозрительный тип'
    UNION ALL SELECT 'GAMBLING_DEN', 'ORDINARY', 7, 'Шпион'
    UNION ALL SELECT 'GAMBLING_DEN', 'ORDINARY', 6, 'Искатель приключений'
    UNION ALL SELECT 'GAMBLING_DEN', 'ORDINARY', 5, 'Солдат'
    UNION ALL SELECT 'GAMBLING_DEN', 'ORDINARY', 4, 'Торговец'
    UNION ALL SELECT 'GAMBLING_DEN', 'ORDINARY', 3, 'Нищий'
) AS s ON v.name = s.name;
