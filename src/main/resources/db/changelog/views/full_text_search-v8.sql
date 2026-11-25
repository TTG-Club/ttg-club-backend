-- Глобальный поиск, версия 8 (changeset 0032): описание расы — разметка core-api (jsonb), как и описания
-- заклинаний, черт, особенностей, правил, ширмы, предметов, предысторий и богов; отдаются JSON-текстом,
-- анонс из него собирает SearchRepository. Версия 7 — views/full_text_search-v7.sql.
-- Файл не редактировать после применения: следующая правка view — новая версия файла.
CREATE VIEW full_text_search AS
SELECT (spells.name) collate ci AS name,
       (spells.alt_name) collate ci AS alt_name,
       (spells.english_name) collate ci AS english_name,
       ('Заклинания') collate ci AS section,
       (concat('/spells/',replace(lower(spells.english_name),' ','_'))) collate ci AS url,
       (spells.description::text) collate ci AS description,
       (spells.source) collate ci AS source
FROM spells
UNION
SELECT (classes.name) collate ci AS name,
       (classes.alt_name) collate ci AS alt_name,
       (classes.english_name) collate ci AS english_name,
       ('Классы') collate ci AS section,
       (concat('/classes/',replace(lower(classes.english_name),' ','_'))) collate ci AS url,
       (classes.description) collate ci AS description,
       (classes.source) collate ci AS source
FROM classes
UNION
SELECT (hct.name) collate ci AS name,
       (hct.alt_name) collate ci AS alt_name,
       (hct.english_name) collate ci AS english_name,
       (concat('Умение ',c.name_accusative)) collate ci AS section,
       (concat('/classes/',replace(lower(c.english_name),' ','_'),'#c',hct.id)) collate ci AS url,
       (hct.description) collate ci AS description,
       (hct.source) collate ci AS source
FROM (classes c join hero_class_traits hct on((hct.hero_class_id = c.id))) where ((hct.child is null) and (hct.architype = false))
UNION
SELECT (hcf.name) collate ci AS name,
       (hcf.alt_name) collate ci AS alt_name,
       (hcf.english_name) collate ci AS english_name,
       (concat('Умение ',a.genitive_name)) collate ci AS section,
       (concat('/classes/',replace(lower(c.english_name),' ','_'),'/',replace(lower(a.english_name),' ','_'),'#a',hcf.id)) collate ci AS url,
       (hcf.description) collate ci AS description,
       (hcf.source) collate ci AS source
FROM ((classes c join archetypes a on((a.class_id = c.id))) join hero_class_feats hcf on((hcf.archetype_id = a.id))) where ((hcf.child is null) and (hcf.source is not null))
UNION
SELECT (a.name) collate ci AS name,
       (a.alt_name) collate ci AS alt_name,
       (a.english_name) collate ci AS english_name,
       (c.archetype_name) collate ci AS section,
       (concat('/classes/',replace(lower(c.english_name),' ','_'),'/',replace(lower(a.english_name),' ','_'))) collate ci AS url,
       (a.description) collate ci AS description,
       (a.source) collate ci AS source
FROM (archetypes a join classes c on((c.id = a.class_id)))
UNION
SELECT (r.name) collate ci AS name,
       (r.alt_name) collate ci AS alt_name,
       (r.english_name) collate ci AS english_name,
       ('Расы и происхождения ') collate ci AS section,
       ((case when (p.name is not null) then concat('/races/',replace(lower(p.english_name),' ','_'),'/',replace(lower(r.english_name),' ','_')) else concat('/races/',replace(lower(r.english_name),' ','_')) end)) collate ci AS url,
       (r.description::text) collate ci AS description,
       (r.source) collate ci AS source
FROM (races r left join races p on((r.parent_id = p.id)))
UNION
SELECT (creatures.name) collate ci AS name,
       (creatures.alt_name) collate ci AS alt_name,
       (creatures.english_name) collate ci AS english_name,
       ('Бестиарий') collate ci AS section,
       (concat('/bestiary/',replace(lower(creatures.english_name),' ','_'))) collate ci AS url,
       (creatures.description) collate ci AS description,
       (creatures.source) collate ci AS source
FROM creatures
UNION
SELECT (artifactes.name) collate ci AS name,
       (artifactes.alt_name) collate ci AS alt_name,
       (artifactes.english_name) collate ci AS english_name,
       ('Магические предметы') collate ci AS section,
       (concat('/items/magic/',replace(lower(artifactes.english_name),' ','_'))) collate ci AS url,
       (artifactes.description::text) collate ci AS description,
       (artifactes.source) collate ci AS source
FROM artifactes
UNION
SELECT (equipments.name) collate ci AS name,
       (equipments.alt_name) collate ci AS alt_name,
       (equipments.english_name) collate ci AS english_name,
       ('Снаряжение') collate ci AS section,
       (concat('/items/',replace(lower(equipments.english_name),' ','_'))) collate ci AS url,
       (equipments.description::text) collate ci AS description,
       (equipments.source) collate ci AS source
FROM equipments
UNION
SELECT (weapons.name) collate ci AS name,
       (weapons.alt_name) collate ci AS alt_name,
       (weapons.english_name) collate ci AS english_name,
       ('Оружие') collate ci AS section,
       (concat('/weapons/',replace(lower(weapons.english_name),' ','_'))) collate ci AS url,
       (weapons.description::text) collate ci AS description,
       (weapons.source) collate ci AS source
FROM weapons
UNION
SELECT (armors.name) collate ci AS name,
       (armors.alt_name) collate ci AS alt_name,
       (armors.english_name) collate ci AS english_name,
       ('Доспехи') collate ci AS section,
       (concat('/armors/',replace(lower(armors.english_name),' ','_'))) collate ci AS url,
       (armors.description::text) collate ci AS description,
       (armors.source) collate ci AS source
FROM armors
UNION
SELECT (screens.name) collate ci AS name,
       (screens.alt_name) collate ci AS alt_name,
       (screens.english_name) collate ci AS english_name,
       ('Ширма Мастера') collate ci AS section,
       (concat('/screens/',replace(lower(screens.english_name),' ','_'))) collate ci AS url,
       (screens.description::text) collate ci AS description,
       (screens.source) collate ci AS source
FROM screens
UNION
SELECT (rules.name) collate ci AS name,
       (rules.alt_name) collate ci AS alt_name,
       (rules.english_name) collate ci AS english_name,
       ('Правила и термины') collate ci AS section,
       (concat('/rules/',replace(lower(rules.english_name),' ','_'))) collate ci AS url,
       (rules.description::text) collate ci AS description,
       (rules.source) collate ci AS source
FROM rules
UNION
SELECT (traits.name) collate ci AS name,
       (traits.alt_name) collate ci AS alt_name,
       (traits.english_name) collate ci AS english_name,
       ('Черты') collate ci AS section,
       (concat('/traits/',replace(lower(traits.english_name),' ','_'))) collate ci AS url,
       (traits.description::text) collate ci AS description,
       (traits.source) collate ci AS source
FROM traits
UNION
SELECT (options.name) collate ci AS name,
       (options.alt_name) collate ci AS alt_name,
       (options.english_name) collate ci AS english_name,
       ('Особенности классов') collate ci AS section,
       (concat('/options/',replace(lower(options.english_name),' ','_'))) collate ci AS url,
       (options.description::text) collate ci AS description,
       (options.source) collate ci AS source
FROM options
UNION
SELECT (backgrounds.name) collate ci AS name,
       (backgrounds.alt_name) collate ci AS alt_name,
       (backgrounds.english_name) collate ci AS english_name,
       ('Предыстории и происхождения') collate ci AS section,
       (concat('/backgrounds/',replace(lower(backgrounds.english_name),' ','_'))) collate ci AS url,
       (backgrounds.description::text) collate ci AS description,
       (backgrounds.source) collate ci AS source
FROM backgrounds
UNION
SELECT (gods.name) collate ci AS name,
       (gods.alt_name) collate ci AS alt_name,
       (gods.english_name) collate ci AS english_name,
       ('Боги') collate ci AS section,
       (concat('/gods/',replace(lower(gods.english_name),' ','_'))) collate ci AS url,
       (gods.description::text) collate ci AS description,
       (gods.source) collate ci AS source
FROM gods
UNION
SELECT (books.name) collate ci AS name,
       (books.alt_name) collate ci AS alt_name,
       (books.english_name) collate ci AS english_name,
       ('Источники') collate ci AS section,
       (concat('/books/',replace(lower(books.english_name),' ','_'))) collate ci AS url,
       (books.description) collate ci AS description,
       (books.source) collate ci AS source
FROM books;
