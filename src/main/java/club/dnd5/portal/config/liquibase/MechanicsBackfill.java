package club.dnd5.portal.config.liquibase;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.mechanics.AbilityBonus;
import club.dnd5.portal.model.mechanics.ChoiceOption;
import club.dnd5.portal.model.mechanics.ChoiceType;
import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.mechanics.FeatMechanics;
import club.dnd5.portal.model.mechanics.MechanicChoice;
import club.dnd5.portal.model.mechanics.ProficiencyGrant;
import club.dnd5.portal.model.mechanics.RaceMechanics;
import club.dnd5.portal.model.mechanics.SenseGrant;
import club.dnd5.portal.model.mechanics.SenseType;
import club.dnd5.portal.model.mechanics.SheetModifiers;
import club.dnd5.portal.model.mechanics.SpeedModifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Сборка механики (db/changelog 0045) из колонок, которые в базе уже лежат структурированно.
 *
 * <p>Только надёжные источники: списки enum, отдельные таблицы и короткие поля со словарными
 * значениями. Всё, что описано только текстом (владение доспехами и оружием, языки, заклинания
 * рас и архетипов, ресурсы таблицы класса), сюда не идёт — механика из разбора свободного
 * текста оказалась бы правдоподобной, но неверной.</p>
 *
 * <p>Смысл полей повторяет выгрузку VTTG ({@code service.vttg}): прибавки рас — как
 * {@code VttgRaceMapper.abilityGroups}, повышение черты — как {@code VttgFeatMapper}, спасброски
 * класса — как {@code VttgClassMapper}. Методы чистые, без базы: их проверяет
 * {@code MechanicsBackfillTest}.</p>
 */
final class MechanicsBackfill {
	/** Ключ выбора навыков класса; на него лист вешает сделанный выбор. */
	static final String CLASS_SKILLS_KEY = "class-skills";
	/** Предел повышения характеристики чертой 5e 2014. */
	static final int FEAT_ABILITY_LIMIT = 20;

	private static final Set<AbilityType> BASE_ABILITIES = AbilityType.getBaseAbility();
	private static final Pattern TAG = Pattern.compile("<[^>]+>");
	private static final Pattern ABILITY_WORD = Pattern.compile(
		"(?<![\\p{L}])(сил|ловк|телослож|интеллект|мудр|хариз)", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

	private MechanicsBackfill() {
	}

	/**
	 * Механика класса: спасброски из строки ({@code classes.saving_throws}, «Сила, Телосложение»)
	 * и выбор навыков ({@code available_skills} + {@code skill_available_count}).
	 *
	 * @return механика; {@code null} — заполнять нечего.
	 */
	static ClassMechanics classMechanics(String savingThrows, List<SkillType> availableSkills, int skillCount) {
		ClassMechanics mechanics = new ClassMechanics();
		Set<AbilityType> saves = parseAbilities(savingThrows);
		if (!saves.isEmpty()) {
			ProficiencyGrant proficiencies = new ProficiencyGrant();
			proficiencies.setSavingThrows(saves);
			mechanics.setProficiencies(proficiencies);
		}
		if (skillCount > 0) {
			MechanicChoice choice = new MechanicChoice();
			choice.setKey(CLASS_SKILLS_KEY);
			choice.setType(ChoiceType.SKILL);
			choice.setCount(skillCount);
			List<ChoiceOption> options = distinct(availableSkills).stream()
				.map(skill -> new ChoiceOption(skill.name(), skill.getCyrilicName()))
				.collect(Collectors.toList());
			// пустой список на сайте — «любые навыки», в механике это отсутствие options
			choice.setOptions(options.isEmpty() ? null : options);
			mechanics.setChoices(List.of(choice));
		}
		return mechanics.getProficiencies() == null && mechanics.getChoices() == null ? null : mechanics;
	}

	/**
	 * Характеристики, названные в строке по-русски. Распознаётся начало слова, как в
	 * {@code VttgDictionaries.abilityByName}: «Сила», «Силы», «Мудрость».
	 */
	static Set<AbilityType> parseAbilities(String text) {
		Set<AbilityType> result = EnumSet.noneOf(AbilityType.class);
		if (text == null) {
			return result;
		}
		Matcher matcher = ABILITY_WORD.matcher(TAG.matcher(text).replaceAll(" "));
		while (matcher.find()) {
			switch (matcher.group(1).toLowerCase(Locale.ROOT)) {
				case "сил" -> result.add(AbilityType.STRENGTH);
				case "ловк" -> result.add(AbilityType.DEXTERITY);
				case "телослож" -> result.add(AbilityType.CONSTITUTION);
				case "интеллект" -> result.add(AbilityType.INTELLIGENCE);
				case "мудр" -> result.add(AbilityType.WISDOM);
				case "хариз" -> result.add(AbilityType.CHARISMA);
				default -> {
					// других начал шаблон не ловит
				}
			}
		}
		return result;
	}

	/** Строка {@code race_bonuses}: характеристика (или вид выбора) и прибавка. */
	record RaceBonus(AbilityType ability, Integer bonus) {
	}

	/**
	 * Механика расы или разновидности: прибавки к характеристикам ({@code race_bonuses}),
	 * тёмное зрение и особые скорости.
	 *
	 * @param own    прибавки самой записи в порядке id.
	 * @param parent прибавки основной расы (для разновидности; иначе пусто) — только чтобы
	 *               «к другой» не предлагала характеристику, которую раса уже подняла.
	 * @return механика; {@code null} — заполнять нечего.
	 */
	static RaceMechanics raceMechanics(List<RaceBonus> own, List<RaceBonus> parent,
									   Integer darkvision, Integer fly, Integer climb, Integer swim) {
		RaceMechanics mechanics = new RaceMechanics();
		List<AbilityBonus> bonuses = raceAbilityBonuses(own, parent);
		mechanics.setAbilityBonuses(bonuses.isEmpty() ? null : bonuses);

		SheetModifiers modifiers = new SheetModifiers();
		if (positive(darkvision)) {
			modifiers.setSenses(List.of(new SenseGrant(SenseType.DARKVISION, darkvision)));
		}
		if (positive(fly) || positive(climb) || positive(swim)) {
			SpeedModifier speed = new SpeedModifier();
			speed.setFly(positive(fly) ? fly : null);
			speed.setClimb(positive(climb) ? climb : null);
			speed.setSwim(positive(swim) ? swim : null);
			modifiers.setSpeed(speed);
		}
		if (modifiers.getSenses() != null || modifiers.getSpeed() != null) {
			mechanics.setModifiers(modifiers);
		}
		return mechanics.getAbilityBonuses() == null && mechanics.getModifiers() == null ? null : mechanics;
	}

	/**
	 * Прибавки расы списком, элементы которого складываются. Фиксированные — по элементу на
	 * характеристику (повторы суммируются); выборы — пул и количество:
	 * <ul>
	 * <li>{@code CHOICE} «к другой» — одна из ещё не поднятых;</li>
	 * <li>{@code CHOICE_UNIQUE} «к 2 другим» — две из ещё не поднятых;</li>
	 * <li>{@code ONE} «к одной» — любая одна;</li>
	 * <li>{@code CHOICE_DOUBLE} «+2 и +1 / +1 к трём» — вариант «+2 к одной и +1 к другой»
	 * (вариант «+1 к трём» этой моделью не выразить — так же поступает выгрузка VTTG).</li>
	 * </ul>
	 * Выборы с одной прибавкой и одним пулом сливаются в один с бо́льшим количеством.
	 */
	static List<AbilityBonus> raceAbilityBonuses(List<RaceBonus> own, List<RaceBonus> parent) {
		Map<AbilityType, Integer> fixed = fixedBonuses(own);
		Set<AbilityType> raised = EnumSet.noneOf(AbilityType.class);
		raised.addAll(fixed.keySet());
		raised.addAll(fixedBonuses(parent).keySet());
		List<AbilityType> others = BASE_ABILITIES.stream().filter(a -> !raised.contains(a)).collect(Collectors.toList());
		List<AbilityType> any = new ArrayList<>(BASE_ABILITIES);

		List<AbilityBonus> result = new ArrayList<>();
		fixed.forEach((ability, bonus) -> result.add(bonus(List.of(ability), bonus, null)));

		List<AbilityBonus> choices = new ArrayList<>();
		for (RaceBonus row : own == null ? List.<RaceBonus>of() : own) {
			int amount = row.bonus() == null ? 1 : row.bonus();
			if (row.ability() == null || amount == 0) {
				continue;
			}
			switch (row.ability()) {
				case CHOICE -> addChoice(choices, amount, 1, others);
				case CHOICE_UNIQUE -> addChoice(choices, amount, 2, others);
				case ONE -> addChoice(choices, amount, 1, any);
				case CHOICE_DOUBLE -> {
					addChoice(choices, 2, 1, any);
					addChoice(choices, 1, 1, any);
				}
				default -> {
					// базовые характеристики уже среди фиксированных
				}
			}
		}
		result.addAll(choices);
		return result;
	}

	private static Map<AbilityType, Integer> fixedBonuses(List<RaceBonus> rows) {
		Map<AbilityType, Integer> fixed = new LinkedHashMap<>();
		for (RaceBonus row : rows == null ? List.<RaceBonus>of() : rows) {
			if (row.ability() != null && BASE_ABILITIES.contains(row.ability()) && row.bonus() != null && row.bonus() != 0) {
				fixed.merge(row.ability(), row.bonus(), Integer::sum);
			}
		}
		return fixed;
	}

	private static void addChoice(List<AbilityBonus> choices, int amount, int count, List<AbilityType> from) {
		if (from.isEmpty()) {
			return;
		}
		for (AbilityBonus choice : choices) {
			if (choice.getBonus() == amount && choice.getAbilities().equals(from)) {
				choice.setCount(choice.getCount() + count);
				return;
			}
		}
		choices.add(bonus(from, amount, count));
	}

	private static AbilityBonus bonus(List<AbilityType> abilities, int amount, Integer count) {
		AbilityBonus bonus = new AbilityBonus();
		bonus.setAbilities(new ArrayList<>(abilities));
		bonus.setBonus(amount);
		bonus.setCount(count);
		return bonus;
	}

	/**
	 * Механика умения расы: навыки ({@code race_features.skills}) — владение без выбора.
	 *
	 * @return механика; {@code null} — навыков нет.
	 */
	static RaceMechanics raceFeatureMechanics(List<SkillType> skills) {
		ProficiencyGrant proficiencies = skillProficiencies(skills);
		if (proficiencies == null) {
			return null;
		}
		RaceMechanics mechanics = new RaceMechanics();
		mechanics.setProficiencies(proficiencies);
		return mechanics;
	}

	/**
	 * Механика предыстории: навыки ({@code backgrounds.skills}) — владение без выбора.
	 *
	 * @return механика; {@code null} — навыков нет.
	 */
	static FeatMechanics backgroundMechanics(List<SkillType> skills) {
		ProficiencyGrant proficiencies = skillProficiencies(skills);
		if (proficiencies == null) {
			return null;
		}
		FeatMechanics mechanics = new FeatMechanics();
		mechanics.setProficiencies(proficiencies);
		return mechanics;
	}

	/**
	 * Механика черты: повышение одной из характеристик черты ({@code traits.abilities}) на 1,
	 * не выше 20. Как и в выгрузке VTTG ({@code VttgFeatMapper}), только когда описание его
	 * называет: на сайте характеристики черты — ещё и фильтр «что затрагивает», и у черты с
	 * требованием «Сила 13» в списке тоже стоит Сила. Навыки черты ({@code traits.skills}) —
	 * только фильтр, владения они не означают и сюда не идут.
	 *
	 * @return механика; {@code null} — повышения нет.
	 */
	static FeatMechanics featMechanics(List<AbilityType> abilities, List<String> description) {
		List<AbilityType> pool = distinct(abilities).stream()
			.filter(BASE_ABILITIES::contains)
			.collect(Collectors.toList());
		if (pool.isEmpty() || !mentionsIncrease(description)) {
			return null;
		}
		AbilityBonus bonus = bonus(pool, 1, 1);
		bonus.setUpto(FEAT_ABILITY_LIMIT);
		FeatMechanics mechanics = new FeatMechanics();
		mechanics.setAbilityBonuses(List.of(bonus));
		return mechanics;
	}

	/** Та же проверка, что у {@code VttgFeatMapper.mentionsIncrease}. */
	static boolean mentionsIncrease(List<String> description) {
		if (description == null) {
			return false;
		}
		String text = String.join(" ", description).toLowerCase(Locale.ROOT);
		return text.contains("увеличивается на 1") || text.contains("увеличьте");
	}

	private static ProficiencyGrant skillProficiencies(List<SkillType> skills) {
		Set<SkillType> set = new LinkedHashSet<>(distinct(skills));
		if (set.isEmpty()) {
			return null;
		}
		ProficiencyGrant proficiencies = new ProficiencyGrant();
		proficiencies.setSkills(set);
		return proficiencies;
	}

	private static <T> List<T> distinct(Collection<T> values) {
		return values == null ? List.of() : values.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
	}

	private static boolean positive(Integer value) {
		return value != null && value > 0;
	}
}
