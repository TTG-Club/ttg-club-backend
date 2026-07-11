-- Сид-данные: веса появления рас за столиками таверны в зависимости от местности.
--
-- Таблица taverna_race_habitates (english_name, habitat, chance):
--   * english_name — англ. слаг расы (races.english_name) ЛИБО его часть. Правило
--     применяется ко всем расам, в чьём english_name встречается это значение
--     (напр. «dwarf» ловит «mountain-dwarf», «hill-dwarf»). Из нескольких подходящих
--     правил берётся самое специфичное — с самым длинным ключом (поэтому «half-elf»
--     не попадает под «elf», а «deep-gnome» — под «gnome»).
--   * habitat — местность (значение HabitatType). NULL = базовый вес расы во всех
--     прочих местностях (для рас-эндемиков — их редкий вес «где угодно кроме своей»).
--   * chance — относительный вес выбора расы. Базовый вес для рас без правил (человек,
--     любые несопоставленные) — 3, задан в коде (BASE_RACE_WEIGHT). Здесь: 9 — «часто»,
--     1 — «редко».
--
-- Идемпотентно: таблица целиком принадлежит этому сиду, поэтому перед вставкой чистим.
-- Правьте веса/местности/расы под свой справочник рас.

DELETE FROM taverna_race_habitates;

INSERT INTO taverna_race_habitates (english_name, habitat, chance)
SELECT s.english_name, s.habitat, s.chance
FROM (
    -- Эндемики подземья: часто в подземье, редко где-либо ещё (строка habitat=NULL).
    SELECT 'drow'        AS english_name, 'UNDERGROUND' AS habitat, 9 AS chance
    UNION ALL SELECT 'drow',        NULL,          1
    UNION ALL SELECT 'duergar',     'UNDERGROUND', 9
    UNION ALL SELECT 'duergar',     NULL,          1
    UNION ALL SELECT 'svirfneblin', 'UNDERGROUND', 9
    UNION ALL SELECT 'svirfneblin', NULL,          1
    UNION ALL SELECT 'deep-gnome',  'UNDERGROUND', 9
    UNION ALL SELECT 'deep-gnome',  NULL,          1

    -- Расы со «своими» местностями: часто там, обычный (базовый) вес в остальных.
    UNION ALL SELECT 'dwarf',       'MOUNTAIN',    9
    UNION ALL SELECT 'dwarf',       'HILL',        9
    UNION ALL SELECT 'dwarf',       'UNDERGROUND', 9

    UNION ALL SELECT 'elf',         'FOREST',      9

    UNION ALL SELECT 'gnome',       'FOREST',      9
    UNION ALL SELECT 'gnome',       'HILL',        9

    UNION ALL SELECT 'halfling',    'GRASSLAND',   9
    UNION ALL SELECT 'halfling',    'VILLAGE',     9
    UNION ALL SELECT 'halfling',    'HILL',        9

    UNION ALL SELECT 'orc',         'MOUNTAIN',    9
    UNION ALL SELECT 'orc',         'HILL',        9
    UNION ALL SELECT 'orc',         'GRASSLAND',   9

    UNION ALL SELECT 'goblin',      'FOREST',      9
    UNION ALL SELECT 'goblin',      'HILL',        9
    UNION ALL SELECT 'goblin',      'UNDERGROUND', 9
    UNION ALL SELECT 'hobgoblin',   'FOREST',      9
    UNION ALL SELECT 'hobgoblin',   'HILL',        9
    UNION ALL SELECT 'hobgoblin',   'UNDERGROUND', 9
    UNION ALL SELECT 'bugbear',     'FOREST',      9
    UNION ALL SELECT 'bugbear',     'HILL',        9
    UNION ALL SELECT 'bugbear',     'UNDERGROUND', 9

    UNION ALL SELECT 'kobold',      'UNDERGROUND', 9
    UNION ALL SELECT 'kobold',      'MOUNTAIN',    9

    UNION ALL SELECT 'dragonborn',  'MOUNTAIN',    9
    UNION ALL SELECT 'dragonborn',  'DESERT',      9

    UNION ALL SELECT 'tiefling',    'CITY',        9

    UNION ALL SELECT 'goliath',     'MOUNTAIN',    9
    UNION ALL SELECT 'goliath',     'ARCTIC',      9

    UNION ALL SELECT 'aarakocra',   'MOUNTAIN',    9

    UNION ALL SELECT 'triton',      'COAST',       9
    UNION ALL SELECT 'triton',      'WATERS',      9

    UNION ALL SELECT 'lizardfolk',  'SWAMP',       9

    UNION ALL SELECT 'tortle',      'COAST',       9
    UNION ALL SELECT 'tortle',      'SWAMP',       9

    UNION ALL SELECT 'tabaxi',      'TROPICS',     9
    UNION ALL SELECT 'tabaxi',      'FOREST',      9

    UNION ALL SELECT 'firbolg',     'FOREST',      9

    -- Полукровки — космополиты: базовый вес везде. Ключи длиннее «elf»/«orc», поэтому
    -- перекрывают их и не дают полукровкам наследовать местности эльфов/орков.
    UNION ALL SELECT 'half-elf',    NULL,          3
    UNION ALL SELECT 'half-orc',    NULL,          3
) AS s;
