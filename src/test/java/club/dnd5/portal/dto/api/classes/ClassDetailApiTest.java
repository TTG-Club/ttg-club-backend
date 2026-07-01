package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpell;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.model.splells.Spell;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassDetailApiTest {
	@Test
	void exposesArchetypeSpellsGroupedByCharacterLevel() {
		HeroClass heroClass = baseClass();
		Archetype archetype = baseArchetype(heroClass);
		archetype.setSpells(Arrays.asList(
			archetypeSpell(3, "Щит", "Shield", "всегда подготовлено"),
			archetypeSpell(1, "Свет", "Light", null),
			archetypeSpell(0, "Игнорируется", "Ignored", null)
		));

		ClassDetailApi details = new ClassDetailApi(archetype, Collections.emptyList(), new ClassRequestApi());
		List<ClassDetailApi.ArchetypeSpellLevelApi> levels = new ArrayList<>(details.getArchetypeSpells());

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
		archetype.setDescription("Описание чемпиона");
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
		assertEquals("Описание чемпиона", details.getTraits().getArchetype().getDescription());
	}

	private HeroClass baseClass() {
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
		heroClass.setTraits(Collections.emptyList());
		return heroClass;
	}

	private Archetype baseArchetype(HeroClass heroClass) {
		Archetype archetype = new Archetype();
		archetype.setId(10);
		archetype.setName("Чемпион");
		archetype.setEnglishName("Champion");
		archetype.setLevel((byte) 3);
		archetype.setDescription("Описание чемпиона");
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
