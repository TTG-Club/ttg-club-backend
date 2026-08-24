START TRANSACTION;

SELECT
    archetype.id,
    hero_class.name AS class_name,
    archetype.name AS archetype_name,
    archetype.spell_level_type
FROM archetypes archetype
JOIN classes hero_class ON hero_class.id = archetype.class_id
WHERE LOWER(hero_class.english_name) = 'warlock'
ORDER BY archetype.id;

UPDATE archetypes archetype
JOIN classes hero_class ON hero_class.id = archetype.class_id
SET archetype.spell_level_type = 'SPELL_LEVEL'
WHERE LOWER(hero_class.english_name) = 'warlock'
  AND archetype.spell_level_type <> 'SPELL_LEVEL';

SELECT ROW_COUNT() AS updated_rows;

SELECT
    archetype.id,
    hero_class.name AS class_name,
    archetype.name AS archetype_name,
    archetype.spell_level_type
FROM archetypes archetype
JOIN classes hero_class ON hero_class.id = archetype.class_id
WHERE LOWER(hero_class.english_name) = 'warlock'
ORDER BY archetype.id;

-- После проверки updated_rows и контрольного SELECT выполнить вручную:
-- COMMIT;
-- Для отмены изменений выполнить вручную:
-- ROLLBACK;
