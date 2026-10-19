package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.ActionType;
import club.dnd5.portal.model.creature.Condition;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.CreatureFeat;
import club.dnd5.portal.model.creature.CreatureSpell;
import club.dnd5.portal.model.creature.HabitatType;
import club.dnd5.portal.model.creature.SavingThrow;
import club.dnd5.portal.model.creature.Skill;
import club.dnd5.portal.model.creature.Spellcater;
import club.dnd5.portal.model.splells.Spell;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VttgCreatureMapperTest {
	private final VttgCreatureMapper mapper = new VttgCreatureMapper(new VttgSpellMapper());

	@Test
	void goblinCarriesStatBlockAndParsedAttack() {
		Creature goblin = creature("Гоблин", "Goblin");
		goblin.setSize(CreatureSize.SMALL);
		goblin.setType(CreatureType.HUMANOID);
		goblin.setRaceName("гоблиноид");
		goblin.setAlignment(Alignment.NEUTRAL_EVIL);
		goblin.setAC((byte) 15);
		goblin.setBonusAC("кожаный доспех, щит");
		goblin.setCountDiceHp((short) 2);
		goblin.setDexterity((byte) 14);
		goblin.setChallengeRating("1/4");
		goblin.setPassivePerception((byte) 9);
		goblin.setDarkvision(60);
		goblin.setSkills(Collections.singletonList(new Skill(SkillType.STEALTH, (byte) 6)));
		goblin.setHabitates(Arrays.asList(HabitatType.FOREST, HabitatType.RUINS));
		Action scimitar = action("Скимитар", ActionType.ACTION,
			"<p><em>Рукопашная атака оружием:</em> +4 к попаданию, досягаемость 5 фт., одна цель. "
				+ "<em>Попадание:</em> 5 (<dice-roller formula=\"1к6 + 2\">1к6 + 2</dice-roller>) рубящего урона.</p>");
		goblin.setActions(Collections.singletonList(scimitar));

		Map<String, Object> data = mapper.toVttg(goblin, "/img/goblin.webp");

		assertThat(data).containsEntry("id", "goblin")
			.containsEntry("type", "creature")
			.containsEntry("section", "creatures")
			.containsEntry("srcSection", "bestiary")
			.containsEntry("header", "Маленький гуманоид (гоблиноид), нейтрально-злой")
			.doesNotContainKey("spells");
		assertThat(asMap(data.get("token"))).containsEntry("imageUrl", "https://ttg.club/img/goblin.webp")
			.containsEntry("scale", 1);
		Map<String, Object> system = asMap(data.get("system"));
		assertThat(system).containsEntry("size", "small")
			.containsEntry("type", "humanoid")
			.containsEntry("subtype", "гоблиноид")
			.containsEntry("alignment", "neutral-evil")
			.containsEntry("challengeRating", "1/4")
			.containsEntry("proficiencyBonus", 2)
			.containsEntry("initiativeBonus", 2)
			.containsEntry("environments", Collections.singletonList("forest"))
			.containsEntry("customEnvironments", "руины")
			.containsEntry("senses", "тёмное зрение 60 фт., пассивная Внимательность 9");
		assertThat(asMap(system.get("skills"))).containsEntry("stealth", "expertise");
		assertThat(asMap(system.get("armorClass"))).containsEntry("value", 15)
			.containsEntry("formula", "кожаный доспех, щит");
		assertThat(asMap(system.get("hitPoints"))).containsEntry("average", 7)
			.containsEntry("formula", "2к6")
			.containsEntry("hitDie", 6);

		Map<String, Object> attack = asList(system.get("actions")).get(0);
		assertThat(attack).containsEntry("name", "Скимитар")
			.containsEntry("attackBonus", 4)
			.containsEntry("reach", 5)
			.containsEntry("rangeType", "melee")
			.containsEntry("distanceUnit", "ft");
		assertThat(asList(attack.get("damageParts"))).singleElement()
			.satisfies(part -> assertThat(part).containsEntry("formula", "1к6+2").containsEntry("type", "slashing"));
	}

	@Test
	void defensesSplitIntoKeysAndText() {
		Creature golem = creature("Голем", "Golem");
		golem.setImmunityDamages(Arrays.asList(DamageType.POISON, DamageType.FAIR, DamageType.NO_ADMANTIT));
		golem.setResistanceDamages(Collections.singletonList(DamageType.NECTOTIC));
		golem.setImmunityStates(Arrays.asList(Condition.CHARMED, Condition.POISONED));

		Map<String, Object> defenses = asMap(asMap(mapper.toVttg(golem, null).get("system")).get("defenses"));

		assertThat(defenses).containsEntry("immunities", Arrays.asList("poison", "fire"))
			.containsEntry("resistances", Collections.singletonList("necrotic"))
			.containsEntry("conditionImmunities", Arrays.asList("charmed", "poisoned"))
			.containsKey("immunitiesText")
			.doesNotContainKey("resistancesText");
	}

	@Test
	void breathWeaponHasSaveAreaAndRecharge() {
		Creature dragon = creature("Красный дракон", "Red Dragon");
		dragon.setSavingThrows(Collections.singletonList(new SavingThrow(AbilityType.DEXTERITY, (byte) 6)));
		dragon.setLegendary("<p>Дракон может совершить 3 легендарных действия.</p>");
		dragon.setActions(Arrays.asList(
			action("Огненное дыхание (Перезарядка 5–6)", ActionType.ACTION,
				"<p>Дракон выдыхает огонь 60-футовым конусом. Каждое существо в этой области должно совершить "
					+ "спасбросок Ловкости Сл 21, получая 63 (<dice-roller formula=\"18к6\"/>) урона огнём при провале, "
					+ "или половину этого урона при успехе.</p>"),
			action("Обнаружение", ActionType.LEGENDARY, "<p>Дракон совершает проверку Мудрости.</p>")));

		Map<String, Object> system = asMap(mapper.toVttg(dragon, null).get("system"));

		assertThat(system).containsEntry("savingThrows", Collections.singletonList("dexterity"));
		Map<String, Object> breath = asList(system.get("actions")).get(0);
		assertThat(breath).containsEntry("name", "Огненное дыхание")
			.containsEntry("recharge", "d5")
			.containsEntry("saveType", "dexterity")
			.containsEntry("saveDC", 21)
			.containsEntry("saveEffect", "half")
			.doesNotContainKey("attackBonus");
		assertThat(asMap(breath.get("areaOfEffect"))).containsEntry("shape", "cone").containsEntry("size", 60);
		assertThat(asList(breath.get("damageParts"))).singleElement()
			.satisfies(part -> assertThat(part).containsEntry("formula", "18к6").containsEntry("type", "fire"));
		Map<String, Object> legendary = asMap(system.get("legendary"));
		assertThat(legendary).containsEntry("count", 3);
		assertThat(asList(legendary.get("actions"))).hasSize(1);
	}

	@Test
	void spellcasterGetsSlotGroupsAndEmbeddedSpells() {
		Creature mage = creature("Маг", "Mage");
		Spellcater caster = new Spellcater();
		caster.setLevel((byte) 9);
		caster.setDc((byte) 14);
		caster.setAttack((byte) 6);
		caster.setSpellAbility(AbilityType.INTELLIGENCE);
		caster.setSlot1((byte) 4);
		mage.setSpellcasters(Collections.singletonList(caster));
		CreatureSpell missile = creatureSpell(spell("Волшебная стрела", "Magic Missile", 1), 0);
		CreatureSpell light = creatureSpell(spell("Свет", "Light", 0), 0);
		CreatureSpell invisibility = creatureSpell(spell("Невидимость", "Invisibility", 2), 1);
		mage.setSpells(Arrays.asList(missile, light, invisibility));

		Map<String, Object> data = mapper.toVttg(mage, null);

		assertThat(asList(data.get("spells"))).extracting(spell -> spell.get("id"))
			.containsExactly("light", "magic_missile", "invisibility");
		assertThat(asList(data.get("spells")).get(0)).containsEntry("spellcastingAbility", "intelligence");
		Map<String, Object> system = asMap(data.get("system"));
		assertThat(asMap(system.get("spellcasting"))).containsEntry("ability", "intelligence")
			.containsEntry("saveDC", 14).containsEntry("attackBonus", 6);
		Map<String, Object> block = asList(system.get("spellcastingBlocks")).get(0);
		assertThat(block).containsEntry("name", "Использование заклинаний");
		List<Map<String, Object>> groups = asList(block.get("groups"));
		assertThat(groups).extracting(group -> group.get("mode"))
			.containsExactly("atWill", "perDayPool", "perDayEach");
		assertThat(groups.get(1)).containsEntry("count", 4);
		assertThat(groups.get(2)).containsEntry("count", 1);
	}

	@Test
	void markdownTraitIsExpandedWithoutTouchingEntity() {
		Creature wolf = creature("Волк", "Wolf");
		CreatureFeat pack = new CreatureFeat();
		pack.setName("Тактика стаи");
		pack.setMarkdown(true);
		pack.setDescription("{@beast_name} совершает с преимуществом бросок атаки {@hit 4}.");
		wolf.setFeats(Collections.singletonList(pack));

		Map<String, Object> trait = asList(asMap(mapper.toVttg(wolf, null).get("system")).get("traits")).get(0);

		assertThat(asStrings(trait.get("description")).get(0)).startsWith("Волк совершает").contains("+4");
		assertThat(pack.getDescription()).startsWith("{@beast_name}");
	}

	private static Creature creature(String name, String englishName) {
		Creature creature = new Creature();
		creature.setName(name);
		creature.setEnglishName(englishName);
		creature.setBook(new Book("MM"));
		creature.setSize(CreatureSize.MEDIUM);
		creature.setType(CreatureType.HUMANOID);
		creature.setAlignment(Alignment.WITHOUT);
		creature.setChallengeRating("1");
		creature.setSavingThrows(new ArrayList<>());
		creature.setSkills(new ArrayList<>());
		creature.setActions(new ArrayList<>());
		creature.setFeats(new ArrayList<>());
		creature.setSpells(new ArrayList<>());
		creature.setSpellcasters(new ArrayList<>());
		creature.setLanguages(new ArrayList<>());
		creature.setHabitates(new ArrayList<>());
		creature.setImmunityDamages(new ArrayList<>());
		creature.setResistanceDamages(new ArrayList<>());
		creature.setVulnerabilityDamages(new ArrayList<>());
		creature.setImmunityStates(new ArrayList<>());
		creature.setArmorTypes(new ArrayList<>());
		return creature;
	}

	private static Action action(String name, ActionType type, String description) {
		Action action = new Action();
		action.setName(name);
		action.setActionType(type);
		action.setDescription(description);
		return action;
	}

	private static Spell spell(String name, String englishName, int level) {
		Spell spell = new Spell();
		spell.setName(name);
		spell.setEnglishName(englishName);
		spell.setLevel((byte) level);
		spell.setDistance("120 футов");
		spell.setDuration("Мгновенная");
		return spell;
	}

	private static CreatureSpell creatureSpell(Spell spell, int count) {
		CreatureSpell creatureSpell = new CreatureSpell();
		creatureSpell.setSpell(spell);
		creatureSpell.setCount((byte) count);
		return creatureSpell;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return (Map<String, Object>) value;
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> asList(Object value) {
		return (List<Map<String, Object>>) value;
	}

	@SuppressWarnings("unchecked")
	private static List<String> asStrings(Object value) {
		return (List<String>) value;
	}
}
