package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityBonus;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.races.Feature;
import club.dnd5.portal.model.races.Race;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VttgRaceMapperTest {
	private final VttgRaceMapper mapper = new VttgRaceMapper();

	@Test
	void raceCarriesFixedIncreaseAndSpeciesFields() {
		Race dwarf = race("Дварф", "Dwarf", bonus(AbilityType.CONSTITUTION, 2));
		dwarf.setDarkvision(60);
		dwarf.setSpeed(25);

		Map<String, Object> data = mapper.toVttg(dwarf);

		assertThat(data).containsEntry("type", "species")
			.containsEntry("id", "dwarf")
			.containsEntry("key", "dwarf")
			.containsEntry("section", "species")
			.containsEntry("srcSection", "races")
			.containsEntry("srcUrl", "dwarf")
			.containsEntry("creatureType", "humanoid")
			.containsEntry("size", Collections.singletonList("medium"))
			.containsEntry("sourceKey", "phb")
			.doesNotContainKey("parentKey");
		assertThat(asMap(data.get("speed"))).containsEntry("walk", 25);
		Map<String, Object> featData = asMap(data.get("featData"));
		assertThat(featData).containsEntry("darkvision", 60);
		assertThat(asMap(asMap(featData.get("abilityScoreIncrease")).get("fixed"))).containsEntry("constitution", 2);
	}

	@Test
	void subraceReferencesParentAndKeepsOwnIncrease() {
		Race dwarf = race("Дварф", "Dwarf", bonus(AbilityType.CONSTITUTION, 2));
		Race hill = race("Холмовой", "Hill", bonus(AbilityType.WISDOM, 1));
		hill.setParent(dwarf);
		hill.setType(null);

		Map<String, Object> data = mapper.toVttg(hill);

		assertThat(data).containsEntry("id", "dwarf-hill")
			.containsEntry("parentKey", "dwarf")
			.containsEntry("creatureType", "humanoid")
			.doesNotContainKeys("srcSection", "srcUrl");
		Map<String, Object> increase = asMap(asMap(data.get("featData")).get("abilityScoreIncrease"));
		assertThat(asMap(increase.get("fixed"))).containsOnlyKeys("wisdom");
	}

	@Test
	@SuppressWarnings("unchecked")
	void halfElfChoosesTwoOtherAbilities() {
		Race halfElf = race("Полуэльф", "Half-Elf",
			bonus(AbilityType.CHARISMA, 2), bonus(AbilityType.CHOICE_UNIQUE, 1));

		Map<String, Object> increase = asMap(asMap(mapper.toVttg(halfElf).get("featData")).get("abilityScoreIncrease"));

		assertThat(asMap(increase.get("fixed"))).containsEntry("charisma", 2);
		Map<String, Object> choice = asMap(increase.get("choice"));
		assertThat(choice).containsEntry("amount", 1).containsEntry("count", 2);
		assertThat((List<Object>) choice.get("from")).hasSize(5).doesNotContain("charisma");
	}

	@Test
	void secondChoiceGroupBecomesSeparateFeature() {
		Race custom = race("Гит", "Gith", bonus(AbilityType.CHOICE_DOUBLE, 1));

		Map<String, Object> data = mapper.toVttg(custom);

		Map<String, Object> first = asMap(asMap(asMap(data.get("featData")).get("abilityScoreIncrease")).get("choice"));
		assertThat(first).containsEntry("amount", 2).containsEntry("count", 1);
		List<?> features = (List<?>) data.get("features");
		Map<String, Object> asi = asMap(features.get(0));
		assertThat(asi).containsEntry("key", "gith-asi-1").containsEntry("name", "Увеличение характеристик");
		Map<String, Object> second = asMap(asMap(asMap(asi.get("featData")).get("abilityScoreIncrease")).get("choice"));
		assertThat(second).containsEntry("amount", 1).containsEntry("count", 1);
	}

	@Test
	void humanGetsPlusOneEverywhere() {
		Race human = race("Человек", "Human",
			bonus(AbilityType.STRENGTH, 1), bonus(AbilityType.DEXTERITY, 1), bonus(AbilityType.CONSTITUTION, 1),
			bonus(AbilityType.INTELLIGENCE, 1), bonus(AbilityType.WISDOM, 1), bonus(AbilityType.CHARISMA, 1));

		Map<String, Object> increase = asMap(asMap(mapper.toVttg(human).get("featData")).get("abilityScoreIncrease"));

		assertThat(asMap(increase.get("fixed"))).hasSize(6);
		assertThat(increase).doesNotContainKey("choice");
	}

	@Test
	void featureCarriesSkillsAndLanguages() {
		Race elf = race("Эльф", "Elf");
		Feature senses = new Feature();
		senses.setId(7);
		senses.setName("Обострённые чувства");
		senses.setEnglishName("Keen Senses");
		senses.setDescription("<p>Вы владеете навыком <a href=\"/skills/perception\">Внимательность</a>.</p>");
		senses.setSkills(Collections.singletonList(SkillType.PERCEPTION));
		Language elvish = new Language();
		elvish.setName("Эльфийский");
		senses.setLanuages(Collections.singletonList(elvish));
		elf.setFeatures(Collections.singletonList(senses));

		Map<String, Object> feature = asMap(((List<?>) mapper.toVttg(elf).get("features")).get(0));

		assertThat(feature).containsEntry("key", "elf-keen_senses");
		assertThat((String) feature.get("description")).contains("href=\"https://ttg.club/skills/perception\"");
		Map<String, Object> featData = asMap(feature.get("featData"));
		assertThat(featData).containsEntry("skillProficiencies", Collections.singletonList("perception"))
			.containsEntry("languages", Collections.singletonList("Эльфийский"));
	}

	@Test
	void smallOrMediumGivesBothSizes() {
		Race race = race("Мутант", "Mutant");
		race.setSize(CreatureSize.SMALL_MEDIUM);

		assertThat(mapper.toVttg(race)).containsEntry("size", Arrays.asList("medium", "small"));
	}

	private static Race race(String name, String englishName, AbilityBonus... bonuses) {
		Book book = new Book("PHB");
		Race race = new Race();
		race.setName(name);
		race.setEnglishName(englishName);
		race.setBook(book);
		race.setType(CreatureType.HUMANOID);
		race.setSize(CreatureSize.MEDIUM);
		race.setSpeed(30);
		race.setBonuses(new ArrayList<>(Arrays.asList(bonuses)));
		race.setFeatures(new ArrayList<>());
		return race;
	}

	private static AbilityBonus bonus(AbilityType ability, int value) {
		AbilityBonus bonus = new AbilityBonus();
		bonus.setAbility(ability);
		bonus.setBonus((byte) value);
		return bonus;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return (Map<String, Object>) value;
	}
}
