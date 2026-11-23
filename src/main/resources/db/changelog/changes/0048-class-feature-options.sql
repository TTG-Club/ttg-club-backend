--liquibase formatted sql

-- Варианты умения класса (как в core-api): у умений класса (hero_class_traits) и архетипа
-- (hero_class_feats) появляются тип списка (options_type → option_type_definitions.code), название
-- списка (options_name), сами варианты (options, jsonb-список FeatureOption) и настройка выбора для
-- листа персонажа (options_choice, FeatureOptionsChoice; NULL — варианты только справка).
--
-- Умение — единственное место, где вариант правится; раздел «Особенности классов» (таблица options)
-- остаётся и обновляется из умений при сохранении класса (FeatureOptionProjection). Типы переносятся
-- по таблице соответствия «тип → умение» (класс, архетип, название умения). Шесть спорных типов
-- (MUTAGEN, TRANSPLANTS, BONUS_DISCIPLINES, PSIONIC_TALANT, SIGNATURE_TECHNIQUES,
-- FIGHTING_STYLE_ALT_PALADIN) не переносятся: их привязывают в мастерской вручную.

--changeset pg-migration:0048-hero_class_traits-options
alter table hero_class_traits
    add column options_type varchar(64) collate ci,
    add column options_name varchar(255) collate ci,
    add column options jsonb not null default '[]'::jsonb,
    add column options_choice jsonb;
--rollback alter table hero_class_traits drop column options_type, drop column options_name, drop column options, drop column options_choice;

--changeset pg-migration:0048-hero_class_feats-options
alter table hero_class_feats
    add column options_type varchar(64) collate ci,
    add column options_name varchar(255) collate ci,
    add column options jsonb not null default '[]'::jsonb,
    add column options_choice jsonb;
--rollback alter table hero_class_feats drop column options_type, drop column options_name, drop column options, drop column options_choice;

--changeset pg-migration:0048-hero_class_traits-options-type
update hero_class_traits t
set options_type = m.code,
    options_name = trim(split_part(d.name, ':', 1))
from (values
    ('WILD_SHAPE', 'Druid', 'Дикий облик'),
    ('METAMAGIC', 'Sorcerer', 'Метамагия'),
    ('ELDRITCH_INVOCATION', 'Warlock', 'Таинственные воззвания'),
    ('BONE', 'Warlock', 'Предмет договора'),
    ('ARTIFICER_INFUSION', 'Artificer', 'Инфузирование предметов'),
    ('FIGHTING_STYLE', 'Fighter', 'Боевой стиль'),
    ('FIGHTING_STYLE_RANGER', 'Ranger', 'Боевой стиль'),
    ('FIGHTING_STYLE_PALADIN', 'Paladin', 'Боевой стиль'),
    ('FIGHTING_STYLE_BLOODHANTER', 'Blood Hunter', 'Боевой стиль'),
    ('BLOOD_CURSE', 'Blood Hunter', 'Проклятая кровь'),
    ('BOMB_FORMULA', 'Alchemist', 'Формулы бомб'),
    ('DISCOVERIES', 'Alchemist', 'Открытия'),
    ('ACADEMIC_DISCIPLINES', 'Savant', 'Научные исследования'),
    ('TOTEMS', 'Shaman', 'Тотемы'),
    ('FIGHTING_STYLE_ALT_FIGHTER', 'Alternate Fighter', 'Боевой стиль'),
    ('MARTIAL_EXPLOITS', 'Alternate Fighter', 'Боевые приёмы'),
    ('SAVAGE_EXPLOITS', 'Alternate Barbarian', 'Дикие приёмы'),
    ('DEGREE_EXPLOITS', 'Alternate Rogue', 'Коварные приёмы')
) as m(code, class_name, trait_name)
join option_type_definitions d on d.code = m.code
join classes c on c.english_name = m.class_name
where t.id = (
    select min(t2.id)
    from hero_class_traits t2
    where t2.hero_class_id = c.id
      and t2.name = m.trait_name
      and coalesce(t2.architype, false) = false
);
--rollback update hero_class_traits set options_type = null, options_name = null;

--changeset pg-migration:0048-hero_class_feats-options-type
update hero_class_feats f
set options_type = m.code,
    options_name = trim(split_part(d.name, ':', 1))
from (values
    ('ARCANE_SHOT', 'Fighter', 'arcane_archer', 'Магический выстрел'),
    ('ELEMENTAL_DISCIPLINE', 'Monk', 'four_elements', 'Адепт стихий'),
    ('RUNE', 'Fighter', 'rune_knight', 'Резчик рун'),
    ('MANEUVER', 'Fighter', 'battle_master', 'Боевое превосходство'),
    ('FIGHTING_STYLE_BARD', 'Bard', 'swords', 'Боевой стиль'),
    ('PHILOSOPHICAL_SCHOOL', 'Wizard', 'academy_philosopher', 'Философская школа')
) as m(code, class_name, archetype_slug, trait_name)
join option_type_definitions d on d.code = m.code
join classes c on c.english_name = m.class_name
join archetypes a on a.class_id = c.id and lower(replace(a.english_name, ' ', '_')) = m.archetype_slug
where f.id = (
    select min(f2.id)
    from hero_class_feats f2
    where f2.archetype_id = a.id
      and f2.name = m.trait_name
);
--rollback update hero_class_feats set options_type = null, options_name = null;

--changeset pg-migration:0048-hero_class_traits-options-fill
update hero_class_traits t
set options = coalesce((
    select jsonb_agg(jsonb_build_object(
        'name', o.name,
        'englishName', o.english_name,
        'altName', o.alt_name,
        'level', o.level,
        'prerequisite', nullif(nullif(trim(o.prerequisite), ''), 'Нет'),
        'additional', null,
        'description', coalesce(o.description, '[]'::jsonb),
        'repeatable', false,
        'hideInSubclasses', false,
        'mechanics', o.mechanics,
        'activeEffects', o.active_effects,
        'source', o.source,
        'page', o.page
    ) order by o.level nulls first, o.name)
    from options o
    join option_types ot on ot.option_id = o.id
    where ot.option_type = t.options_type
), '[]'::jsonb)
where t.options_type is not null;
--rollback update hero_class_traits set options = '[]'::jsonb;

--changeset pg-migration:0048-hero_class_feats-options-fill
update hero_class_feats f
set options = coalesce((
    select jsonb_agg(jsonb_build_object(
        'name', o.name,
        'englishName', o.english_name,
        'altName', o.alt_name,
        'level', o.level,
        'prerequisite', nullif(nullif(trim(o.prerequisite), ''), 'Нет'),
        'additional', null,
        'description', coalesce(o.description, '[]'::jsonb),
        'repeatable', false,
        'hideInSubclasses', false,
        'mechanics', o.mechanics,
        'activeEffects', o.active_effects,
        'source', o.source,
        'page', o.page
    ) order by o.level nulls first, o.name)
    from options o
    join option_types ot on ot.option_id = o.id
    where ot.option_type = f.options_type
), '[]'::jsonb)
where f.options_type is not null;
--rollback update hero_class_feats set options = '[]'::jsonb;
