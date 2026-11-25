package club.dnd5.portal.config.liquibase;

import club.dnd5.portal.config.liquibase.MechanicsBackfill.RaceBonus;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.mechanics.AbilityBonus;
import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.mechanics.FeatMechanics;
import club.dnd5.portal.model.mechanics.MechanicChoice;
import club.dnd5.portal.model.mechanics.RaceMechanics;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Set;

import static club.dnd5.portal.model.AbilityType.CHARISMA;
import static club.dnd5.portal.model.AbilityType.CHOICE;
import static club.dnd5.portal.model.AbilityType.CHOICE_DOUBLE;
import static club.dnd5.portal.model.AbilityType.CHOICE_UNIQUE;
import static club.dnd5.portal.model.AbilityType.CONSTITUTION;
import static club.dnd5.portal.model.AbilityType.DEXTERITY;
import static club.dnd5.portal.model.AbilityType.INTELLIGENCE;
import static club.dnd5.portal.model.AbilityType.ONE;
import static club.dnd5.portal.model.AbilityType.STRENGTH;
import static club.dnd5.portal.model.AbilityType.WISDOM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MechanicsBackfillTest {
	private static final List<AbilityType> ALL = List.of(STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA);

	@Test
	void classSavingThrowsAndSkillChoice() {
		ClassMechanics mechanics = MechanicsBackfill.classMechanics("Сила, Телосложение",
			List.of(SkillType.ATHLETICS, SkillType.PERCEPTION), 2);

		assertEquals(Set.of(STRENGTH, CONSTITUTION), mechanics.getProficiencies().getSavingThrows());
		MechanicChoice choice = mechanics.getChoices().get(0);
		assertEquals("class-skills", choice.getKey());
		assertEquals(2, choice.getCount());
		assertEquals(List.of("ATHLETICS", "PERCEPTION"), choice.getOptions().stream().map(o -> o.getValue()).toList());
		assertEquals("Внимательность", choice.getOptions().get(1).getName());
		// порядок ключей не важен: Jackson 3 по умолчанию сортирует свойства
		JsonMapper mapper = JsonMapper.builder().build();
		assertEquals(mapper.readTree("{\"proficiencies\":{\"savingThrows\":[\"STRENGTH\",\"CONSTITUTION\"]},"
				+ "\"choices\":[{\"key\":\"class-skills\",\"type\":\"SKILL\",\"count\":2,"
				+ "\"options\":[{\"value\":\"ATHLETICS\",\"name\":\"Атлетика\"},{\"value\":\"PERCEPTION\",\"name\":\"Внимательность\"}]}]}"),
			mapper.valueToTree(mechanics));
	}

	@Test
	void classWithAnySkillsHasNoOptionsAndNothingToFillIsNull() {
		ClassMechanics bard = MechanicsBackfill.classMechanics("<p>Ловкость и Харизма</p>", List.of(), 3);
		assertEquals(Set.of(DEXTERITY, CHARISMA), bard.getProficiencies().getSavingThrows());
		assertNull(bard.getChoices().get(0).getOptions());

		assertNull(MechanicsBackfill.classMechanics(null, List.of(SkillType.ARCANA), 0));
		assertEquals(Set.of(WISDOM, INTELLIGENCE), MechanicsBackfill.parseAbilities("Мудрости, интеллект"));
	}

	@Test
	void raceFixedAndChoiceBonusesAreSeparateCumulativeEntries() {
		// полуэльф: Харизма +2 и +1 к двум другим
		List<AbilityBonus> bonuses = MechanicsBackfill.raceAbilityBonuses(
			List.of(new RaceBonus(CHARISMA, 2), new RaceBonus(CHOICE_UNIQUE, 1)), List.of());

		assertEquals(2, bonuses.size());
		assertEquals(List.of(CHARISMA), bonuses.get(0).getAbilities());
		assertEquals(2, bonuses.get(0).getBonus());
		assertNull(bonuses.get(0).getCount());
		assertEquals(List.of(STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM), bonuses.get(1).getAbilities());
		assertEquals(2, bonuses.get(1).getCount());
		assertEquals(1, bonuses.get(1).getBonus());
	}

	@Test
	void subraceChoiceExcludesAbilitiesRaisedByParentAndChoicesMerge() {
		List<AbilityBonus> bonuses = MechanicsBackfill.raceAbilityBonuses(
			List.of(new RaceBonus(CHOICE, 1), new RaceBonus(CHOICE, 1), new RaceBonus(ONE, 1)),
			List.of(new RaceBonus(STRENGTH, 2)));

		assertEquals(2, bonuses.size());
		assertEquals(List.of(DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA), bonuses.get(0).getAbilities());
		assertEquals(2, bonuses.get(0).getCount());
		assertEquals(ALL, bonuses.get(1).getAbilities());
		assertEquals(1, bonuses.get(1).getCount());
	}

	@Test
	void customOriginBecomesPlusTwoAndPlusOne() {
		List<AbilityBonus> bonuses = MechanicsBackfill.raceAbilityBonuses(List.of(new RaceBonus(CHOICE_DOUBLE, null)), List.of());

		assertEquals(2, bonuses.size());
		assertEquals(2, bonuses.get(0).getBonus());
		assertEquals(1, bonuses.get(1).getBonus());
		assertEquals(ALL, bonuses.get(1).getAbilities());
	}

	@Test
	void raceSensesAndSpeeds() {
		RaceMechanics mechanics = MechanicsBackfill.raceMechanics(List.of(), List.of(), 60, null, 0, 30);

		assertNull(mechanics.getAbilityBonuses());
		assertEquals(60, mechanics.getModifiers().getSenses().get(0).getRange());
		assertNull(mechanics.getModifiers().getSpeed().getClimb());
		assertEquals(30, mechanics.getModifiers().getSpeed().getSwim());
		assertNull(MechanicsBackfill.raceMechanics(List.of(), List.of(), 0, null, null, null));
	}

	@Test
	void skillsBecomeProficiencies() {
		assertEquals(Set.of(SkillType.INSIGHT, SkillType.RELIGION),
			MechanicsBackfill.backgroundMechanics(List.of(SkillType.INSIGHT, SkillType.RELIGION)).getProficiencies().getSkills());
		assertEquals(Set.of(SkillType.PERCEPTION),
			MechanicsBackfill.raceFeatureMechanics(List.of(SkillType.PERCEPTION)).getProficiencies().getSkills());
		assertNull(MechanicsBackfill.backgroundMechanics(List.of()));
		assertNull(MechanicsBackfill.raceFeatureMechanics(null));
	}

	@Test
	void featIncreaseOnlyWhenDescriptionNamesIt() {
		FeatMechanics mechanics = MechanicsBackfill.featMechanics(List.of(STRENGTH, DEXTERITY),
			List.of("Значение вашей Силы или Ловкости увеличивается на 1, но не более чем до 20."));
		AbilityBonus bonus = mechanics.getAbilityBonuses().get(0);

		assertEquals(List.of(STRENGTH, DEXTERITY), bonus.getAbilities());
		assertEquals(1, bonus.getBonus());
		assertEquals(1, bonus.getCount());
		assertEquals(20, bonus.getUpto());
		assertNull(MechanicsBackfill.featMechanics(List.of(STRENGTH), List.of("Требуется Сила 13.")));
	}
}
