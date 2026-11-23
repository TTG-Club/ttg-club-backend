--liquibase formatted sql

-- Рост умения по уровням (как в core-api): jsonb-список scaling у умений класса (hero_class_traits) и
-- архетипа (hero_class_feats). Элемент — {level, name, additional, description, hideInSubclasses},
-- модель club.dnd5.portal.model.classes.FeatureScaling.
--
-- До этого повтор умения («Увеличение характеристик» на 8/12/16/19) был отдельной строкой с child =
-- id первой строки и обычно пустым описанием. Такие строки сворачиваются в scaling родителя: уровень,
-- своё название (если отличается), suffix → additional, своё описание. Сворачиваются только строки, чей
-- child указывает на умение того же класса (архетипа); остальные остаются как есть. Повторы умений
-- архетипа со своими заклинаниями тоже остаются: перенос заклинаний сломал бы порядок списка.

--changeset pg-migration:0047-hero_class_traits-scaling
alter table hero_class_traits add column scaling jsonb not null default '[]'::jsonb;
--rollback alter table hero_class_traits drop column scaling;

--changeset pg-migration:0047-hero_class_feats-scaling
alter table hero_class_feats add column scaling jsonb not null default '[]'::jsonb;
--rollback alter table hero_class_feats drop column scaling;

--changeset pg-migration:0047-hero_class_traits-collapse-children
update hero_class_traits p
set scaling = rows.items
from (
    select parent.id as parent_id,
           jsonb_agg(jsonb_build_object(
               'level', c.level,
               'name', case when lower(trim(c.name)) = lower(trim(parent.name)) then null else trim(c.name) end,
               'additional', nullif(trim(c.suffix), ''),
               'description', coalesce(c.description, '[]'::jsonb),
               'hideInSubclasses', false
           ) order by c.level, c.id) as items
    from hero_class_traits c
    join hero_class_traits parent
      on c.child ~ '^\s*[0-9]+\s*$'
     and parent.id = trim(c.child)::integer
     and parent.hero_class_id = c.hero_class_id
    group by parent.id
) rows
where p.id = rows.parent_id;

delete from hero_class_traits c
using hero_class_traits parent
where c.child ~ '^\s*[0-9]+\s*$'
  and parent.id = trim(c.child)::integer
  and parent.hero_class_id = c.hero_class_id;
--rollback insert into hero_class_traits (name, suffix, level, description, hero_class_id, architype, child, optional, source, scaling)
--rollback select coalesce(s->>'name', p.name), coalesce(s->>'additional', ''), (s->>'level')::smallint, s->'description',
--rollback        p.hero_class_id, false, p.id::text, 0, p.source, '[]'::jsonb
--rollback from hero_class_traits p, jsonb_array_elements(p.scaling) s;
--rollback update hero_class_traits set scaling = '[]'::jsonb;

--changeset pg-migration:0047-hero_class_feats-collapse-children
update hero_class_feats p
set scaling = rows.items
from (
    select parent.id as parent_id,
           jsonb_agg(jsonb_build_object(
               'level', c.level,
               'name', case when lower(trim(c.name)) = lower(trim(parent.name)) then null else trim(c.name) end,
               'additional', nullif(trim(c.suffix), ''),
               'description', coalesce(c.description, '[]'::jsonb),
               'hideInSubclasses', false
           ) order by c.level, c.id) as items
    from hero_class_feats c
    join hero_class_feats parent
      on c.child ~ '^\s*[0-9]+\s*$'
     and parent.id = trim(c.child)::integer
     and parent.archetype_id = c.archetype_id
    where not exists (select 1 from hero_class_feats_spells s where s.archetype_trait_id = c.id)
    group by parent.id
) rows
where p.id = rows.parent_id;

delete from hero_class_feats c
using hero_class_feats parent
where c.child ~ '^\s*[0-9]+\s*$'
  and parent.id = trim(c.child)::integer
  and parent.archetype_id = c.archetype_id
  and not exists (select 1 from hero_class_feats_spells s where s.archetype_trait_id = c.id);
--rollback insert into hero_class_feats (name, suffix, level, description, archetype_id, child, source, scaling)
--rollback select coalesce(s->>'name', p.name), coalesce(s->>'additional', ''), (s->>'level')::smallint, s->'description',
--rollback        p.archetype_id, p.id::text, p.source, '[]'::jsonb
--rollback from hero_class_feats p, jsonb_array_elements(p.scaling) s;
--rollback update hero_class_feats set scaling = '[]'::jsonb;
