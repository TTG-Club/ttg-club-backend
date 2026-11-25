--liquibase formatted sql

--changeset pg-migration:0004-ix_archetype_spell_archetype_id
CREATE INDEX ix_archetype_spell_archetype_id ON archetype_spell (archetype_id);
--rollback DROP INDEX ix_archetype_spell_archetype_id;

--changeset pg-migration:0004-ix_archetype_spell_spell_id
CREATE INDEX ix_archetype_spell_spell_id ON archetype_spell (spell_id);
--rollback DROP INDEX ix_archetype_spell_spell_id;

--changeset pg-migration:0004-ix_archetypes_source
CREATE INDEX ix_archetypes_source ON archetypes (source);
--rollback DROP INDEX ix_archetypes_source;

--changeset pg-migration:0004-ix_archetypes_class_id
CREATE INDEX ix_archetypes_class_id ON archetypes (class_id);
--rollback DROP INDEX ix_archetypes_class_id;

--changeset pg-migration:0004-ix_armors_source
CREATE INDEX ix_armors_source ON armors (source);
--rollback DROP INDEX ix_armors_source;

--changeset pg-migration:0004-ux_armors_english_name
CREATE UNIQUE INDEX ux_armors_english_name ON armors (english_name);
--rollback DROP INDEX ux_armors_english_name;

--changeset pg-migration:0004-ix_artifactes_source
CREATE INDEX ix_artifactes_source ON artifactes (source);
--rollback DROP INDEX ix_artifactes_source;

--changeset pg-migration:0004-ix_artifactes_customization
CREATE INDEX ix_artifactes_customization ON artifactes (customization);
--rollback DROP INDEX ix_artifactes_customization;

--changeset pg-migration:0004-ux_artifactes_english_name
CREATE UNIQUE INDEX ux_artifactes_english_name ON artifactes (english_name);
--rollback DROP INDEX ux_artifactes_english_name;

--changeset pg-migration:0004-ix_artifactes_name_alt_name
CREATE INDEX ix_artifactes_name_alt_name ON artifactes (name, alt_name);
--rollback DROP INDEX ix_artifactes_name_alt_name;

--changeset pg-migration:0004-ix_artifactes_rarity_name
CREATE INDEX ix_artifactes_rarity_name ON artifactes (rarity, name);
--rollback DROP INDEX ix_artifactes_rarity_name;

--changeset pg-migration:0004-ix_artifactes_armors_magic_item_id
CREATE INDEX ix_artifactes_armors_magic_item_id ON artifactes_armors (magic_item_id);
--rollback DROP INDEX ix_artifactes_armors_magic_item_id;

--changeset pg-migration:0004-ix_artifactes_armors_armors_id
CREATE INDEX ix_artifactes_armors_armors_id ON artifactes_armors (armors_id);
--rollback DROP INDEX ix_artifactes_armors_armors_id;

--changeset pg-migration:0004-ix_artifactes_cust_classes_magic_item_id
CREATE INDEX ix_artifactes_cust_classes_magic_item_id ON artifactes_cust_classes (magic_item_id);
--rollback DROP INDEX ix_artifactes_cust_classes_magic_item_id;

--changeset pg-migration:0004-ix_artifactes_cust_classes_cust_classes_id
CREATE INDEX ix_artifactes_cust_classes_cust_classes_id ON artifactes_cust_classes (cust_classes_id);
--rollback DROP INDEX ix_artifactes_cust_classes_cust_classes_id;

--changeset pg-migration:0004-ix_artifactes_weapons_weapons_id
CREATE INDEX ix_artifactes_weapons_weapons_id ON artifactes_weapons (weapons_id);
--rollback DROP INDEX ix_artifactes_weapons_weapons_id;

--changeset pg-migration:0004-ix_artifactes_weapons_magic_item_id
CREATE INDEX ix_artifactes_weapons_magic_item_id ON artifactes_weapons (magic_item_id);
--rollback DROP INDEX ix_artifactes_weapons_magic_item_id;

--changeset pg-migration:0004-ix_background_personalizations_background_id
CREATE INDEX ix_background_personalizations_background_id ON background_personalizations (background_id);
--rollback DROP INDEX ix_background_personalizations_background_id;

--changeset pg-migration:0004-ix_background_skill_type_background_id
CREATE INDEX ix_background_skill_type_background_id ON background_skill_type (background_id);
--rollback DROP INDEX ix_background_skill_type_background_id;

--changeset pg-migration:0004-ix_backgrounds_source
CREATE INDEX ix_backgrounds_source ON backgrounds (source);
--rollback DROP INDEX ix_backgrounds_source;

--changeset pg-migration:0004-ux_backgrounds_english_name
CREATE UNIQUE INDEX ux_backgrounds_english_name ON backgrounds (english_name);
--rollback DROP INDEX ux_backgrounds_english_name;

--changeset pg-migration:0004-ix_backgrounds_name
CREATE INDEX ix_backgrounds_name ON backgrounds (name);
--rollback DROP INDEX ix_backgrounds_name;

--changeset pg-migration:0004-ix_backgrounds_languages_background_id
CREATE INDEX ix_backgrounds_languages_background_id ON backgrounds_languages (background_id);
--rollback DROP INDEX ix_backgrounds_languages_background_id;

--changeset pg-migration:0004-ix_backgrounds_languages_languages_id
CREATE INDEX ix_backgrounds_languages_languages_id ON backgrounds_languages (languages_id);
--rollback DROP INDEX ix_backgrounds_languages_languages_id;

--changeset pg-migration:0004-ix_bookmarks_parent_id
CREATE INDEX ix_bookmarks_parent_id ON bookmarks (parent_id);
--rollback DROP INDEX ix_bookmarks_parent_id;

--changeset pg-migration:0004-ix_class_available_skills_class_id
CREATE INDEX ix_class_available_skills_class_id ON class_available_skills (class_id);
--rollback DROP INDEX ix_class_available_skills_class_id;

--changeset pg-migration:0004-ix_class_primary_abilities_class_id
CREATE INDEX ix_class_primary_abilities_class_id ON class_primary_abilities (class_id);
--rollback DROP INDEX ix_class_primary_abilities_class_id;

--changeset pg-migration:0004-ix_classes_source
CREATE INDEX ix_classes_source ON classes (source);
--rollback DROP INDEX ix_classes_source;

--changeset pg-migration:0004-ix_classes_feature_level_definitions_hero_class_id
CREATE INDEX ix_classes_feature_level_definitions_hero_class_id ON classes_feature_level_definitions (hero_class_id);
--rollback DROP INDEX ix_classes_feature_level_definitions_hero_class_id;

--changeset pg-migration:0004-ix_classes_feature_level_definitions_archetype_id
CREATE INDEX ix_classes_feature_level_definitions_archetype_id ON classes_feature_level_definitions (archetype_id);
--rollback DROP INDEX ix_classes_feature_level_definitions_archetype_id;

--changeset pg-migration:0004-ix_conditions_source
CREATE INDEX ix_conditions_source ON conditions (source);
--rollback DROP INDEX ix_conditions_source;

--changeset pg-migration:0004-ix_creature_armor_types_creature_id
CREATE INDEX ix_creature_armor_types_creature_id ON creature_armor_types (creature_id);
--rollback DROP INDEX ix_creature_armor_types_creature_id;

--changeset pg-migration:0004-ix_creature_feats_creature_id
CREATE INDEX ix_creature_feats_creature_id ON creature_feats (creature_id);
--rollback DROP INDEX ix_creature_feats_creature_id;

--changeset pg-migration:0004-ux_creature_habitates_creature_id_habitates
CREATE UNIQUE INDEX ux_creature_habitates_creature_id_habitates ON creature_habitates (creature_id, habitates);
--rollback DROP INDEX ux_creature_habitates_creature_id_habitates;

--changeset pg-migration:0004-ix_creature_immunity_states_creature_id
CREATE INDEX ix_creature_immunity_states_creature_id ON creature_immunity_states (creature_id);
--rollback DROP INDEX ix_creature_immunity_states_creature_id;

--changeset pg-migration:0004-ix_creature_immunity_damages_creature_id
CREATE INDEX ix_creature_immunity_damages_creature_id ON creature_immunity_damages (creature_id);
--rollback DROP INDEX ix_creature_immunity_damages_creature_id;

--changeset pg-migration:0004-ux_creature_races_name
CREATE UNIQUE INDEX ux_creature_races_name ON creature_races (name);
--rollback DROP INDEX ux_creature_races_name;

--changeset pg-migration:0004-ix_creature_resistance_damages_creature_id
CREATE INDEX ix_creature_resistance_damages_creature_id ON creature_resistance_damages (creature_id);
--rollback DROP INDEX ix_creature_resistance_damages_creature_id;

--changeset pg-migration:0004-ix_creature_saving_throws_creature_id
CREATE INDEX ix_creature_saving_throws_creature_id ON creature_saving_throws (creature_id);
--rollback DROP INDEX ix_creature_saving_throws_creature_id;

--changeset pg-migration:0004-ix_creature_skills_creature_id
CREATE INDEX ix_creature_skills_creature_id ON creature_skills (creature_id);
--rollback DROP INDEX ix_creature_skills_creature_id;

--changeset pg-migration:0004-ix_creature_spellcaster_creature_id
CREATE INDEX ix_creature_spellcaster_creature_id ON creature_spellcaster (creature_id);
--rollback DROP INDEX ix_creature_spellcaster_creature_id;

--changeset pg-migration:0004-ix_creature_spells_creature_id
CREATE INDEX ix_creature_spells_creature_id ON creature_spells (creature_id);
--rollback DROP INDEX ix_creature_spells_creature_id;

--changeset pg-migration:0004-ix_creature_spells_spell_id
CREATE INDEX ix_creature_spells_spell_id ON creature_spells (spell_id);
--rollback DROP INDEX ix_creature_spells_spell_id;

--changeset pg-migration:0004-ix_creature_vulnerability_damages_creature_id
CREATE INDEX ix_creature_vulnerability_damages_creature_id ON creature_vulnerability_damages (creature_id);
--rollback DROP INDEX ix_creature_vulnerability_damages_creature_id;

--changeset pg-migration:0004-ix_creatures_source
CREATE INDEX ix_creatures_source ON creatures (source);
--rollback DROP INDEX ix_creatures_source;

--changeset pg-migration:0004-ux_creatures_english_name_source
CREATE UNIQUE INDEX ux_creatures_english_name_source ON creatures (english_name, source);
--rollback DROP INDEX ux_creatures_english_name_source;

--changeset pg-migration:0004-ix_creatures_exp_challenge_rating
CREATE INDEX ix_creatures_exp_challenge_rating ON creatures (exp, challenge_rating);
--rollback DROP INDEX ix_creatures_exp_challenge_rating;

--changeset pg-migration:0004-ix_creatures_lair_id
CREATE INDEX ix_creatures_lair_id ON creatures (lair_id);
--rollback DROP INDEX ix_creatures_lair_id;

--changeset pg-migration:0004-ix_creatures_name_alt_name
CREATE INDEX ix_creatures_name_alt_name ON creatures (name, alt_name);
--rollback DROP INDEX ix_creatures_name_alt_name;

--changeset pg-migration:0004-ix_creatures_race_id
CREATE INDEX ix_creatures_race_id ON creatures (race_id);
--rollback DROP INDEX ix_creatures_race_id;

--changeset pg-migration:0004-ix_creatures_size
CREATE INDEX ix_creatures_size ON creatures (size);
--rollback DROP INDEX ix_creatures_size;

--changeset pg-migration:0004-ix_creatures_type
CREATE INDEX ix_creatures_type ON creatures (type);
--rollback DROP INDEX ix_creatures_type;

--changeset pg-migration:0004-ix_creatures_actions_actions_id
CREATE INDEX ix_creatures_actions_actions_id ON creatures_actions (actions_id);
--rollback DROP INDEX ix_creatures_actions_actions_id;

--changeset pg-migration:0004-ix_creatures_actions_creature_id
CREATE INDEX ix_creatures_actions_creature_id ON creatures_actions (creature_id);
--rollback DROP INDEX ix_creatures_actions_creature_id;

--changeset pg-migration:0004-ix_creatures_languages_creature_id
CREATE INDEX ix_creatures_languages_creature_id ON creatures_languages (creature_id);
--rollback DROP INDEX ix_creatures_languages_creature_id;

--changeset pg-migration:0004-ix_creatures_languages_languages_id
CREATE INDEX ix_creatures_languages_languages_id ON creatures_languages (languages_id);
--rollback DROP INDEX ix_creatures_languages_languages_id;

--changeset pg-migration:0004-ix_creatures_races_creature_id
CREATE INDEX ix_creatures_races_creature_id ON creatures_races (creature_id);
--rollback DROP INDEX ix_creatures_races_creature_id;

--changeset pg-migration:0004-ix_creatures_races_races_id
CREATE INDEX ix_creatures_races_races_id ON creatures_races (races_id);
--rollback DROP INDEX ix_creatures_races_races_id;

--changeset pg-migration:0004-ix_dm_screens_user_id
CREATE INDEX ix_dm_screens_user_id ON dm_screens (user_id);
--rollback DROP INDEX ix_dm_screens_user_id;

--changeset pg-migration:0004-ix_equipments_cost
CREATE INDEX ix_equipments_cost ON equipments (cost);
--rollback DROP INDEX ix_equipments_cost;

--changeset pg-migration:0004-ux_equipments_english_name
CREATE UNIQUE INDEX ux_equipments_english_name ON equipments (english_name);
--rollback DROP INDEX ux_equipments_english_name;

--changeset pg-migration:0004-ix_equipments_name_alt_name
CREATE INDEX ix_equipments_name_alt_name ON equipments (name, alt_name);
--rollback DROP INDEX ix_equipments_name_alt_name;

--changeset pg-migration:0004-ix_equipments_source
CREATE INDEX ix_equipments_source ON equipments (source);
--rollback DROP INDEX ix_equipments_source;

--changeset pg-migration:0004-ix_equipments_types_equipment_id
CREATE INDEX ix_equipments_types_equipment_id ON equipments_types (equipment_id);
--rollback DROP INDEX ix_equipments_types_equipment_id;

--changeset pg-migration:0004-ix_god_domains_god_id
CREATE INDEX ix_god_domains_god_id ON god_domains (god_id);
--rollback DROP INDEX ix_god_domains_god_id;

--changeset pg-migration:0004-ux_gods_english_name
CREATE UNIQUE INDEX ux_gods_english_name ON gods (english_name);
--rollback DROP INDEX ux_gods_english_name;

--changeset pg-migration:0004-ux_gods_name_english_name
CREATE UNIQUE INDEX ux_gods_name_english_name ON gods (name, english_name);
--rollback DROP INDEX ux_gods_name_english_name;

--changeset pg-migration:0004-ix_gods_name_nicknames_alt_name
CREATE INDEX ix_gods_name_nicknames_alt_name ON gods (name, nicknames, alt_name);
--rollback DROP INDEX ix_gods_name_nicknames_alt_name;

--changeset pg-migration:0004-ix_gods_pantheon_id
CREATE INDEX ix_gods_pantheon_id ON gods (pantheon_id);
--rollback DROP INDEX ix_gods_pantheon_id;

--changeset pg-migration:0004-ix_gods_source
CREATE INDEX ix_gods_source ON gods (source);
--rollback DROP INDEX ix_gods_source;

--changeset pg-migration:0004-ix_hero_class_feats_source
CREATE INDEX ix_hero_class_feats_source ON hero_class_feats (source);
--rollback DROP INDEX ix_hero_class_feats_source;

--changeset pg-migration:0004-ix_hero_class_feats_archetype_id
CREATE INDEX ix_hero_class_feats_archetype_id ON hero_class_feats (archetype_id);
--rollback DROP INDEX ix_hero_class_feats_archetype_id;

--changeset pg-migration:0004-ix_hero_class_feats_name_alt_name_english_name
CREATE INDEX ix_hero_class_feats_name_alt_name_english_name ON hero_class_feats (name, alt_name, english_name);
--rollback DROP INDEX ix_hero_class_feats_name_alt_name_english_name;

--changeset pg-migration:0004-ix_hero_class_feats_spells_archetype_trait_id
CREATE INDEX ix_hero_class_feats_spells_archetype_trait_id ON hero_class_feats_spells (archetype_trait_id);
--rollback DROP INDEX ix_hero_class_feats_spells_archetype_trait_id;

--changeset pg-migration:0004-ix_hero_class_traits_source
CREATE INDEX ix_hero_class_traits_source ON hero_class_traits (source);
--rollback DROP INDEX ix_hero_class_traits_source;

--changeset pg-migration:0004-ix_hero_class_traits_hero_class_id
CREATE INDEX ix_hero_class_traits_hero_class_id ON hero_class_traits (hero_class_id);
--rollback DROP INDEX ix_hero_class_traits_hero_class_id;

--changeset pg-migration:0004-ix_hero_madness_type
CREATE INDEX ix_hero_madness_type ON hero_madness (type);
--rollback DROP INDEX ix_hero_madness_type;

--changeset pg-migration:0004-ix_images_ref_id_type
CREATE INDEX ix_images_ref_id_type ON images (ref_id, type);
--rollback DROP INDEX ix_images_ref_id_type;

--changeset pg-migration:0004-ix_initiative_participant_tracker_id
CREATE INDEX ix_initiative_participant_tracker_id ON initiative_participant (tracker_id);
--rollback DROP INDEX ix_initiative_participant_tracker_id;

--changeset pg-migration:0004-ix_initiative_tracker_owner_username
CREATE INDEX ix_initiative_tracker_owner_username ON initiative_tracker (owner_username);
--rollback DROP INDEX ix_initiative_tracker_owner_username;

--changeset pg-migration:0004-ix_invitation_user_party_id
CREATE INDEX ix_invitation_user_party_id ON invitation (user_party_id);
--rollback DROP INDEX ix_invitation_user_party_id;

--changeset pg-migration:0004-ix_level_definitions_archetype_id
CREATE INDEX ix_level_definitions_archetype_id ON level_definitions (archetype_id);
--rollback DROP INDEX ix_level_definitions_archetype_id;

--changeset pg-migration:0004-ix_level_definitions_hero_class_id
CREATE INDEX ix_level_definitions_hero_class_id ON level_definitions (hero_class_id);
--rollback DROP INDEX ix_level_definitions_hero_class_id;

--changeset pg-migration:0004-ix_magic_things_random_magic_thing_id
CREATE INDEX ix_magic_things_random_magic_thing_id ON magic_things_random (magic_thing_id);
--rollback DROP INDEX ix_magic_things_random_magic_thing_id;

--changeset pg-migration:0004-ix_menu_parent_id
CREATE INDEX ix_menu_parent_id ON menu (parent_id);
--rollback DROP INDEX ix_menu_parent_id;

--changeset pg-migration:0004-ix_name_owners_parent_id
CREATE INDEX ix_name_owners_parent_id ON name_owners (parent_id);
--rollback DROP INDEX ix_name_owners_parent_id;

--changeset pg-migration:0004-ux_options_english_name
CREATE UNIQUE INDEX ux_options_english_name ON options (english_name);
--rollback DROP INDEX ux_options_english_name;

--changeset pg-migration:0004-ix_options_level
CREATE INDEX ix_options_level ON options (level);
--rollback DROP INDEX ix_options_level;

--changeset pg-migration:0004-ix_options_name_alt_name
CREATE INDEX ix_options_name_alt_name ON options (name, alt_name);
--rollback DROP INDEX ix_options_name_alt_name;

--changeset pg-migration:0004-ix_options_prerequisite
CREATE INDEX ix_options_prerequisite ON options (prerequisite);
--rollback DROP INDEX ix_options_prerequisite;

--changeset pg-migration:0004-ix_options_source
CREATE INDEX ix_options_source ON options (source);
--rollback DROP INDEX ix_options_source;

--changeset pg-migration:0004-ix_race_bonuses_race_id
CREATE INDEX ix_race_bonuses_race_id ON race_bonuses (race_id);
--rollback DROP INDEX ix_race_bonuses_race_id;

--changeset pg-migration:0004-ix_race_feature_skill_type_feature_id
CREATE INDEX ix_race_feature_skill_type_feature_id ON race_feature_skill_type (feature_id);
--rollback DROP INDEX ix_race_feature_skill_type_feature_id;

--changeset pg-migration:0004-ix_race_features_race_id
CREATE INDEX ix_race_features_race_id ON race_features (race_id);
--rollback DROP INDEX ix_race_features_race_id;

--changeset pg-migration:0004-ix_race_features_lanuages_lanuages_id
CREATE INDEX ix_race_features_lanuages_lanuages_id ON race_features_lanuages (lanuages_id);
--rollback DROP INDEX ix_race_features_lanuages_lanuages_id;

--changeset pg-migration:0004-ix_race_features_lanuages_feature_id
CREATE INDEX ix_race_features_lanuages_feature_id ON race_features_lanuages (feature_id);
--rollback DROP INDEX ix_race_features_lanuages_feature_id;

--changeset pg-migration:0004-ix_race_names_race_id
CREATE INDEX ix_race_names_race_id ON race_names (race_id);
--rollback DROP INDEX ix_race_names_race_id;

--changeset pg-migration:0004-ix_race_nicknames_race_id
CREATE INDEX ix_race_nicknames_race_id ON race_nicknames (race_id);
--rollback DROP INDEX ix_race_nicknames_race_id;

--changeset pg-migration:0004-ix_race_spells_spells_id
CREATE INDEX ix_race_spells_spells_id ON race_spells (spells_id);
--rollback DROP INDEX ix_race_spells_spells_id;

--changeset pg-migration:0004-ix_races_source
CREATE INDEX ix_races_source ON races (source);
--rollback DROP INDEX ix_races_source;

--changeset pg-migration:0004-ix_races_english_name
CREATE INDEX ix_races_english_name ON races (english_name);
--rollback DROP INDEX ix_races_english_name;

--changeset pg-migration:0004-ix_races_name_alt_name
CREATE INDEX ix_races_name_alt_name ON races (name, alt_name);
--rollback DROP INDEX ix_races_name_alt_name;

--changeset pg-migration:0004-ix_races_parent_id
CREATE INDEX ix_races_parent_id ON races (parent_id);
--rollback DROP INDEX ix_races_parent_id;

--changeset pg-migration:0004-ix_races_languages_race_id
CREATE INDEX ix_races_languages_race_id ON races_languages (race_id);
--rollback DROP INDEX ix_races_languages_race_id;

--changeset pg-migration:0004-ix_random_encounters_source
CREATE INDEX ix_random_encounters_source ON random_encounters (source);
--rollback DROP INDEX ix_random_encounters_source;

--changeset pg-migration:0004-ix_random_encounters_encounter_id
CREATE INDEX ix_random_encounters_encounter_id ON random_encounters (encounter_id);
--rollback DROP INDEX ix_random_encounters_encounter_id;

--changeset pg-migration:0004-ix_random_encounters_start_end_level_type
CREATE INDEX ix_random_encounters_start_end_level_type ON random_encounters ("start", "end", level, type);
--rollback DROP INDEX ix_random_encounters_start_end_level_type;

--changeset pg-migration:0004-ix_revision_history_entity_type_entity_id_revision
CREATE INDEX ix_revision_history_entity_type_entity_id_revision ON revision_history (entity_type, entity_id, revision);
--rollback DROP INDEX ix_revision_history_entity_type_entity_id_revision;

--changeset pg-migration:0004-ux_rules_english_name
CREATE UNIQUE INDEX ux_rules_english_name ON rules (english_name);
--rollback DROP INDEX ux_rules_english_name;

--changeset pg-migration:0004-ix_rules_name_english_name
CREATE INDEX ix_rules_name_english_name ON rules (name, english_name);
--rollback DROP INDEX ix_rules_name_english_name;

--changeset pg-migration:0004-ix_rules_source
CREATE INDEX ix_rules_source ON rules (source);
--rollback DROP INDEX ix_rules_source;

--changeset pg-migration:0004-ix_rules_type
CREATE INDEX ix_rules_type ON rules (type);
--rollback DROP INDEX ix_rules_type;

--changeset pg-migration:0004-ux_screens_english_name
CREATE UNIQUE INDEX ux_screens_english_name ON screens (english_name);
--rollback DROP INDEX ux_screens_english_name;

--changeset pg-migration:0004-ix_screens_name_alt_name
CREATE INDEX ix_screens_name_alt_name ON screens (name, alt_name);
--rollback DROP INDEX ix_screens_name_alt_name;

--changeset pg-migration:0004-ix_screens_parent_id
CREATE INDEX ix_screens_parent_id ON screens (parent_id);
--rollback DROP INDEX ix_screens_parent_id;

--changeset pg-migration:0004-ix_screens_source
CREATE INDEX ix_screens_source ON screens (source);
--rollback DROP INDEX ix_screens_source;

--changeset pg-migration:0004-ix_spell_damage_type_spell_id
CREATE INDEX ix_spell_damage_type_spell_id ON spell_damage_type (spell_id);
--rollback DROP INDEX ix_spell_damage_type_spell_id;

--changeset pg-migration:0004-ix_spell_heal_type_spell_id
CREATE INDEX ix_spell_heal_type_spell_id ON spell_heal_type (spell_id);
--rollback DROP INDEX ix_spell_heal_type_spell_id;

--changeset pg-migration:0004-ix_spell_savingthrows_spell_id
CREATE INDEX ix_spell_savingthrows_spell_id ON spell_savingthrows (spell_id);
--rollback DROP INDEX ix_spell_savingthrows_spell_id;

--changeset pg-migration:0004-ix_spells_concentration
CREATE INDEX ix_spells_concentration ON spells (concentration);
--rollback DROP INDEX ix_spells_concentration;

--changeset pg-migration:0004-ix_spells_level
CREATE INDEX ix_spells_level ON spells (level);
--rollback DROP INDEX ix_spells_level;

--changeset pg-migration:0004-ix_spells_name_alt_name
CREATE INDEX ix_spells_name_alt_name ON spells (name, alt_name);
--rollback DROP INDEX ix_spells_name_alt_name;

--changeset pg-migration:0004-ix_spells_ritual
CREATE INDEX ix_spells_ritual ON spells (ritual);
--rollback DROP INDEX ix_spells_ritual;

--changeset pg-migration:0004-ix_spells_source
CREATE INDEX ix_spells_source ON spells (source);
--rollback DROP INDEX ix_spells_source;

--changeset pg-migration:0004-ix_spells_hero_class_hero_class_id
CREATE INDEX ix_spells_hero_class_hero_class_id ON spells_hero_class (hero_class_id);
--rollback DROP INDEX ix_spells_hero_class_hero_class_id;

--changeset pg-migration:0004-ix_spells_hero_class_spell_id
CREATE INDEX ix_spells_hero_class_spell_id ON spells_hero_class (spell_id);
--rollback DROP INDEX ix_spells_hero_class_spell_id;

--changeset pg-migration:0004-ux_spells_hero_class_spell_id_hero_class_id
CREATE UNIQUE INDEX ux_spells_hero_class_spell_id_hero_class_id ON spells_hero_class (spell_id, hero_class_id);
--rollback DROP INDEX ux_spells_hero_class_spell_id_hero_class_id;

--changeset pg-migration:0004-ix_spells_times_spell_id
CREATE INDEX ix_spells_times_spell_id ON spells_times (spell_id);
--rollback DROP INDEX ix_spells_times_spell_id;

--changeset pg-migration:0004-ux_taverna_names_name
CREATE UNIQUE INDEX ux_taverna_names_name ON taverna_names (name);
--rollback DROP INDEX ux_taverna_names_name;

--changeset pg-migration:0004-ux_taverna_prefixes_name
CREATE UNIQUE INDEX ux_taverna_prefixes_name ON taverna_prefixes (name);
--rollback DROP INDEX ux_taverna_prefixes_name;

--changeset pg-migration:0004-ix_taverna_visitor_chances_visitor_id
CREATE INDEX ix_taverna_visitor_chances_visitor_id ON taverna_visitor_chances (visitor_id);
--rollback DROP INDEX ix_taverna_visitor_chances_visitor_id;

--changeset pg-migration:0004-ix_taverna_visitor_habitates_visitor_id
CREATE INDEX ix_taverna_visitor_habitates_visitor_id ON taverna_visitor_habitates (visitor_id);
--rollback DROP INDEX ix_taverna_visitor_habitates_visitor_id;

--changeset pg-migration:0004-ix_token_borders_name
CREATE INDEX ix_token_borders_name ON token_borders (name);
--rollback DROP INDEX ix_token_borders_name;

--changeset pg-migration:0004-ux_token_borders_type_url
CREATE UNIQUE INDEX ux_token_borders_type_url ON token_borders (type, url);
--rollback DROP INDEX ux_token_borders_type_url;

--changeset pg-migration:0004-ix_tokens_ref_id
CREATE INDEX ix_tokens_ref_id ON tokens (ref_id);
--rollback DROP INDEX ix_tokens_ref_id;

--changeset pg-migration:0004-ix_tokens_name_alt_name_english_name
CREATE INDEX ix_tokens_name_alt_name_english_name ON tokens (name, alt_name, english_name);
--rollback DROP INDEX ix_tokens_name_alt_name_english_name;

--changeset pg-migration:0004-ix_trackers_level_def_id
CREATE INDEX ix_trackers_level_def_id ON trackers (level_def_id);
--rollback DROP INDEX ix_trackers_level_def_id;

--changeset pg-migration:0004-ix_trait_abilities_trait_id
CREATE INDEX ix_trait_abilities_trait_id ON trait_abilities (trait_id);
--rollback DROP INDEX ix_trait_abilities_trait_id;

--changeset pg-migration:0004-ix_trait_skills_trait_id
CREATE INDEX ix_trait_skills_trait_id ON trait_skills (trait_id);
--rollback DROP INDEX ix_trait_skills_trait_id;

--changeset pg-migration:0004-ux_traits_english_name_source
CREATE UNIQUE INDEX ux_traits_english_name_source ON traits (english_name, source);
--rollback DROP INDEX ux_traits_english_name_source;

--changeset pg-migration:0004-ix_traits_name_alt_name
CREATE INDEX ix_traits_name_alt_name ON traits (name, alt_name);
--rollback DROP INDEX ix_traits_name_alt_name;

--changeset pg-migration:0004-ix_traits_source
CREATE INDEX ix_traits_source ON traits (source);
--rollback DROP INDEX ix_traits_source;

--changeset pg-migration:0004-ix_treasures_source
CREATE INDEX ix_treasures_source ON treasures (source);
--rollback DROP INDEX ix_treasures_source;

--changeset pg-migration:0004-ix_user_membership_user_party_id
CREATE INDEX ix_user_membership_user_party_id ON user_membership (user_party_id);
--rollback DROP INDEX ix_user_membership_user_party_id;

--changeset pg-migration:0004-ix_user_role_role_id
CREATE INDEX ix_user_role_role_id ON user_role (role_id);
--rollback DROP INDEX ix_user_role_role_id;

--changeset pg-migration:0004-ix_user_role_user_id
CREATE INDEX ix_user_role_user_id ON user_role (user_id);
--rollback DROP INDEX ix_user_role_user_id;

--changeset pg-migration:0004-ix_user_wait_list_user_party_id
CREATE INDEX ix_user_wait_list_user_party_id ON user_wait_list (user_party_id);
--rollback DROP INDEX ix_user_wait_list_user_party_id;

--changeset pg-migration:0004-ux_users_email
CREATE UNIQUE INDEX ux_users_email ON users (email);
--rollback DROP INDEX ux_users_email;

--changeset pg-migration:0004-ux_users_name
CREATE UNIQUE INDEX ux_users_name ON users (name);
--rollback DROP INDEX ux_users_name;

--changeset pg-migration:0004-ix_verification_token_user_id
CREATE INDEX ix_verification_token_user_id ON verification_token (user_id);
--rollback DROP INDEX ix_verification_token_user_id;

--changeset pg-migration:0004-ix_weapon_properties_source
CREATE INDEX ix_weapon_properties_source ON weapon_properties (source);
--rollback DROP INDEX ix_weapon_properties_source;

--changeset pg-migration:0004-ix_weapons_damage_type
CREATE INDEX ix_weapons_damage_type ON weapons (damage_type);
--rollback DROP INDEX ix_weapons_damage_type;

--changeset pg-migration:0004-ux_weapons_english_name
CREATE UNIQUE INDEX ux_weapons_english_name ON weapons (english_name);
--rollback DROP INDEX ux_weapons_english_name;

--changeset pg-migration:0004-ix_weapons_name_alt_name
CREATE INDEX ix_weapons_name_alt_name ON weapons (name, alt_name);
--rollback DROP INDEX ix_weapons_name_alt_name;

--changeset pg-migration:0004-ix_weapons_source
CREATE INDEX ix_weapons_source ON weapons (source);
--rollback DROP INDEX ix_weapons_source;

--changeset pg-migration:0004-ix_weapons_properties_weapon_id
CREATE INDEX ix_weapons_properties_weapon_id ON weapons_properties (weapon_id);
--rollback DROP INDEX ix_weapons_properties_weapon_id;

--changeset pg-migration:0004-ix_weapons_properties_properties_id
CREATE INDEX ix_weapons_properties_properties_id ON weapons_properties (properties_id);
--rollback DROP INDEX ix_weapons_properties_properties_id;

--changeset pg-migration:0004-ix_wild_magics_source
CREATE INDEX ix_wild_magics_source ON wild_magics (source);
--rollback DROP INDEX ix_wild_magics_source;

--changeset pg-migration:0004-ix_youtube_videos_user_id
CREATE INDEX ix_youtube_videos_user_id ON youtube_videos (user_id);
--rollback DROP INDEX ix_youtube_videos_user_id;

--changeset pg-migration:0004-ix_bookmarks_user_id
CREATE INDEX ix_bookmarks_user_id ON bookmarks (user_id);
--rollback DROP INDEX ix_bookmarks_user_id;

--changeset pg-migration:0004-ix_hero_class_feats_spells_spells_id
CREATE INDEX ix_hero_class_feats_spells_spells_id ON hero_class_feats_spells (spells_id);
--rollback DROP INDEX ix_hero_class_feats_spells_spells_id;

--changeset pg-migration:0004-ix_races_languages_languages_id
CREATE INDEX ix_races_languages_languages_id ON races_languages (languages_id);
--rollback DROP INDEX ix_races_languages_languages_id;

--changeset pg-migration:0004-ix_random_encounter_tables_source
CREATE INDEX ix_random_encounter_tables_source ON random_encounter_tables (source);
--rollback DROP INDEX ix_random_encounter_tables_source;

--changeset pg-migration:0004-ix_spell_source_source
CREATE INDEX ix_spell_source_source ON spell_source (source);
--rollback DROP INDEX ix_spell_source_source;

--changeset pg-migration:0004-ix_user_membership_user_id
CREATE INDEX ix_user_membership_user_id ON user_membership (user_id);
--rollback DROP INDEX ix_user_membership_user_id;

--changeset pg-migration:0004-ix_user_sources_source
CREATE INDEX ix_user_sources_source ON user_sources (source);
--rollback DROP INDEX ix_user_sources_source;

--changeset pg-migration:0004-ix_user_sources_user_id
CREATE INDEX ix_user_sources_user_id ON user_sources (user_id);
--rollback DROP INDEX ix_user_sources_user_id;

--changeset pg-migration:0004-ix_user_wait_list_user_id
CREATE INDEX ix_user_wait_list_user_id ON user_wait_list (user_id);
--rollback DROP INDEX ix_user_wait_list_user_id;
