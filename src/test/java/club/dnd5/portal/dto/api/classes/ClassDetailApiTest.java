package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
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
			classTrait(1, "Умение класса 1", 1)
		));

		Archetype archetype = new Archetype();
		archetype.setId(10);
		archetype.setName("Чемпион");
		archetype.setEnglishName("Champion");
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
	}

	private HeroClassTrait classTrait(int id, String name, int level) {
		HeroClassTrait trait = new HeroClassTrait();
		trait.setId(id);
		trait.setName(name);
		trait.setLevel((byte) level);
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
