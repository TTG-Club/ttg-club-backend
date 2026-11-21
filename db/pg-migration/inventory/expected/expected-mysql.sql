
    create table action_damage (
        id integer not null auto_increment,
        context varchar(255),
        formula varchar(255),
        type enum ('ACID','BLUDGEONING','COLD','DARK','FAIR','FORCE','LIGHTNING','MAGIC','METAL_WEAPON','NECROTIC','NECTOTIC','NO_ADMANTIT','NO_DAMAGE','NO_NOSILVER','PHYSICAL','PHYSICAL_MAGIC','PIERCING','PIERCING_GOOD','POISON','PSYCHIC','RADIANT','SLASHING','SOUND','THUNDER','VORPAL_SWORD'),
        primary key (id)
    ) engine=InnoDB;

    create table archetype_spell (
        id integer not null auto_increment,
        advenced varchar(255),
        level integer not null,
        type varchar(255),
        spell_id integer,
        archetype_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table archetypes (
        id integer not null auto_increment,
        description TEXT,
        english_name varchar(255),
        genitive_name varchar(255),
        level tinyint not null,
        name varchar(255),
        page smallint,
        spell_level_type varchar(32) default 'CLASS_LEVEL' not null,
        spellcaster_type enum ('FULL','HALF','NONE','PARTLY'),
        source varchar(255),
        class_id integer,
        option_type varchar(64),
        primary key (id)
    ) engine=InnoDB;

    create table armors (
        id integer not null auto_increment,
        ac integer not null,
        alt_name varchar(255),
        cost integer not null,
        description TEXT,
        english_name varchar(255),
        force_requirements integer,
        name varchar(255),
        stels_hindrance bit not null,
        type tinyint,
        weight float(23) not null,
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table artifactes (
        id integer not null auto_increment,
        alt_name varchar(255),
        bonus tinyint,
        charge integer,
        consumed bit not null,
        cost integer,
        curse bit,
        cust_special varchar(255),
        customization bit,
        description TEXT,
        english_name varchar(255),
        name varchar(255),
        page smallint,
        rarity tinyint,
        special varchar(255),
        type tinyint,
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table artifactes_armors (
        magic_item_id integer not null,
        armors_id integer not null,
        sort_order integer not null,
        primary key (magic_item_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table artifactes_cust_classes (
        magic_item_id integer not null,
        cust_classes_id integer not null,
        sort_order integer not null,
        primary key (magic_item_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table artifactes_weapons (
        magic_item_id integer not null,
        weapons_id integer not null,
        sort_order integer not null,
        primary key (magic_item_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table background_personalizations (
        id integer not null auto_increment,
        text TEXT,
        type enum ('ADOPTED_CULTURE','AFFECTION','AMPLOIS','BAUBLE','CREED','DEFINING_EVENT','DISFIGURED','EMPLOYMENT','HERITAGE','IDEAL','LIFE_IN_SECLUSION','SCAM','SPECIALIZATION','TRAIT','VALUABLE_ITEMS','WEAKNESS','WHERE_ARE_YOU_FROM','WHY_ARE_YOU_HERE'),
        background_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table background_skill_type (
        background_id integer not null,
        skills enum ('ACROBATICS','ANIMAL_HANDLING','ARCANA','ATHLETICS','DECEPTION','HISTORY','INSIGHT','INTIMIDATION','INVESTIGATION','MEDICINE','NATURE','PERCEPTION','PERFORMANCE','PERSUASION','RELIGION','SLEIGHT_OF_HAND','STEALTH','SURVIVAL') not null,
        sort_order integer not null,
        primary key (background_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table backgrounds (
        id integer not null auto_increment,
        alt_name varchar(255),
        description TEXT,
        english_name varchar(255),
        equipments_text TEXT,
        `language` varchar(255),
        life_style enum ('ANYTHING','ARISTOCRATIC','BEGGARLY','COMFORTABLE','MODEST','POOR','RICH'),
        name varchar(255),
        other_skills TEXT,
        page smallint,
        personalization TEXT,
        skill_description TEXT,
        skill_name varchar(255),
        start_money integer not null,
        tool_ownership TEXT,
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table backgrounds_languages (
        background_id integer not null,
        languages_id integer not null,
        sort_order integer not null,
        primary key (background_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table bookmarks (
        uuid varchar(36) not null,
        name varchar(255),
        bookmark_order integer,
        prefix varchar(255),
        url varchar(255),
        parent_id varchar(36),
        user_id bigint,
        primary key (uuid)
    ) engine=InnoDB;

    create table books (
        source varchar(255) not null,
        alt_name varchar(255),
        cover varchar(255),
        description TEXT,
        english_name varchar(255),
        name varchar(255),
        type enum ('CUSTOM','MODULE','OFFICAL','SETTING','TEST','THIRD_PARTY'),
        `year` integer,
        primary key (source)
    ) engine=InnoDB;

    create table class_available_skills (
        class_id integer not null,
        skill enum ('ACROBATICS','ANIMAL_HANDLING','ARCANA','ATHLETICS','DECEPTION','HISTORY','INSIGHT','INTIMIDATION','INVESTIGATION','MEDICINE','NATURE','PERCEPTION','PERFORMANCE','PERSUASION','RELIGION','SLEIGHT_OF_HAND','STEALTH','SURVIVAL') not null,
        sort_order integer not null,
        primary key (class_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table class_primary_abilities (
        class_id integer not null,
        ability enum ('CHARISMA','CHOICE','CHOICE_DOUBLE','CHOICE_UNIQUE','CONSTITUTION','DEXTERITY','INTELLIGENCE','ONE','STRENGTH','WISDOM') not null,
        sort_order integer not null,
        primary key (class_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table classes (
        id integer not null auto_increment,
        name_accusative varchar(255),
        archetype_name varchar(255),
        armor varchar(255),
        description TEXT,
        dice_hp tinyint not null,
        enabled_arhitype_level integer not null,
        english_name varchar(255),
        equipment TEXT,
        icon varchar(255),
        name varchar(255),
        page smallint,
        saving_throws varchar(255),
        sidekick bit not null,
        skill_available_count smallint not null,
        slots_reset enum ('FULL','LONG','SHORT'),
        spell_ability enum ('CHARISMA','CHOICE','CHOICE_DOUBLE','CHOICE_UNIQUE','CONSTITUTION','DEXTERITY','INTELLIGENCE','ONE','STRENGTH','WISDOM'),
        spellcaster_type enum ('FULL','HALF','NONE','PARTLY'),
        tools varchar(255),
        weapon varchar(255),
        source varchar(255),
        option_type varchar(64),
        primary key (id)
    ) engine=InnoDB;

    create table classes_feature_level_definitions (
        id integer not null auto_increment,
        l1 tinyint,
        l10 tinyint,
        l11 tinyint,
        l12 tinyint,
        l13 tinyint,
        l14 tinyint,
        l15 tinyint,
        l16 tinyint,
        l17 tinyint,
        l18 tinyint,
        l19 tinyint,
        l2 tinyint,
        l20 tinyint,
        l3 tinyint,
        l4 tinyint,
        l5 tinyint,
        l6 tinyint,
        l7 tinyint,
        l8 tinyint,
        l9 tinyint,
        name varchar(255),
        prefix varchar(255),
        sufix varchar(255),
        hero_class_id integer,
        archetype_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table conditions (
        id integer not null auto_increment,
        description TEXT,
        english_name varchar(255),
        name varchar(255),
        page smallint,
        type enum ('CONDITION','DISEASE','OTHER'),
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table creature_actions (
        id integer not null auto_increment,
        action_type tinyint,
        description TEXT,
        english_name varchar(255),
        markdown boolean default false,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table creature_armor_types (
        creature_id integer not null,
        armor_types enum ('BONECRAFT','BREASTPLATE','CHAINMAIL','CHAIN_MAIL','CHAIN_SHIRT','CUIRASS','HALF_PLATE','HIDE','LEATHER','MAGE_ARMOR','NATURAL','PADDED_ARMOR','PLATE','PLATE_HALF','RING_MAIL','RIVETED_LEATHER','SCALED','SCALE_MAIL','SCRAPPY','SCRAPS','SHIELD','SPLINT','STUDDED_LEATHER'),
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table creature_feats (
        id integer not null auto_increment,
        description TEXT,
        english_name varchar(255),
        markdown boolean default false,
        name varchar(255),
        creature_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table creature_habitates (
        creature_id integer not null,
        habitates enum ('ARCTIC','CITY','COAST','DESERT','DUNGEON','FOREST','GRASSLAND','HILL','MOUNTAIN','RUINS','SWAMP','TROPICS','UNDERGROUND','VILLAGE','WATERS'),
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table creature_immunity_states (
        creature_id integer not null,
        immunity_states tinyint,
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table creature_immunity_damages (
        creature_id integer not null,
        immunity_damages tinyint,
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table creature_races (
        id integer not null auto_increment,
        description TEXT,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table creature_resistance_damages (
        creature_id integer not null,
        resistance_damages tinyint,
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table creature_saving_throws (
        id integer not null auto_increment,
        ability enum ('CHARISMA','CHOICE','CHOICE_DOUBLE','CHOICE_UNIQUE','CONSTITUTION','DEXTERITY','INTELLIGENCE','ONE','STRENGTH','WISDOM'),
        additional_bonus varchar(255),
        bonus tinyint not null,
        creature_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table creature_skills (
        id integer not null auto_increment,
        additional_bonus varchar(255),
        bonus tinyint not null,
        type enum ('ACROBATICS','ANIMAL_HANDLING','ARCANA','ATHLETICS','DECEPTION','HISTORY','INSIGHT','INTIMIDATION','INVESTIGATION','MEDICINE','NATURE','PERCEPTION','PERFORMANCE','PERSUASION','RELIGION','SLEIGHT_OF_HAND','STEALTH','SURVIVAL'),
        creature_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table creature_spellcaster (
        id integer not null auto_increment,
        attack tinyint,
        dc tinyint,
        level tinyint,
        slot1 tinyint,
        slot2 tinyint,
        slot3 tinyint,
        slot4 tinyint,
        slot5 tinyint,
        slot6 tinyint,
        slot7 tinyint,
        slot8 tinyint,
        slot9 tinyint,
        spell_ability enum ('CHARISMA','CHOICE','CHOICE_DOUBLE','CHOICE_UNIQUE','CONSTITUTION','DEXTERITY','INTELLIGENCE','ONE','STRENGTH','WISDOM'),
        creature_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table creature_spells (
        id integer not null auto_increment,
        count tinyint not null,
        unit varchar(255),
        spell_id integer,
        creature_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table creature_vulnerability_damages (
        creature_id integer not null,
        vulnerability_damages tinyint,
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table creatures (
        id integer not null auto_increment,
        ac tinyint not null,
        alignment tinyint,
        alignment_suffix varchar(255),
        alt_name varchar(255),
        average_hp smallint not null,
        blindsight integer,
        blindsight_radius integer,
        bonusac varchar(255),
        bonushp smallint,
        challenge_rating varchar(255),
        charisma tinyint not null,
        climbing_speed smallint,
        constitution tinyint not null,
        count_dice_hp smallint,
        darkvision integer,
        description TEXT,
        dexterity tinyint not null,
        dice_hp tinyint,
        digging_speed smallint,
        english_name varchar(255),
        exp integer not null,
        fly_speed smallint,
        hover smallint,
        intellect tinyint not null,
        `language` TEXT,
        legendary TEXT,
        name varchar(255) not null,
        page smallint,
        passive_perception tinyint not null,
        passive_perception_bonus varchar(255),
        proficiency_bonus varchar(255),
        race_id integer,
        race_name varchar(255),
        reaction TEXT,
        size tinyint,
        speed tinyint not null,
        speed_text varchar(255),
        strength tinyint not null,
        suffixhp varchar(255),
        swimming_spped smallint,
        trysight integer,
        type tinyint,
        vibration integer,
        wizdom tinyint not null,
        source varchar(255),
        lair_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table creatures_actions (
        creature_id integer not null,
        actions_id integer not null,
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table creatures_languages (
        creature_id integer not null,
        languages_id integer not null,
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table creatures_races (
        creature_id integer not null,
        races_id integer not null,
        sort_order integer not null,
        primary key (creature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table dm_screens (
        id varchar(36) not null,
        created_at datetime(6),
        name varchar(64) not null,
        updated_at datetime(6) not null,
        windows text not null,
        user_id bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table equipments (
        id integer not null auto_increment,
        alt_name varchar(255),
        cost integer,
        currency tinyint,
        description TEXT,
        english_name varchar(255),
        name varchar(255),
        page smallint,
        weight float(23),
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table equipments_types (
        equipment_id integer not null,
        type enum ('ADVENTURING_GEAR','AMMUNITION','ARMOR','ARTISANS_TOOLS','CLOTHES','CONTAINER','DRUID_FOCUS','EAT_DRINK','EXOTIC_WEAPON','FIRE_WEAPON','FOCUSING','GAME_SET','HEAVY_ARMOR','HOLY_SYMBOL','LIGHT','LIGHT_ARMOR','MARTIAL_WEAPON','MEDIUM_ARMOR','MELE_WAPON','MOUNT','MUSICAL_INSTRUMENTS','POISON','RANGE_WAPON','SET','SHIELD','SIEGE_EQUIPMENT','SIMPLE_WEAPON','THROWABLE','TOOL','TRADE_GOOD','VEHICLE_AIR','VEHICLE_LAND','VEHICLE_SPELLJAMMER','VEHICLE_WATER','WEAPON') not null,
        primary key (equipment_id, type)
    ) engine=InnoDB;

    create table god_domains (
        god_id integer not null,
        domains enum ('ARCANA','DEATH','DECEPTION','FORGE','GRAVE','KNOWLEDGE','LIFE','LIGHT','NATURE','NONE','ORDER','REPOSE','STORM','TEMPEST','TRICKERY','UNDEFINE','WAR'),
        sort_order integer not null,
        primary key (god_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table god_pantheons (
        id integer not null auto_increment,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table gods (
        id integer not null auto_increment,
        aligment enum ('ALL','ALL_EVIL','CHAOTIC_EVIL','CHAOTIC_GOOD','CHAOTIC_NEUTRAL','LAWFUL_EVIL','LAWFUL_GOOD','LAWFUL_NEUTRAL','NEUTRAL','NEUTRAL_EVIL','NEUTRAL_GOOD','NO_GOOD','TRUE_NEUTRAL','WITHOUT'),
        alt_name varchar(255),
        alternative_description TEXT,
        commitment varchar(255),
        description TEXT,
        english_name varchar(255),
        name varchar(255),
        nicknames varchar(255),
        page smallint,
        `rank` enum ('ABSOLUTE','DEAD','GREAT','HALF','LESS','MIDDLE','QUASI'),
        sex enum ('FEMALE','MALE','PHILOSOPHY','UNDEFINE'),
        symbol varchar(255),
        version integer default 1,
        source varchar(255),
        pantheon_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table hero_class_feats (
        id integer not null auto_increment,
        child varchar(255),
        description TEXT,
        level tinyint not null,
        name varchar(255),
        suffix varchar(25) default '',
        archetype_id integer,
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table hero_class_feats_spells (
        archetype_trait_id integer not null,
        spells_id integer not null,
        sort_order integer not null,
        primary key (archetype_trait_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table hero_class_traits (
        id integer not null auto_increment,
        architype bit not null,
        child varchar(255),
        description TEXT,
        level tinyint not null,
        name varchar(255),
        optional integer not null,
        suffix varchar(25) default '',
        source varchar(255),
        hero_class_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table hero_madness (
        id integer not null auto_increment,
        description TEXT,
        type enum ('LONG','SHORT','UNLIMITED'),
        other varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table images (
        id bigint not null auto_increment,
        ref_id integer,
        type enum ('ARMOR','CLASS','CREATURE','GOD','ITEM','MAGIC_ITEM','RACE','SUBCLASS','WEAPON'),
        url varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table info_pages (
        url varchar(255) not null,
        description TEXT,
        english_name varchar(255),
        subtitle varchar(255),
        title varchar(255),
        primary key (url)
    ) engine=InnoDB;

    create table initiative_participant (
        id varchar(36) not null,
        created_at datetime(6),
        creature_url varchar(255),
        dead bit not null,
        initiative_bonus integer not null,
        initiative_roll integer,
        initiative_total integer,
        name varchar(255) not null,
        seq integer not null,
        tie_roll integer,
        tracker_id varchar(36) not null,
        type enum ('CREATURE','PLAYER') not null,
        updated_at datetime(6),
        primary key (id)
    ) engine=InnoDB;

    create table initiative_tracker (
        id varchar(36) not null,
        access_key varchar(36) not null,
        created_at datetime(6),
        current_participant_id varchar(36),
        deleted bit not null,
        name varchar(255) not null,
        owner_username varchar(255),
        reroll_each_round bit not null,
        round integer not null,
        status enum ('ACTIVE','PREPARING') not null,
        updated_at datetime(6),
        primary key (id)
    ) engine=InnoDB;

    create table invitation (
        id bigint not null auto_increment,
        code varchar(255),
        expiration_time bigint,
        generation_date datetime(6),
        link varchar(255),
        unique_identifier varchar(255),
        user_party_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table json_storage (
        ref_id integer not null,
        type_json enum ('CREATURE','SPELL') not null,
        version_foundry enum ('V10','V11') not null,
        json_data text not null,
        name varchar(255) not null,
        primary key (ref_id, type_json, version_foundry)
    ) engine=InnoDB;

    create table lairs (
        lair_id integer not null auto_increment,
        action TEXT,
        description TEXT,
        effect TEXT,
        primary key (lair_id)
    ) engine=InnoDB;

    create table languages (
        id integer not null auto_increment,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table level_definitions (
        id integer not null auto_increment,
        level tinyint,
        slot1 tinyint,
        slot2 tinyint,
        slot3 tinyint,
        slot4 tinyint,
        slot5 tinyint,
        slot6 tinyint,
        slot7 tinyint,
        slot8 tinyint,
        slot9 tinyint,
        hero_class_id integer,
        archetype_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table magic_things_random (
        id integer not null auto_increment,
        `end` integer not null,
        name_table varchar(255),
        `start` integer not null,
        magic_thing_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table menu (
        id smallint not null auto_increment,
        `external` bit not null,
        icon varchar(255),
        index_order integer,
        name varchar(255) not null,
        on_index bit not null,
        only_dev bit not null,
        `order` integer not null,
        url varchar(255),
        parent_id smallint,
        primary key (id)
    ) engine=InnoDB;

    create table name_owners (
        id bigint not null auto_increment,
        english_name varchar(255),
        name varchar(255),
        parent_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table news_banner (
        id integer not null auto_increment,
        active bit not null,
        created_at datetime(6),
        description varchar(255),
        image varchar(255),
        name varchar(255),
        url varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table option_type_definitions (
        code varchar(64) not null,
        archetype_name varchar(255),
        class_name varchar(255) not null,
        feature_stem varchar(255),
        homebrew bit not null,
        name varchar(255) not null,
        ordering integer,
        primary key (code)
    ) engine=InnoDB;

    create table option_types (
        option_id integer not null,
        option_type varchar(64) not null,
        sort_order integer not null,
        primary key (option_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table options (
        id integer not null auto_increment,
        alt_name varchar(255),
        description TEXT,
        english_name varchar(255),
        level integer,
        name varchar(255),
        page smallint,
        prerequisite varchar(255),
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table partners (
        id smallint not null auto_increment,
        description varchar(255),
        img varchar(255) not null,
        name varchar(255) not null,
        `order` integer not null,
        url varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table race_bonuses (
        id integer not null auto_increment,
        ability enum ('CHARISMA','CHOICE','CHOICE_DOUBLE','CHOICE_UNIQUE','CONSTITUTION','DEXTERITY','INTELLIGENCE','ONE','STRENGTH','WISDOM'),
        bonus tinyint,
        race_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table race_feature_skill_type (
        feature_id integer not null,
        skills enum ('ACROBATICS','ANIMAL_HANDLING','ARCANA','ATHLETICS','DECEPTION','HISTORY','INSIGHT','INTIMIDATION','INVESTIGATION','MEDICINE','NATURE','PERCEPTION','PERFORMANCE','PERSUASION','RELIGION','SLEIGHT_OF_HAND','STEALTH','SURVIVAL') not null,
        sort_order integer not null,
        primary key (feature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table race_features (
        id integer not null auto_increment,
        description TEXT,
        english_name varchar(255),
        feature bit not null,
        name varchar(255),
        race_id integer not null,
        replace_feature_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table race_features_lanuages (
        feature_id integer not null,
        lanuages_id integer not null,
        sort_order integer not null,
        primary key (feature_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table race_names (
        id integer not null auto_increment,
        name varchar(255),
        sex enum ('CHILD','FEMALE','MALE','UNISEX'),
        race_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table race_nicknames (
        id integer not null auto_increment,
        name varchar(255),
        type enum ('CLAN','DYNASTY','FROM','HOUSE','NICKNAME','SQUAD','SURNAME','TRIBE'),
        race_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table race_spells (
        race_id integer not null,
        spells_id integer not null,
        sort_order integer not null,
        primary key (race_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table races (
        id integer not null auto_increment,
        alt_name varchar(255),
        climb integer,
        darkvision integer,
        description TEXT,
        english_name varchar(255),
        fly integer,
        icon varchar(255),
        max_age integer,
        min_age integer,
        name varchar(255),
        origin bit,
        page smallint,
        size varchar(255) default 'MEDIUM' not null,
        speed int default 30,
        swim integer,
        type enum ('ABERRATION','BEAST','CELESTIAL','CONSTRUCT','DEVIL','DRAGON','ELEMENTAL','FEY','FIEND','GIANT','HUMANOID','MONSTROSITY','OOZE','OUTSIDER','PLANT','SLIME','SMALL_BEAST','SWARM','UNDEAD','VERMIN'),
        view bit not null,
        source varchar(255),
        parent_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table races_languages (
        race_id integer not null,
        languages_id integer not null,
        sort_order integer not null,
        primary key (race_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table random_encounter_tables (
        id integer not null auto_increment,
        environment varchar(255),
        formula varchar(255),
        level integer,
        name TEXT,
        type enum ('ARCTIC','CITY','COAST','DESERT','DUNGEON','FOREST','GRASSLAND','HILL','MOUNTAIN','RUINS','SWAMP','TROPICS','UNDERGROUND','VILLAGE','WATERS'),
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table random_encounters (
        id integer not null auto_increment,
        description TEXT,
        `end` integer not null,
        level integer not null,
        `start` integer not null,
        type enum ('ARCTIC','CITY','COAST','DESERT','DUNGEON','FOREST','GRASSLAND','HILL','MOUNTAIN','RUINS','SWAMP','TROPICS','UNDERGROUND','VILLAGE','WATERS'),
        source varchar(255),
        encounter_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table revision_history (
        id bigint not null auto_increment,
        created_at datetime(6) not null,
        entity_id integer not null,
        entity_type varchar(64) not null,
        operation enum ('CREATE','DELETE','UPDATE') not null,
        revision integer not null,
        snapshot text,
        user_id bigint,
        username varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table roles (
        id bigint not null auto_increment,
        name varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table rules (
        id integer not null auto_increment,
        alt_name varchar(255),
        description TEXT,
        english_name varchar(255),
        name varchar(255),
        page smallint,
        type varchar(255),
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table screens (
        id integer not null auto_increment,
        alt_name varchar(255),
        category varchar(255),
        description TEXT,
        english_name varchar(255),
        icon varchar(255),
        name varchar(255),
        ordering integer not null,
        source varchar(255),
        parent_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table site_configuration (
        config_key varchar(255) not null,
        config_value varchar(255),
        primary key (config_key)
    ) engine=InnoDB;

    create table spell_damage_type (
        spell_id integer not null,
        damage_type enum ('ACID','BLUDGEONING','COLD','DARK','FAIR','FORCE','LIGHTNING','MAGIC','METAL_WEAPON','NECROTIC','NECTOTIC','NO_ADMANTIT','NO_DAMAGE','NO_NOSILVER','PHYSICAL','PHYSICAL_MAGIC','PIERCING','PIERCING_GOOD','POISON','PSYCHIC','RADIANT','SLASHING','SOUND','THUNDER','VORPAL_SWORD') not null,
        sort_order integer not null,
        primary key (spell_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table spell_heal_type (
        spell_id integer not null,
        heal_type enum ('HP','THP') not null,
        sort_order integer not null,
        primary key (spell_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table spell_savingthrows (
        spell_id integer not null,
        ability enum ('CHARISMA','CHOICE','CHOICE_DOUBLE','CHOICE_UNIQUE','CONSTITUTION','DEXTERITY','INTELLIGENCE','ONE','STRENGTH','WISDOM') not null,
        sort_order integer not null,
        primary key (spell_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table spell_source (
        id integer not null auto_increment,
        class_id integer not null,
        spell_id integer not null,
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table spells (
        id integer not null auto_increment,
        additional_material_component TEXT,
        additional_type varchar(255),
        alt_name varchar(255),
        concentration bit,
        consumable boolean default false,
        description TEXT,
        distance varchar(255),
        duration varchar(255),
        english_name varchar(255),
        level tinyint not null,
        name varchar(255),
        page smallint,
        ritual bit,
        school tinyint,
        somatic_component bit not null,
        srd bit,
        time_cast integer not null,
        upper_level TEXT,
        verbal_component bit not null,
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table spells_hero_class (
        spell_id integer not null,
        hero_class_id integer not null,
        sort_order integer not null,
        primary key (spell_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table spells_times (
        id integer not null auto_increment,
        `condition` varchar(255),
        number integer not null,
        unit enum ('ACTION','BONUS','HOUR','MINUTE','ON_HIT','REACTION','ROUND') not null,
        spell_id integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table tavern_owner_secret (
        id integer not null auto_increment,
        description TEXT,
        sex enum ('CHILD','FEMALE','MALE','UNISEX'),
        primary key (id)
    ) engine=InnoDB;

    create table tavern_owner_traits (
        id integer not null auto_increment,
        description TEXT,
        sex enum ('CHILD','FEMALE','MALE','UNISEX'),
        primary key (id)
    ) engine=InnoDB;

    create table tavern_owner_weaknesses (
        id integer not null auto_increment,
        description TEXT,
        sex enum ('CHILD','FEMALE','MALE','UNISEX'),
        primary key (id)
    ) engine=InnoDB;

    create table taverna_atmosphera (
        id integer not null auto_increment,
        description TEXT,
        name varchar(255),
        visitors integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table taverna_dishes (
        id integer not null auto_increment,
        category enum ('CHEAP','ELITE','EXPENSIVE','ORDINARY'),
        habitat enum ('ARCTIC','CITY','COAST','DESERT','DUNGEON','FOREST','GRASSLAND','HILL','MOUNTAIN','RUINS','SWAMP','TROPICS','UNDERGROUND','VILLAGE','WATERS'),
        name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table taverna_drink_effects (
        id integer not null auto_increment,
        description TEXT,
        primary key (id)
    ) engine=InnoDB;

    create table taverna_drinks (
        id integer not null auto_increment,
        category enum ('CHEAP','ELITE','EXPENSIVE','ORDINARY'),
        habitat enum ('ARCTIC','CITY','COAST','DESERT','DUNGEON','FOREST','GRASSLAND','HILL','MOUNTAIN','RUINS','SWAMP','TROPICS','UNDERGROUND','VILLAGE','WATERS'),
        name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table taverna_events (
        id integer not null auto_increment,
        description TEXT,
        visitors integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table taverna_names (
        id integer not null auto_increment,
        name varchar(255),
        names varchar(255),
        object_type enum ('ANIMATED','OBJECT'),
        sex enum ('CHILD','FEMALE','MALE','UNISEX'),
        primary key (id)
    ) engine=InnoDB;

    create table taverna_prefixes (
        id integer not null auto_increment,
        female varchar(255),
        male varchar(255),
        name varchar(255),
        neuter varchar(255),
        object_type enum ('ANIMATED','OBJECT'),
        primary key (id)
    ) engine=InnoDB;

    create table taverna_race_habitates (
        id integer not null auto_increment,
        chance integer not null,
        english_name varchar(255) not null,
        habitat enum ('ARCTIC','CITY','COAST','DESERT','DUNGEON','FOREST','GRASSLAND','HILL','MOUNTAIN','RUINS','SWAMP','TROPICS','UNDERGROUND','VILLAGE','WATERS'),
        primary key (id)
    ) engine=InnoDB;

    create table taverna_topics (
        id integer not null auto_increment,
        name TEXT,
        visitors integer not null,
        primary key (id)
    ) engine=InnoDB;

    create table taverna_visitor_chances (
        id integer not null auto_increment,
        chance integer not null,
        taverna_category enum ('CHEAP','ELITE','EXPENSIVE','ORDINARY'),
        taverna_type enum ('BEER','CAFE','GAMBLING_DEN','HOTEL','INN','RESTAURANT'),
        visitor_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table taverna_visitor_habitates (
        visitor_id integer not null,
        habitat_types enum ('ACROBATICS','ANIMAL_HANDLING','ARCANA','ATHLETICS','DECEPTION','HISTORY','INSIGHT','INTIMIDATION','INVESTIGATION','MEDICINE','NATURE','PERCEPTION','PERFORMANCE','PERSUASION','RELIGION','SLEIGHT_OF_HAND','STEALTH','SURVIVAL') not null,
        sort_order integer not null,
        primary key (visitor_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table taverna_visitors (
        id integer not null auto_increment,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table token_borders (
        id bigint not null auto_increment,
        name varchar(255) not null,
        type varchar(255) not null,
        url varchar(255) not null,
        user_id bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table tokens (
        id bigint not null auto_increment,
        alt_name varchar(255),
        english_name varchar(255) not null,
        name varchar(255) not null,
        ref_id integer not null,
        type varchar(255) not null,
        url varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table trackers (
        id integer not null auto_increment,
        name varchar(255),
        rest enum ('FULL','LONG','SHORT'),
        type enum ('Counter','Number','Roll'),
        `value` varchar(255),
        level_def_id integer,
        primary key (id)
    ) engine=InnoDB;

    create table trait_abilities (
        trait_id integer not null,
        ability enum ('CHARISMA','CHOICE','CHOICE_DOUBLE','CHOICE_UNIQUE','CONSTITUTION','DEXTERITY','INTELLIGENCE','ONE','STRENGTH','WISDOM') not null,
        sort_order integer not null,
        primary key (trait_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table trait_skills (
        trait_id integer not null,
        skill enum ('ACROBATICS','ANIMAL_HANDLING','ARCANA','ATHLETICS','DECEPTION','HISTORY','INSIGHT','INTIMIDATION','INVESTIGATION','MEDICINE','NATURE','PERCEPTION','PERFORMANCE','PERSUASION','RELIGION','SLEIGHT_OF_HAND','STEALTH','SURVIVAL') not null,
        sort_order integer not null,
        primary key (trait_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table traits (
        id integer not null auto_increment,
        alt_name varchar(255),
        description TEXT,
        english_name varchar(255),
        level integer,
        name varchar(255),
        requirement varchar(255),
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table treasures (
        id integer not null auto_increment,
        alt_name varchar(255),
        cost integer,
        english_name varchar(255),
        name varchar(255),
        page smallint,
        type enum ('AI_TRINKET','BAUBLE','COINS','EET_TRINKET','ELVEN1_TRINKET','ELVEN_TRINKET','GEM','GOTIC_BAUBLE','IDR_TRINKET','WORKS_OF_ART'),
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table user_excluded_sources (
        user_id bigint not null,
        source varchar(255) not null,
        primary key (user_id, source)
    ) engine=InnoDB;

    create table user_membership (
        user_party_id bigint not null,
        user_id bigint not null,
        sort_order integer not null,
        primary key (user_party_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table user_party (
        id bigint not null auto_increment,
        creation_date datetime(6),
        description varchar(255),
        group_name varchar(255),
        last_update_date datetime(6),
        owner_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table user_role (
        user_id bigint not null,
        role_id bigint not null,
        sort_order integer not null,
        primary key (user_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table user_sources (
        id bigint not null auto_increment,
        enabled bit,
        source varchar(255),
        user_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table user_wait_list (
        user_party_id bigint not null,
        user_id bigint not null,
        sort_order integer not null,
        primary key (user_party_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table users (
        id bigint not null auto_increment,
        create_date datetime(6),
        display_name varchar(24),
        email varchar(255),
        enabled bit,
        name varchar(255),
        password varchar(255),
        username varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table verification_token (
        id bigint not null auto_increment,
        expiry_date date,
        token varchar(255),
        user_id bigint not null,
        primary key (id)
    ) engine=InnoDB;

    create table weapon_properties (
        id integer not null auto_increment,
        description TEXT,
        english_name varchar(255),
        name varchar(255),
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table weapons (
        id integer not null auto_increment,
        alt_name varchar(255),
        ammo tinyint,
        cost integer,
        currency tinyint,
        damage_dice tinyint,
        damage_type tinyint,
        description TEXT,
        english_name varchar(255),
        max_distance smallint,
        min_distance smallint,
        name varchar(255),
        number_dice tinyint,
        special TEXT,
        two_hand_damage_dice tinyint,
        type tinyint,
        weight float(23) not null,
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table weapons_properties (
        weapon_id integer not null,
        properties_id integer not null,
        sort_order integer not null,
        primary key (weapon_id, sort_order),
        check ((sort_order>=0))
    ) engine=InnoDB;

    create table wild_magics (
        id integer not null auto_increment,
        description TEXT,
        source varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table youtube_videos (
        id varchar(12) not null,
        active bit not null,
        created datetime(6),
        name varchar(255),
        `order` integer,
        user_id bigint,
        primary key (id)
    ) engine=InnoDB;

    alter table archetype_spell 
       add constraint UK8eh943jx5mxl0vgyl43rqund2 unique (spell_id);

    alter table artifactes_armors 
       add constraint UKsmbpc24pq4y2x3nfgxdg5lg5b unique (armors_id);

    alter table artifactes_cust_classes 
       add constraint UKomubli09yroucsm0vogm8b4ik unique (cust_classes_id);

    alter table artifactes_weapons 
       add constraint UK51ltlbxmeuqb1y5yef3slcrx6 unique (weapons_id);

    alter table creature_spells 
       add constraint UKmuxqrgdwi5evti0ti54c5qkor unique (spell_id);

    alter table creatures 
       add constraint UKqvkx5gc32h1xwjhau5knj0rhd unique (lair_id);

    create index dm_screens_user_id_index 
       on dm_screens (user_id);

    alter table hero_class_feats_spells 
       add constraint UK72oiu962i4i3p7ch1ah0288gf unique (spells_id);

    create index initiative_participant_tracker_index 
       on initiative_participant (tracker_id);

    create index initiative_tracker_owner_username_index 
       on initiative_tracker (owner_username);

    alter table invitation 
       add constraint UKh4sn7fjfq9gsugdwdrkngsw8a unique (link, code);

    alter table invitation 
       add constraint UKib8hbu2lm2peqra478nwfgrrm unique (code);

    alter table invitation 
       add constraint UKkk94f8eq3ev4dmv9incx9b258 unique (link);

    alter table invitation 
       add constraint UKqh0llwffh2dr9lbvbukbf15eg unique (unique_identifier);

    alter table invitation 
       add constraint UKiyocct8x7mxk2pa785hvnfs0p unique (user_party_id);

    alter table race_names 
       add constraint UKgniieuio4gtw627iq9240g6p6 unique (name);

    alter table race_spells 
       add constraint UKh53w35u1xlrpjjos1x5ihulv0 unique (spells_id);

    alter table races 
       add constraint UKoq1k2oocjv0mjn7wi9a0je79u unique (english_name);

    alter table races_languages 
       add constraint UK7eua5yw9r1cri1pgm00cb6nye unique (languages_id);

    create index fn_index 
       on random_encounters (`start`, `end`, level, type);

    create index idx_revision_entity 
       on revision_history (entity_type, entity_id, revision);

    alter table roles 
       add constraint UKofx66keruapi6vyqpv6f2or37 unique (name);

    alter table spells 
       add constraint UKag3fukr2nt1fyt991bjwew78c unique (english_name);

    create index token_border_name_index 
       on token_borders (name);

    alter table token_borders 
       add constraint token_border_type_url_index unique (type, url);

    alter table token_borders 
       add constraint UK4qc3fpmnl6wk91009vgh0uh4y unique (url);

    create index token_multi_index 
       on tokens (name, alt_name, english_name);

    create index IDXitcx7uj2kf5myc8wdooj9juhq 
       on tokens (ref_id);

    alter table tokens 
       add constraint IDXddpjb0ra6p80m08q0uh83mouv unique (url);

    alter table treasures 
       add constraint UKi1w3ud5c4prps96rytynjw98a unique (english_name);

    alter table verification_token 
       add constraint UKq6jibbenp7o9v6tq178xg88hg unique (user_id);

    alter table archetype_spell 
       add constraint FK391hqgq1claarkwloogefqpdw 
       foreign key (spell_id) 
       references spells (id);

    alter table archetype_spell 
       add constraint FKa54cpgbnwyj57o15ps5h2b6re 
       foreign key (archetype_id) 
       references archetypes (id);

    alter table archetypes 
       add constraint FKg0dq1mw8dpa3688ddvxpoi6uq 
       foreign key (source) 
       references books (source);

    alter table archetypes 
       add constraint FKcdeqyu94otyp3sod7yf4plgpt 
       foreign key (class_id) 
       references classes (id);

    alter table armors 
       add constraint FKbfq5v6c7m7hr5w98yc1ycgwb8 
       foreign key (source) 
       references books (source);

    alter table artifactes 
       add constraint FK5xy4uqaks6ferxr8qn1gww5s2 
       foreign key (source) 
       references books (source);

    alter table artifactes_armors 
       add constraint FK5i5i8eu38fb6xcf06mixnyamn 
       foreign key (armors_id) 
       references armors (id);

    alter table artifactes_armors 
       add constraint FK91gehj049j319m80nyjrjivgj 
       foreign key (magic_item_id) 
       references artifactes (id);

    alter table artifactes_cust_classes 
       add constraint FKc8gt03v2ghgfn1ewrd7nfdn81 
       foreign key (cust_classes_id) 
       references classes (id);

    alter table artifactes_cust_classes 
       add constraint FK9kj56ucic7qpqp0lu190ft4dd 
       foreign key (magic_item_id) 
       references artifactes (id);

    alter table artifactes_weapons 
       add constraint FKl0elaumbmemngvpfnwku83kct 
       foreign key (weapons_id) 
       references weapons (id);

    alter table artifactes_weapons 
       add constraint FKoo9f1fs6u1h2o4npincc9e5ul 
       foreign key (magic_item_id) 
       references artifactes (id);

    alter table background_personalizations 
       add constraint FKh7jq2yxb2yijka8ppnfuujqdj 
       foreign key (background_id) 
       references backgrounds (id);

    alter table background_skill_type 
       add constraint FK1v45ona48t2qdvygder4gmgn7 
       foreign key (background_id) 
       references backgrounds (id);

    alter table backgrounds 
       add constraint FKjpmn88gbvkrlv4jmnc1kgubnf 
       foreign key (source) 
       references books (source);

    alter table backgrounds_languages 
       add constraint FKa2df8p58ckkwox83jo976oe7x 
       foreign key (languages_id) 
       references languages (id);

    alter table backgrounds_languages 
       add constraint FK1x6elbx0ik9agg9vrdjgeg1k 
       foreign key (background_id) 
       references backgrounds (id);

    alter table bookmarks 
       add constraint FKshv247db7uv8rcu2w3q5nhi77 
       foreign key (parent_id) 
       references bookmarks (uuid);

    alter table bookmarks 
       add constraint FKdbsho2e05w5r13fkjqfjmge5f 
       foreign key (user_id) 
       references users (id);

    alter table class_available_skills 
       add constraint FKaho1tnyikqnja8cpk6u075f52 
       foreign key (class_id) 
       references classes (id);

    alter table class_primary_abilities 
       add constraint FKn0m421nes5u2691uk8uk9nyji 
       foreign key (class_id) 
       references classes (id);

    alter table classes 
       add constraint FK6gkci61ea9j6yq9q0xammknrs 
       foreign key (source) 
       references books (source);

    alter table classes_feature_level_definitions 
       add constraint FK432a231hit6pfq6qbqylore0o 
       foreign key (hero_class_id) 
       references classes (id);

    alter table classes_feature_level_definitions 
       add constraint FKcx0s3br2ypltnbr7gyeql4cif 
       foreign key (archetype_id) 
       references archetypes (id);

    alter table conditions 
       add constraint FKirxc396pe5tqpgcdpq5gtj06q 
       foreign key (source) 
       references books (source);

    alter table creature_armor_types 
       add constraint FK5ps2s8ns2f2gqifwmgxwkfj23 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_feats 
       add constraint FKl4g19uhyg7qhog09mioep7sv4 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_habitates 
       add constraint FK3mefydcigpudtlq3d8u5a6evw 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_immunity_states 
       add constraint FKc0xaul4wpohafsglbnm595swf 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_immunity_damages 
       add constraint FKa5jtidv7gkp0p4oras72ifb9k 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_resistance_damages 
       add constraint FKl6y8x11k76t8y3xt7kt7c2ghw 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_saving_throws 
       add constraint FK8m80tp3qf9h2ar3p0f4wrdga2 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_skills 
       add constraint FKmtsn0xecxxfbwwvui8onx7b93 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_spellcaster 
       add constraint FKmvxfoq1m7xytdm3fqxuw8josw 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_spells 
       add constraint FKqetl114bccl1800u401hkmjky 
       foreign key (spell_id) 
       references spells (id);

    alter table creature_spells 
       add constraint FK6uuukak855h5rwjtpxbeqpnoc 
       foreign key (creature_id) 
       references creatures (id);

    alter table creature_vulnerability_damages 
       add constraint FKto43a3sd0flyusho2e5ftwg1k 
       foreign key (creature_id) 
       references creatures (id);

    alter table creatures 
       add constraint FKtdytg3s53in6mk0xtdsn0csp3 
       foreign key (source) 
       references books (source);

    alter table creatures 
       add constraint FKdu64s54n78vis9xe6mtm6d3je 
       foreign key (lair_id) 
       references lairs (lair_id);

    alter table creatures_actions 
       add constraint FKnhhp1o5sog3haaky88ag9n1kp 
       foreign key (actions_id) 
       references creature_actions (id);

    alter table creatures_actions 
       add constraint FK8d41s9r0h9fbd75s726hu86wi 
       foreign key (creature_id) 
       references creatures (id);

    alter table creatures_languages 
       add constraint FKti4d3h2xm3ovqi5g37exokadj 
       foreign key (languages_id) 
       references languages (id);

    alter table creatures_languages 
       add constraint FKpsu1oivmfjoq1biew7lm1bnfe 
       foreign key (creature_id) 
       references creatures (id);

    alter table creatures_races 
       add constraint FKmcl4vek15iynip6h69kyuoi8n 
       foreign key (races_id) 
       references creature_races (id);

    alter table creatures_races 
       add constraint FKlpy6o0v56hi00we0cgj4rqnja 
       foreign key (creature_id) 
       references creatures (id);

    alter table dm_screens 
       add constraint FKl9of2dkbn5sc940yc10fo3nwe 
       foreign key (user_id) 
       references users (id);

    alter table equipments 
       add constraint FKndpcoyd8re0248vwgbkstwcn8 
       foreign key (source) 
       references books (source);

    alter table equipments_types 
       add constraint FKciorsbn8f74da5ckc17j4st2f 
       foreign key (equipment_id) 
       references equipments (id);

    alter table god_domains 
       add constraint FK57qcx3qt9elttstj1swqgnig4 
       foreign key (god_id) 
       references gods (id);

    alter table gods 
       add constraint FKa33v3bah2ahy8c3lcl2voi1q8 
       foreign key (source) 
       references books (source);

    alter table gods 
       add constraint FKdjcufh3rgwgwfqgqxikawngs3 
       foreign key (pantheon_id) 
       references god_pantheons (id);

    alter table hero_class_feats 
       add constraint FK5vjog62t4ravuowhkf7axiqdp 
       foreign key (archetype_id) 
       references archetypes (id);

    alter table hero_class_feats 
       add constraint FKpme38ha3kxdqmj7awit4sk4cf 
       foreign key (source) 
       references books (source);

    alter table hero_class_feats_spells 
       add constraint FK54bhtvdu98us0adv5hygqo1o9 
       foreign key (spells_id) 
       references spells (id);

    alter table hero_class_feats_spells 
       add constraint FKf3kgg38dum2fub6yvmo17wq9t 
       foreign key (archetype_trait_id) 
       references hero_class_feats (id);

    alter table hero_class_traits 
       add constraint FK9kf706ehl36o45h5yyfxj9ct8 
       foreign key (source) 
       references books (source);

    alter table hero_class_traits 
       add constraint FKnxmf9aaw3fm3xmi0ix7unnhit 
       foreign key (hero_class_id) 
       references classes (id);

    alter table invitation 
       add constraint FKbrl60e7q4pi9y54qgca2g0otu 
       foreign key (user_party_id) 
       references user_party (id);

    alter table level_definitions 
       add constraint FKpa5e8iaewcpeueitbmdgg4t2y 
       foreign key (hero_class_id) 
       references classes (id);

    alter table level_definitions 
       add constraint FKe7mxj9q16derros838tgx230 
       foreign key (archetype_id) 
       references archetypes (id);

    alter table magic_things_random 
       add constraint FK7nfm2a3etawo8yfg0mnse6hwg 
       foreign key (magic_thing_id) 
       references artifactes (id);

    alter table menu 
       add constraint FKgeupubdqncc1lpgf2cn4fqwbc 
       foreign key (parent_id) 
       references menu (id);

    alter table name_owners 
       add constraint FKsjm907nkd21mmclb3ruhj6rkk 
       foreign key (parent_id) 
       references name_owners (id);

    alter table options 
       add constraint FK6trvhr3t4hke6kdigs0byvfgh 
       foreign key (source) 
       references books (source);

    alter table race_bonuses 
       add constraint FKh28mis4dvhlqr76b6fiom0ddf 
       foreign key (race_id) 
       references races (id);

    alter table race_feature_skill_type 
       add constraint FKmws6nj93m4hfh8gv83i7drfja 
       foreign key (feature_id) 
       references race_features (id);

    alter table race_features 
       add constraint FK1wiidtjjqgsmy5ffg7wfl0h2h 
       foreign key (race_id) 
       references races (id);

    alter table race_features_lanuages 
       add constraint FKcugwjnf9kijbbxr67v2ci5c5y 
       foreign key (lanuages_id) 
       references languages (id);

    alter table race_features_lanuages 
       add constraint FKg3rlu574ma7ah7vwxli0tnvef 
       foreign key (feature_id) 
       references race_features (id);

    alter table race_names 
       add constraint FKhxb1ieg3urrpjvrjdqt91gwce 
       foreign key (race_id) 
       references races (id);

    alter table race_nicknames 
       add constraint FKpl8s9hm3l8fshh49s2ydhmlfn 
       foreign key (race_id) 
       references races (id);

    alter table race_spells 
       add constraint FKmqemuianmkuxgekbsojof9eek 
       foreign key (spells_id) 
       references spells (id);

    alter table race_spells 
       add constraint FKnwn0w76sc1713fevi5aq2clry 
       foreign key (race_id) 
       references races (id);

    alter table races 
       add constraint FKeqgp4vwkw5jp0dulw21sn3kay 
       foreign key (source) 
       references books (source);

    alter table races 
       add constraint FKnt8oiohhein5ps6fmoqfhxjng 
       foreign key (parent_id) 
       references races (id);

    alter table races_languages 
       add constraint FKmxcl48ob0tbjy05wcyqv3b5qq 
       foreign key (languages_id) 
       references languages (id);

    alter table races_languages 
       add constraint FKbxlty3f1h994kah8s8nj7gr10 
       foreign key (race_id) 
       references races (id);

    alter table random_encounter_tables 
       add constraint FKg62ry3vbe44dkd6hdc50xtfiu 
       foreign key (source) 
       references books (source);

    alter table random_encounters 
       add constraint FK5jcp2sve2s2kgxuw3nkildaga 
       foreign key (source) 
       references books (source);

    alter table random_encounters 
       add constraint FKd95da2peiaafslttipo4uua0t 
       foreign key (encounter_id) 
       references random_encounter_tables (id);

    alter table rules 
       add constraint FKs3m2we40idk675g6jgcgug5kg 
       foreign key (source) 
       references books (source);

    alter table screens 
       add constraint FKlhyh8v4eeqr6dfsvidnol7c0e 
       foreign key (source) 
       references books (source);

    alter table screens 
       add constraint FKtp02ljuqq1xw33pk3ohgk6i6g 
       foreign key (parent_id) 
       references screens (id);

    alter table spell_damage_type 
       add constraint FK3qkx14nuceyxxugh7judjkhhj 
       foreign key (spell_id) 
       references spells (id);

    alter table spell_heal_type 
       add constraint FK5d03iyh2704x3wy81g5lmpga8 
       foreign key (spell_id) 
       references spells (id);

    alter table spell_savingthrows 
       add constraint FK3xu5l5iwlukcyjr2xn8tdxs2h 
       foreign key (spell_id) 
       references spells (id);

    alter table spell_source 
       add constraint FK16pr839oq3avm3j44etpu4bpl 
       foreign key (source) 
       references books (source);

    alter table spells 
       add constraint FKhalyvs3qve3voc96kwp8rf2dr 
       foreign key (source) 
       references books (source);

    alter table spells_hero_class 
       add constraint FKqo8toyt80nd2cm67cfw2btiv4 
       foreign key (hero_class_id) 
       references classes (id);

    alter table spells_hero_class 
       add constraint FK3wpc84e0k4r9ls0oxkhg8a1et 
       foreign key (spell_id) 
       references spells (id);

    alter table spells_times 
       add constraint FKpghkcnch06gfr7x3e9qbhwg0c 
       foreign key (spell_id) 
       references spells (id);

    alter table taverna_visitor_chances 
       add constraint FK9omad8qe8aqu8n5uaee0jiwet 
       foreign key (visitor_id) 
       references taverna_visitors (id);

    alter table taverna_visitor_habitates 
       add constraint FKag5qarx6kc8wej460jr0wga6u 
       foreign key (visitor_id) 
       references taverna_visitors (id);

    alter table trackers 
       add constraint FKcui9odc3hfmu10c8xa1n44o1f 
       foreign key (level_def_id) 
       references level_definitions (id);

    alter table trait_abilities 
       add constraint FK14dsek15e66cjms9q92da16hv 
       foreign key (trait_id) 
       references traits (id);

    alter table trait_skills 
       add constraint FKfr4esayba1wcxfo2lrpgj8fsa 
       foreign key (trait_id) 
       references traits (id);

    alter table traits 
       add constraint FKhg0mt1y5eqycedtm4ttr9xh08 
       foreign key (source) 
       references books (source);

    alter table treasures 
       add constraint FKmu8tsy05xuvi3p0btbw0uqk57 
       foreign key (source) 
       references books (source);

    alter table user_excluded_sources 
       add constraint FKkryr7ap9l7xng2bjca10l5i8k 
       foreign key (user_id) 
       references users (id);

    alter table user_membership 
       add constraint FKrcofdno3o5impia03lef6mcg3 
       foreign key (user_id) 
       references users (id);

    alter table user_membership 
       add constraint FKf6roppfi60h8fvmdu0htq7g5o 
       foreign key (user_party_id) 
       references user_party (id);

    alter table user_role 
       add constraint FKt7e7djp752sqn6w22i6ocqy6q 
       foreign key (role_id) 
       references roles (id);

    alter table user_role 
       add constraint FKj345gk1bovqvfame88rcx7yyx 
       foreign key (user_id) 
       references users (id);

    alter table user_sources 
       add constraint FKrv9pbfy81qxf35ownwhcll7mo 
       foreign key (source) 
       references books (source);

    alter table user_sources 
       add constraint FKikqanfs6yyui7nnbif1r491wd 
       foreign key (user_id) 
       references users (id);

    alter table user_wait_list 
       add constraint FKjpjgfh5946o480m711cnot57r 
       foreign key (user_id) 
       references users (id);

    alter table user_wait_list 
       add constraint FK7lqtkm6yahlhb7r0fiih47tja 
       foreign key (user_party_id) 
       references user_party (id);

    alter table verification_token 
       add constraint FK3asw9wnv76uxu3kr1ekq4i1ld 
       foreign key (user_id) 
       references users (id);

    alter table weapon_properties 
       add constraint FKc4rvlgrnunclnv8urismu07a 
       foreign key (source) 
       references books (source);

    alter table weapons 
       add constraint FKcj81i2cvf40gg4qd30uaejux9 
       foreign key (source) 
       references books (source);

    alter table weapons_properties 
       add constraint FK2fcchx5hfjj8b3vpwui90m018 
       foreign key (properties_id) 
       references weapon_properties (id);

    alter table weapons_properties 
       add constraint FK1cnhsx4fd7gi3ns4441n0933h 
       foreign key (weapon_id) 
       references weapons (id);

    alter table wild_magics 
       add constraint FKdae3lqea7tkx7ycr2sr1627bp 
       foreign key (source) 
       references books (source);

    alter table youtube_videos 
       add constraint FK4mx6k01ak5dfbtmbybc1672hf 
       foreign key (user_id) 
       references users (id);
