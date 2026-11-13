-- Справочник типов особенностей классов (боевые стили, воззвания, приёмы и т.п.).
-- Раньше типы были зашиты в enum Option.OptionType; теперь новый класс со своими особенностями
-- добавляется одной строкой в эту таблицу, без изменения кода.
--
-- Коды совпадают с прежними значениями enum, поэтому существующие данные в
-- options.option_type, option_types.option_type, classes.option_type и archetypes.option_type не меняются.
--
-- Применять ДО выкладки бэкенда с этим изменением (иначе Hibernate создаст пустую таблицу
-- и вкладки особенностей у классов пропадут до заполнения). Скрипт можно запускать повторно.

CREATE TABLE IF NOT EXISTS `option_type_definitions` (
  `code` varchar(64) NOT NULL,
  `name` varchar(255) NOT NULL,
  `class_name` varchar(255) NOT NULL,
  `archetype_name` varchar(255) DEFAULT NULL,
  `homebrew` bit(1) NOT NULL DEFAULT b'0',
  `feature_stem` varchar(255) DEFAULT NULL,
  `ordering` int DEFAULT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- name          — полное название; часть до двоеточия показывается как заголовок вкладки класса
-- class_name    — английское название класса (classes.english_name)
-- archetype_name— фрагмент URL архетипа, если список относится к архетипу
-- feature_stem  — корень названия умения класса, к которому список прикрепляется при экспорте в VTTG
INSERT INTO `option_type_definitions` (`code`, `name`, `class_name`, `archetype_name`, `homebrew`, `feature_stem`, `ordering`) VALUES
('WILD_SHAPE',                 'Формы Дикого Облика: Друид',                   'Druid',               NULL,            b'0', NULL,           10),
('ARCANE_SHOT',                'Магические выстрелы: Воин Мистический Лучник', 'Fighter',             'Arcane_Archer', b'0', 'выстрел',      20),
('METAMAGIC',                  'Метамагия: Чародей',                           'Sorcerer',            NULL,            b'0', 'метамаги',     30),
('ELDRITCH_INVOCATION',        'Воззвания: Колдун',                            'Warlock',             NULL,            b'0', 'воззвани',     40),
('BONE',                       'Договоры: Колдун',                             'Warlock',             NULL,            b'0', 'договор',      50),
('ELEMENTAL_DISCIPLINE',       'Стихийные практики: Монах Пути четырех стихий', 'Monk',               'Four_Elements', b'0', 'стихи',        60),
('ARTIFICER_INFUSION',         'Инфузии: Изобретатель',                        'Artificer',           NULL,            b'0', 'инфузи',       70),
('RUNE',                       'Руны: Рунический рыцарь',                      'Fighter',             'Rune',          b'0', 'рун',          80),
('MANEUVER',                   'Маневры: Воин Мастер боевых искуств',          'Fighter',             'Battle_Master', b'0', 'превосходств', 90),
('FIGHTING_STYLE',             'Боевые стили: Воин',                           'Fighter',             NULL,            b'0', 'боевой стиль', 100),
('FIGHTING_STYLE_RANGER',      'Боевые стили: Следопыт',                       'Ranger',              NULL,            b'0', 'боевой стиль', 110),
('FIGHTING_STYLE_PALADIN',     'Боевые стили: Паладин',                        'Paladin',             NULL,            b'0', 'боевой стиль', 120),
('FIGHTING_STYLE_BARD',        'Боевые стили: Бард Колллегии Мечей',           'Bard',                'Swords',        b'0', 'боевой стиль', 130),
('FIGHTING_STYLE_BLOODHANTER', 'Боевые стили: Кровавый охотник',               'Blood Hunter',        NULL,            b'1', NULL,           140),
('BLOOD_CURSE',                'Проклятья крови: Кровавый охотник',            'Blood Hunter',        NULL,            b'1', NULL,           150),
('MUTAGEN',                    'Мутагены: Кровавый охотник Ордена мутантов',   'Blood Hunter',        NULL,            b'1', NULL,           160),
('PHILOSOPHICAL_SCHOOL',       'Философские школы: Волшебник Философ Академии', 'Wizard',             NULL,            b'1', NULL,           170),
('BOMB_FORMULA',               'Формулы бомб',                                 'Alchemist',           NULL,            b'1', NULL,           180),
('DISCOVERIES',                'Открытия',                                     'Alchemist',           NULL,            b'1', NULL,           190),
('TRANSPLANTS',                'Трансплантаты',                                'Alchemist',           NULL,            b'1', NULL,           200),
('BONUS_DISCIPLINES',          'Псионические дисциплины: Мистик',              'Mystic',              NULL,            b'0', NULL,           210),
('PSIONIC_TALANT',             'Псионические таланты: Мистик',                 'Mystic',              NULL,            b'0', NULL,           220),
('ACADEMIC_DISCIPLINES',       'Научные исследования: Савант',                 'Savant',              NULL,            b'1', NULL,           230),
('TOTEMS',                     'Тотемы: Шаман',                                'Shaman',              NULL,            b'1', NULL,           240),
('FIGHTING_STYLE_ALT_FIGHTER', 'Боевые стили: Альтернативный воин',            'Alternate Fighter',   NULL,            b'1', 'боевой стиль', 250),
('MARTIAL_EXPLOITS',           'Боевые приёмы: Альтернативный воин',           'Alternate Fighter',   NULL,            b'1', NULL,           260),
('SIGNATURE_TECHNIQUES',       'Фирменные техники: Альтернативный монах',      'Alternate Monk',      NULL,            b'1', NULL,           270),
('SAVAGE_EXPLOITS',            'Дикие приёмы: Альтернативный варвар',          'Alternate Barbarian', NULL,            b'1', NULL,           280),
('DEGREE_EXPLOITS',            'Коварные приёмы: Альтернативный плут',         'Alternate Rogue',     NULL,            b'1', NULL,           290),
('FIGHTING_STYLE_ALT_PALADIN', 'Боевые стили: Альтернативный паладин',         'Alternate Paladin',   NULL,            b'1', 'боевой стиль', 300)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`), `class_name` = VALUES(`class_name`), `archetype_name` = VALUES(`archetype_name`),
  `homebrew` = VALUES(`homebrew`), `feature_stem` = VALUES(`feature_stem`), `ordering` = VALUES(`ordering`);

-- Контроль: коды, которые встречаются в данных, но отсутствуют в справочнике (должно вернуть 0 строк).
SELECT 'options' AS src, option_type AS code FROM options WHERE option_type IS NOT NULL AND option_type NOT IN (SELECT code FROM option_type_definitions)
UNION SELECT 'option_types', option_type FROM option_types WHERE option_type NOT IN (SELECT code FROM option_type_definitions)
UNION SELECT 'classes', option_type FROM classes WHERE option_type IS NOT NULL AND option_type <> '' AND option_type NOT IN (SELECT code FROM option_type_definitions)
UNION SELECT 'archetypes', option_type FROM archetypes WHERE option_type IS NOT NULL AND option_type <> '' AND option_type NOT IN (SELECT code FROM option_type_definitions);
