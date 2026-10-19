package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Rest;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.FeatureLevelDefinition;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.SpellLevelDefinition;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpell;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpellLevelType;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.model.splells.Spell;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VttgClassMapperTest {
	private final VttgClassMapper mapper = new VttgClassMapper();

	@Test
	void fighterCarriesProficienciesFeaturesAndArchetype() {
		HeroClass fighter = heroClass("Воин", "Fighter");
		fighter.setDiceHp((byte) 10);
		fighter.setArmor("Все доспехи, щиты");
		fighter.setWeapon("Простое оружие, воинское оружие");
		fighter.setTools("Нет");
		fighter.setSavingThrows("Сила, Телосложение");
		fighter.setSkillAvailableCount((short) 2);
		fighter.setAvailableSkills(Arrays.asList(SkillType.ATHLETICS, SkillType.ANIMAL_HANDLING));
		fighter.setArchetypeName("Воинский архетип");
		fighter.setEnabledArhitypeLevel(3);
		HeroClassTrait style = trait(1, "Боевой стиль", 1);
		HeroClassTrait asi = trait(2, "Увеличение характеристик", 4);
		HeroClassTrait asiAgain = trait(3, "Увеличение характеристик", 8);
		asiAgain.setChild("2");
		asiAgain.setDescription(null);
		HeroClassTrait archetypeRow = trait(4, "Воинский архетип", 3);
		archetypeRow.setArchitype(true);
		fighter.setTraits(Arrays.asList(style, asi, asiAgain, archetypeRow));

		Archetype champion = archetype("Чемпион", "Champion", 3);
		ArchetypeTrait critical = new ArchetypeTrait();
		critical.setName("Улучшенный критический удар");
		critical.setLevel((byte) 3);
		critical.setDescription("<p>Критическое попадание на 19–20.</p>");
		champion.setFeats(Collections.singletonList(critical));
		fighter.setArchetypes(Collections.singletonList(champion));

		Option defense = new Option();
		defense.setName("Оборона");
		defense.setEnglishName("Defense");
		defense.setDescription("<p>+1 к КД.</p>");
		defense.setPrerequisite("Нет");
		Map<Option.OptionType, List<Option>> options = new HashMap<>();
		options.put(Option.OptionType.FIGHTING_STYLE, Collections.singletonList(defense));

		Map<String, Object> data = mapper.toVttg(fighter, options);

		assertThat(data).containsEntry("type", "class")
			.containsEntry("id", "fighter")
			.containsEntry("section", "classes")
			.containsEntry("srcSection", "classes")
			.containsEntry("hitDie", 10)
			.containsEntry("armorProficiencies", Arrays.asList("light", "medium", "heavy", "shield"))
			.containsEntry("weaponProficiencies", Arrays.asList("simple", "martial"))
			.containsEntry("toolProficiencies", Collections.emptyList())
			.containsEntry("savingThrowProficiencies", Arrays.asList("strength", "constitution"))
			.containsEntry("subclassLevel", 3)
			.containsEntry("subclassLabel", "Воинский архетип")
			.containsEntry("spellcasting", null)
			.doesNotContainKeys("armorProficienciesCustom", "weaponProficienciesCustom");
		assertThat(asMap(data.get("skillChoices"))).containsEntry("count", 2)
			.containsEntry("from", Arrays.asList("athletics", "animalHandling"));

		List<Map<String, Object>> features = asList(data.get("features"));
		assertThat(features).extracting(feature -> feature.get("key"))
			.containsExactly("boevoy-stil", "voinskiy-arhetip", "asi-4", "asi-8");
		assertThat(features.get(1)).containsEntry("isInformationalOnly", true);
		assertThat(features.get(2)).containsEntry("abilityImprovement", true);
		assertThat(features.get(3)).containsEntry("description", "<p>Описание 2</p>");
		assertThat(asList(features.get(0).get("choices"))).singleElement()
			.satisfies(choice -> assertThat(choice).containsEntry("key", "defense")
				.containsEntry("name", "Оборона")
				.doesNotContainKey("prerequisite"));

		List<Map<String, Object>> table = asList(data.get("levelTable"));
		assertThat(table).hasSize(20);
		assertThat(table.get(3)).containsEntry("level", 4).containsEntry("proficiencyBonus", 2)
			.containsEntry("featureKeys", Collections.singletonList("asi-4"));
		assertThat(table.get(16)).containsEntry("proficiencyBonus", 6);

		Map<String, Object> subclass = asList(data.get("subclasses")).get(0);
		assertThat(subclass).containsEntry("key", "champion")
			.containsEntry("unlockLevel", 3)
			.containsEntry("sourceKey", "phb")
			.doesNotContainKeys("spellcasting", "levelTable");
		assertThat(asList(subclass.get("features"))).singleElement()
			.satisfies(feature -> assertThat(feature)
				.containsEntry("key", "champion-uluchshennyy-kriticheskiy-udar")
				.containsEntry("subclassKey", "champion")
				.containsEntry("level", 3));
	}

	@Test
	void wizardTableCarriesSlotsAndNewCantrips() {
		HeroClass wizard = heroClass("Волшебник", "Wizard");
		wizard.setSpellAbility(AbilityType.INTELLIGENCE);
		wizard.setSpellcasterType(SpellcasterType.FULL);
		wizard.setSlotsReset(Rest.LONG);
		FeatureLevelDefinition cantrips = new FeatureLevelDefinition();
		cantrips.setName("Известные заговоры");
		cantrips.setL1((byte) 3);
		cantrips.setL2((byte) 3);
		cantrips.setL3((byte) 3);
		cantrips.setL4((byte) 4);
		wizard.setFeatureLevelDefenitions(Collections.singletonList(cantrips));
		wizard.setLevelDefenitions(Arrays.asList(slots(1, 2, 0), slots(2, 3, 0), slots(3, 4, 2)));

		Map<String, Object> data = mapper.toVttg(wizard, Collections.emptyMap());

		assertThat(asMap(data.get("spellcasting"))).containsEntry("type", "full")
			.containsEntry("ability", "intelligence")
			.containsEntry("startLevel", 1);
		List<Map<String, Object>> columns = asList(data.get("tableColumns"));
		assertThat(columns.get(0)).containsEntry("key", "cantripsKnown");
		assertThat(columns.get(1)).containsEntry("label", "Ячейки заклинаний");
		assertThat(asList(columns.get(1).get("children"))).extracting(child -> child.get("key"))
			.containsExactly("slot1", "slot2");
		List<Map<String, Object>> table = asList(data.get("levelTable"));
		assertThat(table.get(0)).containsEntry("newCantrips", 3).containsEntry("cantripsKnown", "3")
			.containsEntry("slot1", "2").containsEntry("slot2", "—");
		assertThat(table.get(1)).doesNotContainKey("newCantrips");
		assertThat(table.get(2)).containsEntry("slot2", "2");
		assertThat(table.get(3)).containsEntry("newCantrips", 1);
		assertThat(table.get(4)).containsEntry("cantripsKnown", "—");
	}

	@Test
	void halfCasterStartsWithFirstSlotsAndWarlockIsPact() {
		HeroClass paladin = heroClass("Паладин", "Paladin");
		paladin.setSpellAbility(AbilityType.CHARISMA);
		paladin.setSpellcasterType(SpellcasterType.HALF);
		paladin.setLevelDefenitions(Arrays.asList(slots(1, 0, 0), slots(2, 2, 0)));
		HeroClass warlock = heroClass("Колдун", "Warlock");
		warlock.setSpellAbility(AbilityType.CHARISMA);
		warlock.setSpellcasterType(SpellcasterType.NONE);
		warlock.setSlotsReset(Rest.SHORT);

		assertThat(asMap(mapper.toVttg(paladin, Collections.emptyMap()).get("spellcasting")))
			.containsEntry("type", "half").containsEntry("startLevel", 2);
		assertThat(asMap(mapper.toVttg(warlock, Collections.emptyMap()).get("spellcasting")))
			.containsEntry("type", "pact").containsEntry("ability", "charisma");
	}

	@Test
	void resourceColumnBecomesCounter() {
		HeroClass barbarian = heroClass("Варвар", "Barbarian");
		FeatureLevelDefinition rage = new FeatureLevelDefinition();
		rage.setName("Ярость");
		rage.setL1((byte) 2);
		rage.setL3((byte) 3);
		rage.setL20((byte) -1);
		barbarian.setFeatureLevelDefenitions(Collections.singletonList(rage));

		Map<String, Object> data = mapper.toVttg(barbarian, Collections.emptyMap());

		assertThat(asList(data.get("counters"))).singleElement().satisfies(counter -> {
			assertThat(counter).containsEntry("key", "yarost")
				.containsEntry("recovery", "long")
				.containsEntry("startLevel", 1);
			assertThat(asMap(counter.get("progression"))).containsOnlyKeys("1", "3");
		});
		assertThat(asList(data.get("levelTable")).get(19)).containsEntry("yarost", "∞");
	}

	@Test
	void domainSpellsAreGrantedByClassLevel() {
		HeroClass cleric = heroClass("Жрец", "Cleric");
		Archetype life = archetype("Домен жизни", "Life Domain", 1);
		ArchetypeTrait disciple = new ArchetypeTrait();
		disciple.setName("Ученик жизни");
		disciple.setLevel((byte) 1);
		life.setFeats(Collections.singletonList(disciple));
		life.setSpells(Arrays.asList(archetypeSpell(1, "Bless"), archetypeSpell(5, "Revivify")));
		cleric.setArchetypes(Collections.singletonList(life));

		Map<String, Object> feature = asList(asList(mapper.toVttg(cleric, Collections.emptyMap())
			.get("subclasses")).get(0).get("features")).get(0);

		assertThat(feature).containsEntry("grantedSpells", Collections.singletonList("bless"));
		assertThat(asMap(feature.get("grantedSpellsByLevel")))
			.containsEntry("5", Collections.singletonList("revivify"));
		assertThat(asMap(feature.get("featData"))).containsEntry("grantedSpellsAlwaysPrepared", true);
	}

	@Test
	void patronSpellsExpandSpellListByCircle() {
		HeroClass warlock = heroClass("Колдун", "Warlock");
		Archetype fiend = archetype("Исчадие", "Fiend", 1);
		ArchetypeTrait blessing = new ArchetypeTrait();
		blessing.setName("Благословение тёмного");
		blessing.setLevel((byte) 1);
		fiend.setFeats(Collections.singletonList(blessing));
		fiend.setSpellLevelType(ArchetypeSpellLevelType.SPELL_LEVEL);
		fiend.setSpells(Arrays.asList(archetypeSpell(1, "Burning Hands"), archetypeSpell(3, "Fireball")));
		warlock.setArchetypes(Collections.singletonList(fiend));

		Map<String, Object> feature = asList(asList(mapper.toVttg(warlock, Collections.emptyMap())
			.get("subclasses")).get(0).get("features")).get(0);

		List<Map<String, Object>> groups = asList(asMap(asMap(feature.get("featData")).get("spellList")).get("groups"));
		assertThat(groups).extracting(group -> group.get("requiredLevel")).containsExactly(1, 5);
		assertThat(feature).doesNotContainKey("grantedSpells");
	}

	@Test
	void customProficienciesStayAsText() {
		HeroClass rogue = heroClass("Плут", "Rogue");
		rogue.setArmor("Лёгкие доспехи");
		rogue.setWeapon("Простое оружие, длинные мечи, рапиры");

		Map<String, Object> data = mapper.toVttg(rogue, Collections.emptyMap());

		assertThat(data).containsEntry("armorProficiencies", Collections.singletonList("light"))
			.containsEntry("weaponProficiencies", Collections.singletonList("simple"))
			.containsEntry("weaponProficienciesCustom", "длинные мечи, рапиры")
			.doesNotContainKey("armorProficienciesCustom");
	}

	private static HeroClass heroClass(String name, String englishName) {
		HeroClass heroClass = new HeroClass();
		heroClass.setName(name);
		heroClass.setEnglishName(englishName);
		heroClass.setBook(new Book("PHB"));
		heroClass.setDiceHp((byte) 8);
		heroClass.setTraits(new ArrayList<>());
		heroClass.setArchetypes(new ArrayList<>());
		heroClass.setFeatureLevelDefenitions(new ArrayList<>());
		heroClass.setLevelDefenitions(new ArrayList<>());
		return heroClass;
	}

	private static HeroClassTrait trait(int id, String name, int level) {
		HeroClassTrait trait = new HeroClassTrait();
		trait.setId(id);
		trait.setName(name);
		trait.setLevel((byte) level);
		trait.setDescription("<p>Описание " + id + "</p>");
		return trait;
	}

	private static Archetype archetype(String name, String englishName, int level) {
		Archetype archetype = new Archetype();
		archetype.setName(name);
		archetype.setEnglishName(englishName);
		archetype.setLevel((byte) level);
		archetype.setBook(new Book("PHB"));
		archetype.setFeats(new ArrayList<>());
		archetype.setSpells(new ArrayList<>());
		archetype.setFeatureLevelDefenitions(new ArrayList<>());
		archetype.setLevelDefenitions(new ArrayList<>());
		return archetype;
	}

	private static ArchetypeSpell archetypeSpell(int level, String englishName) {
		Spell spell = new Spell();
		spell.setName(englishName);
		spell.setEnglishName(englishName);
		ArchetypeSpell archetypeSpell = new ArchetypeSpell();
		archetypeSpell.setLevel(level);
		archetypeSpell.setSpell(spell);
		return archetypeSpell;
	}

	private static SpellLevelDefinition slots(int level, int first, int second) {
		SpellLevelDefinition definition = new SpellLevelDefinition();
		definition.setLevel((byte) level);
		definition.setSlot1((byte) first);
		definition.setSlot2((byte) second);
		return definition;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return (Map<String, Object>) value;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> asList(Object value) {
		return (List<Map<String, Object>>) value;
	}
}
