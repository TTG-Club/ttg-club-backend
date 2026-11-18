--liquibase formatted sql

--changeset pg-migration:0003-fk-FK391hqgq1claarkwloogefqpdw
ALTER TABLE archetype_spell ADD CONSTRAINT FK391hqgq1claarkwloogefqpdw FOREIGN KEY (spell_id) REFERENCES spells (id);
--rollback ALTER TABLE archetype_spell DROP CONSTRAINT FK391hqgq1claarkwloogefqpdw;

--changeset pg-migration:0003-fk-FKa54cpgbnwyj57o15ps5h2b6re
ALTER TABLE archetype_spell ADD CONSTRAINT FKa54cpgbnwyj57o15ps5h2b6re FOREIGN KEY (archetype_id) REFERENCES archetypes (id);
--rollback ALTER TABLE archetype_spell DROP CONSTRAINT FKa54cpgbnwyj57o15ps5h2b6re;

--changeset pg-migration:0003-fk-FKg0dq1mw8dpa3688ddvxpoi6uq
ALTER TABLE archetypes ADD CONSTRAINT FKg0dq1mw8dpa3688ddvxpoi6uq FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE archetypes DROP CONSTRAINT FKg0dq1mw8dpa3688ddvxpoi6uq;

--changeset pg-migration:0003-fk-FKcdeqyu94otyp3sod7yf4plgpt
ALTER TABLE archetypes ADD CONSTRAINT FKcdeqyu94otyp3sod7yf4plgpt FOREIGN KEY (class_id) REFERENCES classes (id);
--rollback ALTER TABLE archetypes DROP CONSTRAINT FKcdeqyu94otyp3sod7yf4plgpt;

--changeset pg-migration:0003-fk-FKbfq5v6c7m7hr5w98yc1ycgwb8
ALTER TABLE armors ADD CONSTRAINT FKbfq5v6c7m7hr5w98yc1ycgwb8 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE armors DROP CONSTRAINT FKbfq5v6c7m7hr5w98yc1ycgwb8;

--changeset pg-migration:0003-fk-FK5xy4uqaks6ferxr8qn1gww5s2
ALTER TABLE artifactes ADD CONSTRAINT FK5xy4uqaks6ferxr8qn1gww5s2 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE artifactes DROP CONSTRAINT FK5xy4uqaks6ferxr8qn1gww5s2;

--changeset pg-migration:0003-fk-FK5i5i8eu38fb6xcf06mixnyamn
ALTER TABLE artifactes_armors ADD CONSTRAINT FK5i5i8eu38fb6xcf06mixnyamn FOREIGN KEY (armors_id) REFERENCES armors (id);
--rollback ALTER TABLE artifactes_armors DROP CONSTRAINT FK5i5i8eu38fb6xcf06mixnyamn;

--changeset pg-migration:0003-fk-FK91gehj049j319m80nyjrjivgj
ALTER TABLE artifactes_armors ADD CONSTRAINT FK91gehj049j319m80nyjrjivgj FOREIGN KEY (magic_item_id) REFERENCES artifactes (id);
--rollback ALTER TABLE artifactes_armors DROP CONSTRAINT FK91gehj049j319m80nyjrjivgj;

--changeset pg-migration:0003-fk-FKc8gt03v2ghgfn1ewrd7nfdn81
ALTER TABLE artifactes_cust_classes ADD CONSTRAINT FKc8gt03v2ghgfn1ewrd7nfdn81 FOREIGN KEY (cust_classes_id) REFERENCES classes (id);
--rollback ALTER TABLE artifactes_cust_classes DROP CONSTRAINT FKc8gt03v2ghgfn1ewrd7nfdn81;

--changeset pg-migration:0003-fk-FK9kj56ucic7qpqp0lu190ft4dd
ALTER TABLE artifactes_cust_classes ADD CONSTRAINT FK9kj56ucic7qpqp0lu190ft4dd FOREIGN KEY (magic_item_id) REFERENCES artifactes (id);
--rollback ALTER TABLE artifactes_cust_classes DROP CONSTRAINT FK9kj56ucic7qpqp0lu190ft4dd;

--changeset pg-migration:0003-fk-FKl0elaumbmemngvpfnwku83kct
ALTER TABLE artifactes_weapons ADD CONSTRAINT FKl0elaumbmemngvpfnwku83kct FOREIGN KEY (weapons_id) REFERENCES weapons (id);
--rollback ALTER TABLE artifactes_weapons DROP CONSTRAINT FKl0elaumbmemngvpfnwku83kct;

--changeset pg-migration:0003-fk-FKoo9f1fs6u1h2o4npincc9e5ul
ALTER TABLE artifactes_weapons ADD CONSTRAINT FKoo9f1fs6u1h2o4npincc9e5ul FOREIGN KEY (magic_item_id) REFERENCES artifactes (id);
--rollback ALTER TABLE artifactes_weapons DROP CONSTRAINT FKoo9f1fs6u1h2o4npincc9e5ul;

--changeset pg-migration:0003-fk-FKh7jq2yxb2yijka8ppnfuujqdj
ALTER TABLE background_personalizations ADD CONSTRAINT FKh7jq2yxb2yijka8ppnfuujqdj FOREIGN KEY (background_id) REFERENCES backgrounds (id);
--rollback ALTER TABLE background_personalizations DROP CONSTRAINT FKh7jq2yxb2yijka8ppnfuujqdj;

--changeset pg-migration:0003-fk-FK1v45ona48t2qdvygder4gmgn7
ALTER TABLE background_skill_type ADD CONSTRAINT FK1v45ona48t2qdvygder4gmgn7 FOREIGN KEY (background_id) REFERENCES backgrounds (id);
--rollback ALTER TABLE background_skill_type DROP CONSTRAINT FK1v45ona48t2qdvygder4gmgn7;

--changeset pg-migration:0003-fk-FKjpmn88gbvkrlv4jmnc1kgubnf
ALTER TABLE backgrounds ADD CONSTRAINT FKjpmn88gbvkrlv4jmnc1kgubnf FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE backgrounds DROP CONSTRAINT FKjpmn88gbvkrlv4jmnc1kgubnf;

--changeset pg-migration:0003-fk-FKa2df8p58ckkwox83jo976oe7x
ALTER TABLE backgrounds_languages ADD CONSTRAINT FKa2df8p58ckkwox83jo976oe7x FOREIGN KEY (languages_id) REFERENCES languages (id);
--rollback ALTER TABLE backgrounds_languages DROP CONSTRAINT FKa2df8p58ckkwox83jo976oe7x;

--changeset pg-migration:0003-fk-FK1x6elbx0ik9agg9vrdjgeg1k
ALTER TABLE backgrounds_languages ADD CONSTRAINT FK1x6elbx0ik9agg9vrdjgeg1k FOREIGN KEY (background_id) REFERENCES backgrounds (id);
--rollback ALTER TABLE backgrounds_languages DROP CONSTRAINT FK1x6elbx0ik9agg9vrdjgeg1k;

--changeset pg-migration:0003-fk-FKshv247db7uv8rcu2w3q5nhi77
ALTER TABLE bookmarks ADD CONSTRAINT FKshv247db7uv8rcu2w3q5nhi77 FOREIGN KEY (parent_id) REFERENCES bookmarks (uuid);
--rollback ALTER TABLE bookmarks DROP CONSTRAINT FKshv247db7uv8rcu2w3q5nhi77;

--changeset pg-migration:0003-fk-FKdbsho2e05w5r13fkjqfjmge5f
ALTER TABLE bookmarks ADD CONSTRAINT FKdbsho2e05w5r13fkjqfjmge5f FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE bookmarks DROP CONSTRAINT FKdbsho2e05w5r13fkjqfjmge5f;

--changeset pg-migration:0003-fk-FKaho1tnyikqnja8cpk6u075f52
ALTER TABLE class_available_skills ADD CONSTRAINT FKaho1tnyikqnja8cpk6u075f52 FOREIGN KEY (class_id) REFERENCES classes (id);
--rollback ALTER TABLE class_available_skills DROP CONSTRAINT FKaho1tnyikqnja8cpk6u075f52;

--changeset pg-migration:0003-fk-FKn0m421nes5u2691uk8uk9nyji
ALTER TABLE class_primary_abilities ADD CONSTRAINT FKn0m421nes5u2691uk8uk9nyji FOREIGN KEY (class_id) REFERENCES classes (id);
--rollback ALTER TABLE class_primary_abilities DROP CONSTRAINT FKn0m421nes5u2691uk8uk9nyji;

--changeset pg-migration:0003-fk-FK6gkci61ea9j6yq9q0xammknrs
ALTER TABLE classes ADD CONSTRAINT FK6gkci61ea9j6yq9q0xammknrs FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE classes DROP CONSTRAINT FK6gkci61ea9j6yq9q0xammknrs;

--changeset pg-migration:0003-fk-FK432a231hit6pfq6qbqylore0o
ALTER TABLE classes_feature_level_definitions ADD CONSTRAINT FK432a231hit6pfq6qbqylore0o FOREIGN KEY (hero_class_id) REFERENCES classes (id);
--rollback ALTER TABLE classes_feature_level_definitions DROP CONSTRAINT FK432a231hit6pfq6qbqylore0o;

--changeset pg-migration:0003-fk-FKcx0s3br2ypltnbr7gyeql4cif
ALTER TABLE classes_feature_level_definitions ADD CONSTRAINT FKcx0s3br2ypltnbr7gyeql4cif FOREIGN KEY (archetype_id) REFERENCES archetypes (id);
--rollback ALTER TABLE classes_feature_level_definitions DROP CONSTRAINT FKcx0s3br2ypltnbr7gyeql4cif;

--changeset pg-migration:0003-fk-FKirxc396pe5tqpgcdpq5gtj06q
ALTER TABLE conditions ADD CONSTRAINT FKirxc396pe5tqpgcdpq5gtj06q FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE conditions DROP CONSTRAINT FKirxc396pe5tqpgcdpq5gtj06q;

--changeset pg-migration:0003-fk-FK5ps2s8ns2f2gqifwmgxwkfj23
ALTER TABLE creature_armor_types ADD CONSTRAINT FK5ps2s8ns2f2gqifwmgxwkfj23 FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_armor_types DROP CONSTRAINT FK5ps2s8ns2f2gqifwmgxwkfj23;

--changeset pg-migration:0003-fk-FKl4g19uhyg7qhog09mioep7sv4
ALTER TABLE creature_feats ADD CONSTRAINT FKl4g19uhyg7qhog09mioep7sv4 FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_feats DROP CONSTRAINT FKl4g19uhyg7qhog09mioep7sv4;

--changeset pg-migration:0003-fk-FK3mefydcigpudtlq3d8u5a6evw
ALTER TABLE creature_habitates ADD CONSTRAINT FK3mefydcigpudtlq3d8u5a6evw FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_habitates DROP CONSTRAINT FK3mefydcigpudtlq3d8u5a6evw;

--changeset pg-migration:0003-fk-FKc0xaul4wpohafsglbnm595swf
ALTER TABLE creature_immunity_states ADD CONSTRAINT FKc0xaul4wpohafsglbnm595swf FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_immunity_states DROP CONSTRAINT FKc0xaul4wpohafsglbnm595swf;

--changeset pg-migration:0003-fk-FKa5jtidv7gkp0p4oras72ifb9k
ALTER TABLE creature_immunity_damages ADD CONSTRAINT FKa5jtidv7gkp0p4oras72ifb9k FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_immunity_damages DROP CONSTRAINT FKa5jtidv7gkp0p4oras72ifb9k;

--changeset pg-migration:0003-fk-FKl6y8x11k76t8y3xt7kt7c2ghw
ALTER TABLE creature_resistance_damages ADD CONSTRAINT FKl6y8x11k76t8y3xt7kt7c2ghw FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_resistance_damages DROP CONSTRAINT FKl6y8x11k76t8y3xt7kt7c2ghw;

--changeset pg-migration:0003-fk-FK8m80tp3qf9h2ar3p0f4wrdga2
ALTER TABLE creature_saving_throws ADD CONSTRAINT FK8m80tp3qf9h2ar3p0f4wrdga2 FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_saving_throws DROP CONSTRAINT FK8m80tp3qf9h2ar3p0f4wrdga2;

--changeset pg-migration:0003-fk-FKmtsn0xecxxfbwwvui8onx7b93
ALTER TABLE creature_skills ADD CONSTRAINT FKmtsn0xecxxfbwwvui8onx7b93 FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_skills DROP CONSTRAINT FKmtsn0xecxxfbwwvui8onx7b93;

--changeset pg-migration:0003-fk-FKmvxfoq1m7xytdm3fqxuw8josw
ALTER TABLE creature_spellcaster ADD CONSTRAINT FKmvxfoq1m7xytdm3fqxuw8josw FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_spellcaster DROP CONSTRAINT FKmvxfoq1m7xytdm3fqxuw8josw;

--changeset pg-migration:0003-fk-FKqetl114bccl1800u401hkmjky
ALTER TABLE creature_spells ADD CONSTRAINT FKqetl114bccl1800u401hkmjky FOREIGN KEY (spell_id) REFERENCES spells (id);
--rollback ALTER TABLE creature_spells DROP CONSTRAINT FKqetl114bccl1800u401hkmjky;

--changeset pg-migration:0003-fk-FK6uuukak855h5rwjtpxbeqpnoc
ALTER TABLE creature_spells ADD CONSTRAINT FK6uuukak855h5rwjtpxbeqpnoc FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_spells DROP CONSTRAINT FK6uuukak855h5rwjtpxbeqpnoc;

--changeset pg-migration:0003-fk-FKto43a3sd0flyusho2e5ftwg1k
ALTER TABLE creature_vulnerability_damages ADD CONSTRAINT FKto43a3sd0flyusho2e5ftwg1k FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creature_vulnerability_damages DROP CONSTRAINT FKto43a3sd0flyusho2e5ftwg1k;

--changeset pg-migration:0003-fk-FKtdytg3s53in6mk0xtdsn0csp3
ALTER TABLE creatures ADD CONSTRAINT FKtdytg3s53in6mk0xtdsn0csp3 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE creatures DROP CONSTRAINT FKtdytg3s53in6mk0xtdsn0csp3;

--changeset pg-migration:0003-fk-FKdu64s54n78vis9xe6mtm6d3je
ALTER TABLE creatures ADD CONSTRAINT FKdu64s54n78vis9xe6mtm6d3je FOREIGN KEY (lair_id) REFERENCES lairs (lair_id);
--rollback ALTER TABLE creatures DROP CONSTRAINT FKdu64s54n78vis9xe6mtm6d3je;

--changeset pg-migration:0003-fk-FKnhhp1o5sog3haaky88ag9n1kp
ALTER TABLE creatures_actions ADD CONSTRAINT FKnhhp1o5sog3haaky88ag9n1kp FOREIGN KEY (actions_id) REFERENCES creature_actions (id);
--rollback ALTER TABLE creatures_actions DROP CONSTRAINT FKnhhp1o5sog3haaky88ag9n1kp;

--changeset pg-migration:0003-fk-FK8d41s9r0h9fbd75s726hu86wi
ALTER TABLE creatures_actions ADD CONSTRAINT FK8d41s9r0h9fbd75s726hu86wi FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creatures_actions DROP CONSTRAINT FK8d41s9r0h9fbd75s726hu86wi;

--changeset pg-migration:0003-fk-FKti4d3h2xm3ovqi5g37exokadj
ALTER TABLE creatures_languages ADD CONSTRAINT FKti4d3h2xm3ovqi5g37exokadj FOREIGN KEY (languages_id) REFERENCES languages (id);
--rollback ALTER TABLE creatures_languages DROP CONSTRAINT FKti4d3h2xm3ovqi5g37exokadj;

--changeset pg-migration:0003-fk-FKpsu1oivmfjoq1biew7lm1bnfe
ALTER TABLE creatures_languages ADD CONSTRAINT FKpsu1oivmfjoq1biew7lm1bnfe FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creatures_languages DROP CONSTRAINT FKpsu1oivmfjoq1biew7lm1bnfe;

--changeset pg-migration:0003-fk-FKmcl4vek15iynip6h69kyuoi8n
ALTER TABLE creatures_races ADD CONSTRAINT FKmcl4vek15iynip6h69kyuoi8n FOREIGN KEY (races_id) REFERENCES creature_races (id);
--rollback ALTER TABLE creatures_races DROP CONSTRAINT FKmcl4vek15iynip6h69kyuoi8n;

--changeset pg-migration:0003-fk-FKlpy6o0v56hi00we0cgj4rqnja
ALTER TABLE creatures_races ADD CONSTRAINT FKlpy6o0v56hi00we0cgj4rqnja FOREIGN KEY (creature_id) REFERENCES creatures (id);
--rollback ALTER TABLE creatures_races DROP CONSTRAINT FKlpy6o0v56hi00we0cgj4rqnja;

--changeset pg-migration:0003-fk-FKl9of2dkbn5sc940yc10fo3nwe
ALTER TABLE dm_screens ADD CONSTRAINT FKl9of2dkbn5sc940yc10fo3nwe FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE dm_screens DROP CONSTRAINT FKl9of2dkbn5sc940yc10fo3nwe;

--changeset pg-migration:0003-fk-FKndpcoyd8re0248vwgbkstwcn8
ALTER TABLE equipments ADD CONSTRAINT FKndpcoyd8re0248vwgbkstwcn8 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE equipments DROP CONSTRAINT FKndpcoyd8re0248vwgbkstwcn8;

--changeset pg-migration:0003-fk-FKciorsbn8f74da5ckc17j4st2f
ALTER TABLE equipments_types ADD CONSTRAINT FKciorsbn8f74da5ckc17j4st2f FOREIGN KEY (equipment_id) REFERENCES equipments (id);
--rollback ALTER TABLE equipments_types DROP CONSTRAINT FKciorsbn8f74da5ckc17j4st2f;

--changeset pg-migration:0003-fk-FK57qcx3qt9elttstj1swqgnig4
ALTER TABLE god_domains ADD CONSTRAINT FK57qcx3qt9elttstj1swqgnig4 FOREIGN KEY (god_id) REFERENCES gods (id);
--rollback ALTER TABLE god_domains DROP CONSTRAINT FK57qcx3qt9elttstj1swqgnig4;

--changeset pg-migration:0003-fk-FKa33v3bah2ahy8c3lcl2voi1q8
ALTER TABLE gods ADD CONSTRAINT FKa33v3bah2ahy8c3lcl2voi1q8 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE gods DROP CONSTRAINT FKa33v3bah2ahy8c3lcl2voi1q8;

--changeset pg-migration:0003-fk-FKdjcufh3rgwgwfqgqxikawngs3
ALTER TABLE gods ADD CONSTRAINT FKdjcufh3rgwgwfqgqxikawngs3 FOREIGN KEY (pantheon_id) REFERENCES god_pantheons (id);
--rollback ALTER TABLE gods DROP CONSTRAINT FKdjcufh3rgwgwfqgqxikawngs3;

--changeset pg-migration:0003-fk-FK5vjog62t4ravuowhkf7axiqdp
ALTER TABLE hero_class_feats ADD CONSTRAINT FK5vjog62t4ravuowhkf7axiqdp FOREIGN KEY (archetype_id) REFERENCES archetypes (id);
--rollback ALTER TABLE hero_class_feats DROP CONSTRAINT FK5vjog62t4ravuowhkf7axiqdp;

--changeset pg-migration:0003-fk-FKpme38ha3kxdqmj7awit4sk4cf
ALTER TABLE hero_class_feats ADD CONSTRAINT FKpme38ha3kxdqmj7awit4sk4cf FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE hero_class_feats DROP CONSTRAINT FKpme38ha3kxdqmj7awit4sk4cf;

--changeset pg-migration:0003-fk-FK54bhtvdu98us0adv5hygqo1o9
ALTER TABLE hero_class_feats_spells ADD CONSTRAINT FK54bhtvdu98us0adv5hygqo1o9 FOREIGN KEY (spells_id) REFERENCES spells (id);
--rollback ALTER TABLE hero_class_feats_spells DROP CONSTRAINT FK54bhtvdu98us0adv5hygqo1o9;

--changeset pg-migration:0003-fk-FKf3kgg38dum2fub6yvmo17wq9t
ALTER TABLE hero_class_feats_spells ADD CONSTRAINT FKf3kgg38dum2fub6yvmo17wq9t FOREIGN KEY (archetype_trait_id) REFERENCES hero_class_feats (id);
--rollback ALTER TABLE hero_class_feats_spells DROP CONSTRAINT FKf3kgg38dum2fub6yvmo17wq9t;

--changeset pg-migration:0003-fk-FK9kf706ehl36o45h5yyfxj9ct8
ALTER TABLE hero_class_traits ADD CONSTRAINT FK9kf706ehl36o45h5yyfxj9ct8 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE hero_class_traits DROP CONSTRAINT FK9kf706ehl36o45h5yyfxj9ct8;

--changeset pg-migration:0003-fk-FKnxmf9aaw3fm3xmi0ix7unnhit
ALTER TABLE hero_class_traits ADD CONSTRAINT FKnxmf9aaw3fm3xmi0ix7unnhit FOREIGN KEY (hero_class_id) REFERENCES classes (id);
--rollback ALTER TABLE hero_class_traits DROP CONSTRAINT FKnxmf9aaw3fm3xmi0ix7unnhit;

--changeset pg-migration:0003-fk-FKbrl60e7q4pi9y54qgca2g0otu
ALTER TABLE invitation ADD CONSTRAINT FKbrl60e7q4pi9y54qgca2g0otu FOREIGN KEY (user_party_id) REFERENCES user_party (id);
--rollback ALTER TABLE invitation DROP CONSTRAINT FKbrl60e7q4pi9y54qgca2g0otu;

--changeset pg-migration:0003-fk-FKpa5e8iaewcpeueitbmdgg4t2y
ALTER TABLE level_definitions ADD CONSTRAINT FKpa5e8iaewcpeueitbmdgg4t2y FOREIGN KEY (hero_class_id) REFERENCES classes (id);
--rollback ALTER TABLE level_definitions DROP CONSTRAINT FKpa5e8iaewcpeueitbmdgg4t2y;

--changeset pg-migration:0003-fk-FKe7mxj9q16derros838tgx230
ALTER TABLE level_definitions ADD CONSTRAINT FKe7mxj9q16derros838tgx230 FOREIGN KEY (archetype_id) REFERENCES archetypes (id);
--rollback ALTER TABLE level_definitions DROP CONSTRAINT FKe7mxj9q16derros838tgx230;

--changeset pg-migration:0003-fk-FK7nfm2a3etawo8yfg0mnse6hwg
ALTER TABLE magic_things_random ADD CONSTRAINT FK7nfm2a3etawo8yfg0mnse6hwg FOREIGN KEY (magic_thing_id) REFERENCES artifactes (id);
--rollback ALTER TABLE magic_things_random DROP CONSTRAINT FK7nfm2a3etawo8yfg0mnse6hwg;

--changeset pg-migration:0003-fk-FKgeupubdqncc1lpgf2cn4fqwbc
ALTER TABLE menu ADD CONSTRAINT FKgeupubdqncc1lpgf2cn4fqwbc FOREIGN KEY (parent_id) REFERENCES menu (id);
--rollback ALTER TABLE menu DROP CONSTRAINT FKgeupubdqncc1lpgf2cn4fqwbc;

--changeset pg-migration:0003-fk-FKsjm907nkd21mmclb3ruhj6rkk
ALTER TABLE name_owners ADD CONSTRAINT FKsjm907nkd21mmclb3ruhj6rkk FOREIGN KEY (parent_id) REFERENCES name_owners (id);
--rollback ALTER TABLE name_owners DROP CONSTRAINT FKsjm907nkd21mmclb3ruhj6rkk;

--changeset pg-migration:0003-fk-FK6trvhr3t4hke6kdigs0byvfgh
ALTER TABLE options ADD CONSTRAINT FK6trvhr3t4hke6kdigs0byvfgh FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE options DROP CONSTRAINT FK6trvhr3t4hke6kdigs0byvfgh;

--changeset pg-migration:0003-fk-FKh28mis4dvhlqr76b6fiom0ddf
ALTER TABLE race_bonuses ADD CONSTRAINT FKh28mis4dvhlqr76b6fiom0ddf FOREIGN KEY (race_id) REFERENCES races (id);
--rollback ALTER TABLE race_bonuses DROP CONSTRAINT FKh28mis4dvhlqr76b6fiom0ddf;

--changeset pg-migration:0003-fk-FKmws6nj93m4hfh8gv83i7drfja
ALTER TABLE race_feature_skill_type ADD CONSTRAINT FKmws6nj93m4hfh8gv83i7drfja FOREIGN KEY (feature_id) REFERENCES race_features (id);
--rollback ALTER TABLE race_feature_skill_type DROP CONSTRAINT FKmws6nj93m4hfh8gv83i7drfja;

--changeset pg-migration:0003-fk-FK1wiidtjjqgsmy5ffg7wfl0h2h
ALTER TABLE race_features ADD CONSTRAINT FK1wiidtjjqgsmy5ffg7wfl0h2h FOREIGN KEY (race_id) REFERENCES races (id);
--rollback ALTER TABLE race_features DROP CONSTRAINT FK1wiidtjjqgsmy5ffg7wfl0h2h;

--changeset pg-migration:0003-fk-FKcugwjnf9kijbbxr67v2ci5c5y
ALTER TABLE race_features_lanuages ADD CONSTRAINT FKcugwjnf9kijbbxr67v2ci5c5y FOREIGN KEY (lanuages_id) REFERENCES languages (id);
--rollback ALTER TABLE race_features_lanuages DROP CONSTRAINT FKcugwjnf9kijbbxr67v2ci5c5y;

--changeset pg-migration:0003-fk-FKg3rlu574ma7ah7vwxli0tnvef
ALTER TABLE race_features_lanuages ADD CONSTRAINT FKg3rlu574ma7ah7vwxli0tnvef FOREIGN KEY (feature_id) REFERENCES race_features (id);
--rollback ALTER TABLE race_features_lanuages DROP CONSTRAINT FKg3rlu574ma7ah7vwxli0tnvef;

--changeset pg-migration:0003-fk-FKhxb1ieg3urrpjvrjdqt91gwce
ALTER TABLE race_names ADD CONSTRAINT FKhxb1ieg3urrpjvrjdqt91gwce FOREIGN KEY (race_id) REFERENCES races (id);
--rollback ALTER TABLE race_names DROP CONSTRAINT FKhxb1ieg3urrpjvrjdqt91gwce;

--changeset pg-migration:0003-fk-FKpl8s9hm3l8fshh49s2ydhmlfn
ALTER TABLE race_nicknames ADD CONSTRAINT FKpl8s9hm3l8fshh49s2ydhmlfn FOREIGN KEY (race_id) REFERENCES races (id);
--rollback ALTER TABLE race_nicknames DROP CONSTRAINT FKpl8s9hm3l8fshh49s2ydhmlfn;

--changeset pg-migration:0003-fk-FKmqemuianmkuxgekbsojof9eek
ALTER TABLE race_spells ADD CONSTRAINT FKmqemuianmkuxgekbsojof9eek FOREIGN KEY (spells_id) REFERENCES spells (id);
--rollback ALTER TABLE race_spells DROP CONSTRAINT FKmqemuianmkuxgekbsojof9eek;

--changeset pg-migration:0003-fk-FKnwn0w76sc1713fevi5aq2clry
ALTER TABLE race_spells ADD CONSTRAINT FKnwn0w76sc1713fevi5aq2clry FOREIGN KEY (race_id) REFERENCES races (id);
--rollback ALTER TABLE race_spells DROP CONSTRAINT FKnwn0w76sc1713fevi5aq2clry;

--changeset pg-migration:0003-fk-FKeqgp4vwkw5jp0dulw21sn3kay
ALTER TABLE races ADD CONSTRAINT FKeqgp4vwkw5jp0dulw21sn3kay FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE races DROP CONSTRAINT FKeqgp4vwkw5jp0dulw21sn3kay;

--changeset pg-migration:0003-fk-FKnt8oiohhein5ps6fmoqfhxjng
ALTER TABLE races ADD CONSTRAINT FKnt8oiohhein5ps6fmoqfhxjng FOREIGN KEY (parent_id) REFERENCES races (id);
--rollback ALTER TABLE races DROP CONSTRAINT FKnt8oiohhein5ps6fmoqfhxjng;

--changeset pg-migration:0003-fk-FKmxcl48ob0tbjy05wcyqv3b5qq
ALTER TABLE races_languages ADD CONSTRAINT FKmxcl48ob0tbjy05wcyqv3b5qq FOREIGN KEY (languages_id) REFERENCES languages (id);
--rollback ALTER TABLE races_languages DROP CONSTRAINT FKmxcl48ob0tbjy05wcyqv3b5qq;

--changeset pg-migration:0003-fk-FKbxlty3f1h994kah8s8nj7gr10
ALTER TABLE races_languages ADD CONSTRAINT FKbxlty3f1h994kah8s8nj7gr10 FOREIGN KEY (race_id) REFERENCES races (id);
--rollback ALTER TABLE races_languages DROP CONSTRAINT FKbxlty3f1h994kah8s8nj7gr10;

--changeset pg-migration:0003-fk-FKg62ry3vbe44dkd6hdc50xtfiu
ALTER TABLE random_encounter_tables ADD CONSTRAINT FKg62ry3vbe44dkd6hdc50xtfiu FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE random_encounter_tables DROP CONSTRAINT FKg62ry3vbe44dkd6hdc50xtfiu;

--changeset pg-migration:0003-fk-FK5jcp2sve2s2kgxuw3nkildaga
ALTER TABLE random_encounters ADD CONSTRAINT FK5jcp2sve2s2kgxuw3nkildaga FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE random_encounters DROP CONSTRAINT FK5jcp2sve2s2kgxuw3nkildaga;

--changeset pg-migration:0003-fk-FKd95da2peiaafslttipo4uua0t
ALTER TABLE random_encounters ADD CONSTRAINT FKd95da2peiaafslttipo4uua0t FOREIGN KEY (encounter_id) REFERENCES random_encounter_tables (id);
--rollback ALTER TABLE random_encounters DROP CONSTRAINT FKd95da2peiaafslttipo4uua0t;

--changeset pg-migration:0003-fk-FKs3m2we40idk675g6jgcgug5kg
ALTER TABLE rules ADD CONSTRAINT FKs3m2we40idk675g6jgcgug5kg FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE rules DROP CONSTRAINT FKs3m2we40idk675g6jgcgug5kg;

--changeset pg-migration:0003-fk-FKlhyh8v4eeqr6dfsvidnol7c0e
ALTER TABLE screens ADD CONSTRAINT FKlhyh8v4eeqr6dfsvidnol7c0e FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE screens DROP CONSTRAINT FKlhyh8v4eeqr6dfsvidnol7c0e;

--changeset pg-migration:0003-fk-FKtp02ljuqq1xw33pk3ohgk6i6g
ALTER TABLE screens ADD CONSTRAINT FKtp02ljuqq1xw33pk3ohgk6i6g FOREIGN KEY (parent_id) REFERENCES screens (id);
--rollback ALTER TABLE screens DROP CONSTRAINT FKtp02ljuqq1xw33pk3ohgk6i6g;

--changeset pg-migration:0003-fk-FK3qkx14nuceyxxugh7judjkhhj
ALTER TABLE spell_damage_type ADD CONSTRAINT FK3qkx14nuceyxxugh7judjkhhj FOREIGN KEY (spell_id) REFERENCES spells (id);
--rollback ALTER TABLE spell_damage_type DROP CONSTRAINT FK3qkx14nuceyxxugh7judjkhhj;

--changeset pg-migration:0003-fk-FK5d03iyh2704x3wy81g5lmpga8
ALTER TABLE spell_heal_type ADD CONSTRAINT FK5d03iyh2704x3wy81g5lmpga8 FOREIGN KEY (spell_id) REFERENCES spells (id);
--rollback ALTER TABLE spell_heal_type DROP CONSTRAINT FK5d03iyh2704x3wy81g5lmpga8;

--changeset pg-migration:0003-fk-FK3xu5l5iwlukcyjr2xn8tdxs2h
ALTER TABLE spell_savingthrows ADD CONSTRAINT FK3xu5l5iwlukcyjr2xn8tdxs2h FOREIGN KEY (spell_id) REFERENCES spells (id);
--rollback ALTER TABLE spell_savingthrows DROP CONSTRAINT FK3xu5l5iwlukcyjr2xn8tdxs2h;

--changeset pg-migration:0003-fk-FK16pr839oq3avm3j44etpu4bpl
ALTER TABLE spell_source ADD CONSTRAINT FK16pr839oq3avm3j44etpu4bpl FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE spell_source DROP CONSTRAINT FK16pr839oq3avm3j44etpu4bpl;

--changeset pg-migration:0003-fk-FKhalyvs3qve3voc96kwp8rf2dr
ALTER TABLE spells ADD CONSTRAINT FKhalyvs3qve3voc96kwp8rf2dr FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE spells DROP CONSTRAINT FKhalyvs3qve3voc96kwp8rf2dr;

--changeset pg-migration:0003-fk-FKqo8toyt80nd2cm67cfw2btiv4
ALTER TABLE spells_hero_class ADD CONSTRAINT FKqo8toyt80nd2cm67cfw2btiv4 FOREIGN KEY (hero_class_id) REFERENCES classes (id);
--rollback ALTER TABLE spells_hero_class DROP CONSTRAINT FKqo8toyt80nd2cm67cfw2btiv4;

--changeset pg-migration:0003-fk-FK3wpc84e0k4r9ls0oxkhg8a1et
ALTER TABLE spells_hero_class ADD CONSTRAINT FK3wpc84e0k4r9ls0oxkhg8a1et FOREIGN KEY (spell_id) REFERENCES spells (id);
--rollback ALTER TABLE spells_hero_class DROP CONSTRAINT FK3wpc84e0k4r9ls0oxkhg8a1et;

--changeset pg-migration:0003-fk-FKpghkcnch06gfr7x3e9qbhwg0c
ALTER TABLE spells_times ADD CONSTRAINT FKpghkcnch06gfr7x3e9qbhwg0c FOREIGN KEY (spell_id) REFERENCES spells (id);
--rollback ALTER TABLE spells_times DROP CONSTRAINT FKpghkcnch06gfr7x3e9qbhwg0c;

--changeset pg-migration:0003-fk-FK9omad8qe8aqu8n5uaee0jiwet
ALTER TABLE taverna_visitor_chances ADD CONSTRAINT FK9omad8qe8aqu8n5uaee0jiwet FOREIGN KEY (visitor_id) REFERENCES taverna_visitors (id);
--rollback ALTER TABLE taverna_visitor_chances DROP CONSTRAINT FK9omad8qe8aqu8n5uaee0jiwet;

--changeset pg-migration:0003-fk-FKag5qarx6kc8wej460jr0wga6u
ALTER TABLE taverna_visitor_habitates ADD CONSTRAINT FKag5qarx6kc8wej460jr0wga6u FOREIGN KEY (visitor_id) REFERENCES taverna_visitors (id);
--rollback ALTER TABLE taverna_visitor_habitates DROP CONSTRAINT FKag5qarx6kc8wej460jr0wga6u;

--changeset pg-migration:0003-fk-FKcui9odc3hfmu10c8xa1n44o1f
ALTER TABLE trackers ADD CONSTRAINT FKcui9odc3hfmu10c8xa1n44o1f FOREIGN KEY (level_def_id) REFERENCES level_definitions (id);
--rollback ALTER TABLE trackers DROP CONSTRAINT FKcui9odc3hfmu10c8xa1n44o1f;

--changeset pg-migration:0003-fk-FK14dsek15e66cjms9q92da16hv
ALTER TABLE trait_abilities ADD CONSTRAINT FK14dsek15e66cjms9q92da16hv FOREIGN KEY (trait_id) REFERENCES traits (id);
--rollback ALTER TABLE trait_abilities DROP CONSTRAINT FK14dsek15e66cjms9q92da16hv;

--changeset pg-migration:0003-fk-FKfr4esayba1wcxfo2lrpgj8fsa
ALTER TABLE trait_skills ADD CONSTRAINT FKfr4esayba1wcxfo2lrpgj8fsa FOREIGN KEY (trait_id) REFERENCES traits (id);
--rollback ALTER TABLE trait_skills DROP CONSTRAINT FKfr4esayba1wcxfo2lrpgj8fsa;

--changeset pg-migration:0003-fk-FKhg0mt1y5eqycedtm4ttr9xh08
ALTER TABLE traits ADD CONSTRAINT FKhg0mt1y5eqycedtm4ttr9xh08 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE traits DROP CONSTRAINT FKhg0mt1y5eqycedtm4ttr9xh08;

--changeset pg-migration:0003-fk-FKmu8tsy05xuvi3p0btbw0uqk57
ALTER TABLE treasures ADD CONSTRAINT FKmu8tsy05xuvi3p0btbw0uqk57 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE treasures DROP CONSTRAINT FKmu8tsy05xuvi3p0btbw0uqk57;

--changeset pg-migration:0003-fk-FKkryr7ap9l7xng2bjca10l5i8k
ALTER TABLE user_excluded_sources ADD CONSTRAINT FKkryr7ap9l7xng2bjca10l5i8k FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE user_excluded_sources DROP CONSTRAINT FKkryr7ap9l7xng2bjca10l5i8k;

--changeset pg-migration:0003-fk-FKrcofdno3o5impia03lef6mcg3
ALTER TABLE user_membership ADD CONSTRAINT FKrcofdno3o5impia03lef6mcg3 FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE user_membership DROP CONSTRAINT FKrcofdno3o5impia03lef6mcg3;

--changeset pg-migration:0003-fk-FKf6roppfi60h8fvmdu0htq7g5o
ALTER TABLE user_membership ADD CONSTRAINT FKf6roppfi60h8fvmdu0htq7g5o FOREIGN KEY (user_party_id) REFERENCES user_party (id);
--rollback ALTER TABLE user_membership DROP CONSTRAINT FKf6roppfi60h8fvmdu0htq7g5o;

--changeset pg-migration:0003-fk-FKt7e7djp752sqn6w22i6ocqy6q
ALTER TABLE user_role ADD CONSTRAINT FKt7e7djp752sqn6w22i6ocqy6q FOREIGN KEY (role_id) REFERENCES roles (id);
--rollback ALTER TABLE user_role DROP CONSTRAINT FKt7e7djp752sqn6w22i6ocqy6q;

--changeset pg-migration:0003-fk-FKj345gk1bovqvfame88rcx7yyx
ALTER TABLE user_role ADD CONSTRAINT FKj345gk1bovqvfame88rcx7yyx FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE user_role DROP CONSTRAINT FKj345gk1bovqvfame88rcx7yyx;

--changeset pg-migration:0003-fk-FKrv9pbfy81qxf35ownwhcll7mo
ALTER TABLE user_sources ADD CONSTRAINT FKrv9pbfy81qxf35ownwhcll7mo FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE user_sources DROP CONSTRAINT FKrv9pbfy81qxf35ownwhcll7mo;

--changeset pg-migration:0003-fk-FKikqanfs6yyui7nnbif1r491wd
ALTER TABLE user_sources ADD CONSTRAINT FKikqanfs6yyui7nnbif1r491wd FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE user_sources DROP CONSTRAINT FKikqanfs6yyui7nnbif1r491wd;

--changeset pg-migration:0003-fk-FKjpjgfh5946o480m711cnot57r
ALTER TABLE user_wait_list ADD CONSTRAINT FKjpjgfh5946o480m711cnot57r FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE user_wait_list DROP CONSTRAINT FKjpjgfh5946o480m711cnot57r;

--changeset pg-migration:0003-fk-FK7lqtkm6yahlhb7r0fiih47tja
ALTER TABLE user_wait_list ADD CONSTRAINT FK7lqtkm6yahlhb7r0fiih47tja FOREIGN KEY (user_party_id) REFERENCES user_party (id);
--rollback ALTER TABLE user_wait_list DROP CONSTRAINT FK7lqtkm6yahlhb7r0fiih47tja;

--changeset pg-migration:0003-fk-FK3asw9wnv76uxu3kr1ekq4i1ld
ALTER TABLE verification_token ADD CONSTRAINT FK3asw9wnv76uxu3kr1ekq4i1ld FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE verification_token DROP CONSTRAINT FK3asw9wnv76uxu3kr1ekq4i1ld;

--changeset pg-migration:0003-fk-FKc4rvlgrnunclnv8urismu07a
ALTER TABLE weapon_properties ADD CONSTRAINT FKc4rvlgrnunclnv8urismu07a FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE weapon_properties DROP CONSTRAINT FKc4rvlgrnunclnv8urismu07a;

--changeset pg-migration:0003-fk-FKcj81i2cvf40gg4qd30uaejux9
ALTER TABLE weapons ADD CONSTRAINT FKcj81i2cvf40gg4qd30uaejux9 FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE weapons DROP CONSTRAINT FKcj81i2cvf40gg4qd30uaejux9;

--changeset pg-migration:0003-fk-FK2fcchx5hfjj8b3vpwui90m018
ALTER TABLE weapons_properties ADD CONSTRAINT FK2fcchx5hfjj8b3vpwui90m018 FOREIGN KEY (properties_id) REFERENCES weapon_properties (id);
--rollback ALTER TABLE weapons_properties DROP CONSTRAINT FK2fcchx5hfjj8b3vpwui90m018;

--changeset pg-migration:0003-fk-FK1cnhsx4fd7gi3ns4441n0933h
ALTER TABLE weapons_properties ADD CONSTRAINT FK1cnhsx4fd7gi3ns4441n0933h FOREIGN KEY (weapon_id) REFERENCES weapons (id);
--rollback ALTER TABLE weapons_properties DROP CONSTRAINT FK1cnhsx4fd7gi3ns4441n0933h;

--changeset pg-migration:0003-fk-FKdae3lqea7tkx7ycr2sr1627bp
ALTER TABLE wild_magics ADD CONSTRAINT FKdae3lqea7tkx7ycr2sr1627bp FOREIGN KEY (source) REFERENCES books (source);
--rollback ALTER TABLE wild_magics DROP CONSTRAINT FKdae3lqea7tkx7ycr2sr1627bp;

--changeset pg-migration:0003-fk-FK4mx6k01ak5dfbtmbybc1672hf
ALTER TABLE youtube_videos ADD CONSTRAINT FK4mx6k01ak5dfbtmbybc1672hf FOREIGN KEY (user_id) REFERENCES users (id);
--rollback ALTER TABLE youtube_videos DROP CONSTRAINT FK4mx6k01ak5dfbtmbybc1672hf;
