--liquibase formatted sql

-- Урон заклинаний для листа персонажа: броски (кости, бонус, модификатор характеристики,
-- типы урона, лечение, условие), рост заговора и кости за круг выше. NULL — урона нет или
-- он ещё не заполнен. Модель: club.dnd5.portal.model.splells.SpellDamage.

--changeset pg-migration:0046-spells-damage
alter table spells add column damage jsonb;
--rollback alter table spells drop column damage;
