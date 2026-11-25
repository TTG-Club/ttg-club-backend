package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.FeatureScaling;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpell;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpellLevelType;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.effect.ActiveEffect;
import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.mechanics.ProficiencyGrant;
import club.dnd5.portal.model.mechanics.ResourceCounter;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassDetailApiTest {
	@Test
	void exposesArchetypeSpellsWithExplicitLevelType() {
		HeroClass heroClass = baseClass();
		Archetype archetype = baseArchetype(heroClass);
		archetype.setSpellLevelType(ArchetypeSpellLevelType.SPELL_LEVEL);
		archetype.setSpells(Arrays.asList(
			archetypeSpell(3, "Щит", "Shield", "всегда подготовлено"),
			archetypeSpell(1, "Свет", "Light", null),
			archetypeSpell(0, "Игнорируется", "Ignored", null)
		));

		ClassDetailApi details = new ClassDetailApi(archetype, Collections.emptyList(), new ClassRequestApi());
		ClassDetailApi.ArchetypeSpellTableApi table = details.getArchetypeSpellTable();
		List<ClassDetailApi.ArchetypeSpellLevelApi> levels = new ArrayList<>(table.getLevels());

		assertEquals(ArchetypeSpellLevelType.SPELL_LEVEL, table.getLevelType());
		assertEquals("воина из базы", table.getClassNameGenitive());
		assertEquals(Arrays.asList(1, 3), levels.stream()
			.map(ClassDetailApi.ArchetypeSpellLevelApi::getLevel)
			.collect(Collectors.toList()));
		assertEquals("/spells/light", levels.get(0).getSpells().iterator().next().getUrl());
		assertEquals("всегда подготовлено", levels.get(1).getSpells().iterator().next().getAdvanced());
	}

	@Test
	void sortsClassAndArchetypeFeaturesTogetherByLevel() {
		Book book = new Book("TEST");
		book.setName("Test Book");
		book.setType(TypeBook.OFFICAL);

		HeroClass heroClass = new HeroClass();
		heroClass.setId(1);
		heroClass.setName("Воин");
		heroClass.setEnglishName("Fighter");
		heroClass.setBook(book);
		heroClass.setSpellcasterType(SpellcasterType.NONE);
		heroClass.setAvailableSkills(Collections.emptyList());
		heroClass.setPrimaryAbilities(Collections.emptyList());
		heroClass.setFeatureLevelDefenitions(Collections.emptyList());
		heroClass.setLevelDefenitions(Collections.emptyList());
		heroClass.setTraits(Arrays.asList(
			classTrait(2, "Умение класса 5", 5),
			classTrait(1, "Умение класса 1", 1),
			archetypeSelectionTrait(3, "Умение архетипа", 3)
		));

		Archetype archetype = new Archetype();
		archetype.setId(10);
		archetype.setName("Чемпион");
		archetype.setEnglishName("Champion");
		archetype.setLevel((byte) 3);
		archetype.setDescription(List.of("Описание чемпиона"));
		archetype.setGenitiveName("чемпиона");
		archetype.setHeroClass(heroClass);
		archetype.setBook(book);
		archetype.setFeatureLevelDefenitions(Collections.emptyList());
		archetype.setFeats(Arrays.asList(
			archetypeTrait(12, "Умение подкласса 7", 7),
			archetypeTrait(11, "Умение подкласса 3", 3)
		));
		heroClass.setArchetypes(Collections.singletonList(archetype));

		ClassDetailApi details = new ClassDetailApi(
			archetype,
			Collections.emptyList(),
			new ClassRequestApi()
		);
		List<ClassDetailApi.TraitApi> features = new ArrayList<>(details.getTraits().getFeatures());

		assertEquals(Arrays.asList(1, 3, 5, 7), features.stream()
			.map(ClassDetailApi.TraitApi::getLevel)
			.collect(Collectors.toList()));
		assertEquals(Arrays.asList(
			"Умение класса 1",
			"Умение подкласса 3",
			"Умение класса 5",
			"Умение подкласса 7"
		), features.stream().map(ClassDetailApi.TraitApi::getName).collect(Collectors.toList()));
		assertEquals("Чемпион", details.getTraits().getArchetype().getName());
		assertEquals(3, details.getTraits().getArchetype().getLevel());
		assertEquals(List.of("Описание чемпиона"), details.getTraits().getArchetype().getDescription());
	}

	@Test
	void exposesMechanicsOfClassAndFeaturesInJson() {
		HeroClass heroClass = baseClass();
		ClassMechanics classMechanics = new ClassMechanics();
		ProficiencyGrant proficiencies = new ProficiencyGrant();
		proficiencies.setSavingThrows(EnumSet.of(AbilityType.STRENGTH, AbilityType.CONSTITUTION));
		classMechanics.setProficiencies(proficiencies);
		heroClass.setMechanics(classMechanics);
		ActiveEffect effect = new ActiveEffect();
		effect.setName("Защита без доспехов");
		heroClass.setActiveEffects(List.of(effect));

		HeroClassTrait withMechanics = classTrait(1, "Ярость", 1);
		ResourceCounter counter = new ResourceCounter();
		counter.setKey("rage");
		counter.setMax("2");
		ClassMechanics traitMechanics = new ClassMechanics();
		traitMechanics.setCounters(List.of(counter));
		withMechanics.setMechanics(traitMechanics);
		heroClass.setTraits(Arrays.asList(withMechanics, classTrait(2, "Безрассудная атака", 2)));
		heroClass.setArchetypes(Collections.emptyList());

		JsonNode json = JsonMapper.builder().build()
			.valueToTree(new ClassDetailApi(heroClass, Collections.emptyList(), new ClassRequestApi()));

		assertEquals("STRENGTH", json.at("/mechanics/proficiencies/savingThrows/0").asString());
		assertEquals("CONSTITUTION", json.at("/mechanics/proficiencies/savingThrows/1").asString());
		assertEquals("Защита без доспехов", json.at("/activeEffects/0/name").asString());
		assertEquals("rage", json.at("/traits/features/0/mechanics/counters/0/key").asString());
		assertEquals("rage", json.at("/classTraits/0/mechanics/counters/0/key").asString());
		// незаполненная механика не выводится вовсе (NON_NULL), как в core-api
		assertFalse(json.at("/traits/features/1").has("mechanics"));
		assertFalse(json.at("/traits/features/1").has("activeEffects"));
		assertFalse(json.at("/mechanics/proficiencies").has("skills"));
	}

	@Test
	void expandsFeatureScalingIntoLevelTable() {
		HeroClass heroClass = baseClass();
		HeroClassTrait asi = classTrait(5, "Увеличение характеристик", 4);
		asi.setScaling(List.of(scaling(8, null, null, false), scaling(12, null, null, true)));
		heroClass.setTraits(List.of(asi));
		Archetype archetype = baseArchetype(heroClass);
		ArchetypeTrait feat = archetypeTrait(11, "Боевое превосходство", 3);
		feat.setScaling(List.of(scaling(7, null, "(2)", false)));
		archetype.setFeats(List.of(feat));

		ClassDetailApi details = new ClassDetailApi(archetype, Collections.emptyList(), new ClassRequestApi());
		List<ClassDetailApi.LevelRowApi> levels = new ArrayList<>(details.getTraits().getLevels());

		ClassDetailApi.TraitLinkApi eighth = levels.get(7).getTraits().iterator().next();
		assertEquals("увеличение характеристик", eighth.getName());
		assertEquals("#c5", eighth.getAnchor());
		// строка, скрытая в подклассе, в таблице архетипа не появляется
		assertTrue(levels.get(11).getTraits().isEmpty());
		ClassDetailApi.TraitLinkApi seventh = levels.get(6).getTraits().iterator().next();
		assertEquals("боевое превосходство (2)", seventh.getName());
		assertEquals("#a11", seventh.getAnchor());
		assertEquals(2, details.getTraits().getFeatures().stream()
			.filter(trait -> trait.getName().equals("Увеличение характеристик"))
			.findFirst().orElseThrow().getScaling().size());
	}

	private FeatureScaling scaling(int level, String name, String additional, boolean hide) {
		FeatureScaling row = new FeatureScaling();
		row.setLevel(level);
		row.setName(name);
		row.setAdditional(additional);
		row.setHideInSubclasses(hide);
		return row;
	}

	private HeroClass baseClass() {
		Book book = new Book("TEST");
		book.setName("Test Book");
		book.setType(TypeBook.OFFICAL);
		HeroClass heroClass = new HeroClass();
		heroClass.setId(1);
		heroClass.setName("Воин");
		heroClass.setEnglishName("Fighter");
		heroClass.setAccusativeName("Воина из базы");
		heroClass.setBook(book);
		heroClass.setSpellcasterType(SpellcasterType.NONE);
		heroClass.setAvailableSkills(Collections.emptyList());
		heroClass.setPrimaryAbilities(Collections.emptyList());
		heroClass.setFeatureLevelDefenitions(Collections.emptyList());
		heroClass.setLevelDefenitions(Collections.emptyList());
		heroClass.setTraits(Collections.emptyList());
		return heroClass;
	}

	private Archetype baseArchetype(HeroClass heroClass) {
		Archetype archetype = new Archetype();
		archetype.setId(10);
		archetype.setName("Чемпион");
		archetype.setEnglishName("Champion");
		archetype.setLevel((byte) 3);
		archetype.setDescription(List.of("Описание чемпиона"));
		archetype.setGenitiveName("чемпиона");
		archetype.setHeroClass(heroClass);
		archetype.setBook(heroClass.getBook());
		archetype.setFeatureLevelDefenitions(Collections.emptyList());
		archetype.setFeats(Collections.emptyList());
		heroClass.setArchetypes(Collections.singletonList(archetype));
		return archetype;
	}

	private ArchetypeSpell archetypeSpell(int level, String name, String englishName, String advanced) {
		Spell spell = new Spell();
		spell.setName(name);
		spell.setEnglishName(englishName);
		ArchetypeSpell archetypeSpell = new ArchetypeSpell();
		archetypeSpell.setLevel(level);
		archetypeSpell.setAdvenced(advanced);
		archetypeSpell.setSpell(spell);
		return archetypeSpell;
	}

	private HeroClassTrait classTrait(int id, String name, int level) {
		HeroClassTrait trait = new HeroClassTrait();
		trait.setId(id);
		trait.setName(name);
		trait.setLevel((byte) level);
		return trait;
	}

	private HeroClassTrait archetypeSelectionTrait(int id, String name, int level) {
		HeroClassTrait trait = classTrait(id, name, level);
		trait.setArchitype(true);
		return trait;
	}

	private ArchetypeTrait archetypeTrait(int id, String name, int level) {
		ArchetypeTrait trait = new ArchetypeTrait();
		ReflectionTestUtils.setField(trait, "id", id);
		ReflectionTestUtils.setField(trait, "name", name);
		ReflectionTestUtils.setField(trait, "level", (byte) level);
		return trait;
	}
}
