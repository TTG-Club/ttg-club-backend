package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.ActionType;
import club.dnd5.portal.model.creature.Condition;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.CreatureFeat;
import club.dnd5.portal.model.creature.CreatureSpell;
import club.dnd5.portal.model.creature.HabitatType;
import club.dnd5.portal.model.creature.Lair;
import club.dnd5.portal.model.creature.SavingThrow;
import club.dnd5.portal.model.creature.Skill;
import club.dnd5.portal.model.creature.Spellcater;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.util.MarkdownUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Существо в формате VTTG ({@code creature}): статблок в {@code system}, токен и
 * заклинания существа.
 *
 * <p>Боевые записи разбираются из текста статблока ({@link VttgCreatureActions}). Защиты,
 * которые типом урона не выражаются («дробящий, колющий и рубящий урон от немагических
 * атак»), уезжают строкой под значками. Заклинания существа приезжают одним блоком:
 * заговоры и заклинания «по желанию», раз в день — группами по числу, а заклинания из
 * ячеек — общим запасом своего круга.</p>
 */
@Component
@RequiredArgsConstructor
public class VttgCreatureMapper {
	static final String SECTION = "creatures";
	private static final String SITE_SECTION = "bestiary";
	private static final Pattern NUMBER = Pattern.compile("(\\d+)");
	private static final int DEFAULT_LEGENDARY_COUNT = 3;

	private final VttgSpellMapper spellMapper;

	/**
	 * @param creature существо сайта
	 * @param imageUrl картинка токена; {@code null} — токена нет
	 */
	public Map<String, Object> toVttg(Creature creature, String imageUrl) {
		String key = creature.getUrlName();
		SpellBlock spellcasting = spellcasting(creature);

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("id", key);
		data.put("entityType", "creature");
		data.put("type", "creature");
		data.put("section", SECTION);
		data.put("srcSection", SITE_SECTION);
		data.put("srcUrl", key);
		data.put("autoSaves", true);
		data.put("name", VttgText.orEmpty(creature.getName()));
		data.put("nameEn", VttgText.orEmpty(creature.getEnglishName()));
		String description = VttgText.html(creature.getDescription());
		if (!description.isEmpty()) {
			data.put("description", description);
		}
		data.put("header", header(creature));
		data.put("token", token(creature, imageUrl));
		data.put("system", system(creature, spellcasting));
		data.put("sourceKey", VttgDictionaries.sourceKey(creature.getBook()));
		data.put("isSRD", false);
		data.put("isReadOnly", true);
		if (spellcasting != null && !spellcasting.spells.isEmpty()) {
			data.put("spells", spellcasting.spells);
		}
		return data;
	}

	private String header(Creature creature) {
		CreatureType type = creature.getType() == null ? CreatureType.HUMANOID : creature.getType();
		String size = creature.getSize() == null ? "" : creature.getSize().getSizeName(type);
		String kind = type.getCyrillicName();
		if (!VttgText.isBlank(creature.getRaceName())) {
			kind = kind + " (" + creature.getRaceName().trim() + ")";
		}
		// Мировоззрение склоняется по типу существа, а тип в базе бывает пустым
		Alignment raw = creature.getAligmentRaw();
		String alignment = raw == null ? "" : raw.getName(type);
		if (raw != null && !VttgText.isBlank(creature.getAlignmentSuffix())) {
			alignment = creature.getAlignmentSuffix().trim() + " " + alignment;
		}
		return (size + " " + kind).trim() + (alignment.isEmpty() ? "" : ", " + alignment);
	}

	private Map<String, Object> token(Creature creature, String imageUrl) {
		Map<String, Object> vision = new LinkedHashMap<>();
		vision.put("range", Arrays.asList(creature.getDarkvision(), creature.getBlindsight(), creature.getTrysight(),
				creature.getVibration()).stream()
			.filter(Objects::nonNull)
			.mapToInt(Integer::intValue)
			.max()
			.orElse(0));
		vision.put("darkvision", creature.getDarkvision() == null ? 0 : creature.getDarkvision());
		vision.put("angle", 360);
		vision.put("enabled", true);

		Map<String, Object> token = new LinkedHashMap<>();
		token.put("frameUrl", "assets/token-frames/0.png");
		if (imageUrl != null) {
			token.put("imageUrl", imageUrl.startsWith("/") ? VttgText.SITE_URL + imageUrl : imageUrl);
		}
		token.put("showName", true);
		token.put("disposition", "hostile");
		token.put("scale", tokenScale(creature.getSize()));
		token.put("vision", vision);
		return token;
	}

	private static Number tokenScale(CreatureSize size) {
		if (size == null) {
			return 1;
		}
		switch (size) {
			case TINY:
				return 0.5;
			case LARGE:
				return 2;
			case HUGE:
				return 3;
			case GARGANTUAN:
				return 4;
			default:
				return 1;
		}
	}

	private Map<String, Object> system(Creature creature, SpellBlock spellcasting) {
		int proficiency = proficiencyBonus(creature.getProficiencyBonus());

		Map<String, Object> system = new LinkedHashMap<>();
		system.put("size", size(creature.getSize()));
		system.put("type", VttgDictionaries.creatureType(creature.getType()));
		system.put("subtype", VttgText.orEmpty(creature.getRaceName()));
		system.put("alignment", alignment(creature.getAligmentRaw()));
		system.put("armorClass", armorClass(creature));
		system.put("hitPoints", hitPoints(creature));
		system.put("movement", movement(creature));
		system.put("abilities", abilities(creature));
		system.put("challengeRating", VttgText.isBlank(creature.getChallengeRating())
			? "0" : creature.getChallengeRating().trim());
		system.put("proficiencyBonus", proficiency);
		system.put("initiativeBonus", modifier(creature.getDexterity()));
		system.put("savingThrows", savingThrows(creature.getSavingThrows()));
		system.put("skills", skills(creature, proficiency));
		system.put("defenses", defenses(creature));
		system.put("senses", senses(creature));
		system.put("languages", languages(creature));
		system.put("environments", environments(creature.getHabitates()));
		String custom = customEnvironments(creature.getHabitates());
		if (!custom.isEmpty()) {
			system.put("customEnvironments", custom);
		}
		system.put("traits", traits(creature));
		List<Map<String, Object>> actions = actions(creature, ActionType.ACTION);
		actions.addAll(actions(creature, ActionType.MYSTICAL));
		system.put("actions", actions);
		system.put("bonusActions", actions(creature, ActionType.BONUS));
		system.put("reactions", actions(creature, ActionType.REACTION));
		system.put("legendary", legendary(creature));
		Map<String, Object> lair = lair(creature);
		if (lair != null) {
			system.put("lair", lair);
		}
		if (spellcasting != null) {
			if (!spellcasting.summary.isEmpty()) {
				system.put("spellcasting", spellcasting.summary);
			}
			system.put("spellcastingBlocks", Collections.singletonList(spellcasting.block));
		}
		return system;
	}

	// ── Числа статблока ──────────────────────────────────────────

	private static String size(CreatureSize size) {
		if (size == null || size == CreatureSize.SMALL_MEDIUM) {
			return "medium";
		}
		return size.name().toLowerCase(Locale.ROOT);
	}

	private static String alignment(Alignment alignment) {
		if (alignment == null || alignment == Alignment.WITHOUT) {
			return "unaligned";
		}
		switch (alignment) {
			case NEUTRAL:
			case TRUE_NEUTRAL:
				return "true-neutral";
			case ALL:
			case ALL_EVIL:
			case NO_GOOD:
				return "any";
			default:
				return alignment.name().toLowerCase(Locale.ROOT).replace('_', '-');
		}
	}

	private Map<String, Object> armorClass(Creature creature) {
		List<String> formula = new ArrayList<>();
		if (creature.getArmorTypes() != null) {
			creature.getArmorTypes().stream()
				.filter(Objects::nonNull)
				.map(armor -> armor.getCyrillicName())
				.forEach(formula::add);
		}
		if (!VttgText.isBlank(creature.getBonusAC())) {
			formula.add(creature.getBonusAC().trim());
		}
		Map<String, Object> armorClass = new LinkedHashMap<>();
		armorClass.put("value", (int) creature.getAC());
		armorClass.put("calculation", "flat");
		armorClass.put("formula", String.join(", ", formula));
		armorClass.put("flat", (int) creature.getAC());
		return armorClass;
	}

	private Map<String, Object> hitPoints(Creature creature) {
		int average = creature.getAverageHp();
		Integer count = creature.getCountDiceHp() == null ? null : creature.getCountDiceHp().intValue();
		Integer die = creature.getDiceHp() == null ? null : creature.getDiceHp().getMaxValue();
		int bonus = creature.getBonusHP() == null ? 0 : creature.getBonusHP();

		Map<String, Object> hitPoints = new LinkedHashMap<>();
		hitPoints.put("average", average);
		hitPoints.put("formula", count == null || die == null ? "" : count + "к" + die + signed(bonus));
		hitPoints.put("text", VttgText.orEmpty(creature.getSuffixHP()));
		hitPoints.put("current", average);
		hitPoints.put("max", average);
		if (die != null && Arrays.asList(4, 6, 8, 10, 12, 20).contains(die)) {
			hitPoints.put("hitDie", die);
		}
		if (count != null) {
			hitPoints.put("hitDiceCount", count);
		}
		hitPoints.put("bonus", bonus);
		return hitPoints;
	}

	private Map<String, Object> movement(Creature creature) {
		Map<String, Object> movement = new LinkedHashMap<>();
		movement.put("walk", (int) creature.getSpeed());
		movement.put("swim", value(creature.getSwimmingSpped()));
		movement.put("fly", value(creature.getFlySpeed()));
		movement.put("climb", value(creature.getClimbingSpeed()));
		movement.put("burrow", value(creature.getDiggingSpeed()));
		movement.put("hover", creature.getHover() != null);
		movement.put("units", "ft");
		return movement;
	}

	private static Map<String, Object> abilities(Creature creature) {
		Map<String, Object> abilities = new LinkedHashMap<>();
		abilities.put("strength", (int) creature.getStrength());
		abilities.put("dexterity", (int) creature.getDexterity());
		abilities.put("constitution", (int) creature.getConstitution());
		abilities.put("intelligence", (int) creature.getIntellect());
		abilities.put("wisdom", (int) creature.getWizdom());
		abilities.put("charisma", (int) creature.getCharisma());
		return abilities;
	}

	private static List<String> savingThrows(List<SavingThrow> savingThrows) {
		if (savingThrows == null) {
			return Collections.emptyList();
		}
		return savingThrows.stream()
			.filter(Objects::nonNull)
			.map(save -> VttgDictionaries.ability(save.getAbility()))
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
	}

	/**
	 * Навыки: владение или компетентность — по бонусу статблока. Бонус не ниже
	 * модификатора с удвоенным мастерством — компетентность.
	 */
	private static Map<String, Object> skills(Creature creature, int proficiency) {
		Map<String, Object> skills = new LinkedHashMap<>();
		if (creature.getSkills() == null) {
			return skills;
		}
		for (Skill skill : creature.getSkills()) {
			if (skill == null || skill.getType() == null) {
				continue;
			}
			int modifier = modifier(score(creature, skill.getType().getAbility()));
			boolean expertise = proficiency > 0 && skill.getBonus() >= modifier + 2 * proficiency;
			skills.put(VttgDictionaries.skill(skill.getType()), expertise ? "expertise" : "proficient");
		}
		return skills;
	}

	private static int score(Creature creature, AbilityType ability) {
		switch (ability) {
			case STRENGTH:
				return creature.getStrength();
			case DEXTERITY:
				return creature.getDexterity();
			case CONSTITUTION:
				return creature.getConstitution();
			case INTELLIGENCE:
				return creature.getIntellect();
			case WISDOM:
				return creature.getWizdom();
			default:
				return creature.getCharisma();
		}
	}

	private Map<String, Object> defenses(Creature creature) {
		Map<String, Object> defenses = new LinkedHashMap<>();
		putDefense(defenses, "vulnerabilities", creature.getVulnerabilityDamages());
		putDefense(defenses, "resistances", creature.getResistanceDamages());
		putDefense(defenses, "immunities", creature.getImmunityDamages());
		defenses.put("conditionImmunities", creature.getImmunityStates() == null ? Collections.emptyList()
			: creature.getImmunityStates().stream()
				.filter(Objects::nonNull)
				.filter(condition -> condition != Condition.DYING && condition != Condition.EVASIONS)
				.map(condition -> condition.name().toLowerCase(Locale.ROOT))
				.distinct()
				.collect(Collectors.toList()));
		return defenses;
	}

	/** Защиты типом урона — значками, составные («от немагических атак») — строкой. */
	private static void putDefense(Map<String, Object> defenses, String field, List<DamageType> types) {
		List<String> keys = new ArrayList<>();
		List<String> text = new ArrayList<>();
		if (types != null) {
			for (DamageType type : types) {
				if (type == null) {
					continue;
				}
				String key = VttgDictionaries.damageType(type);
				if (key != null) {
					if (!keys.contains(key)) {
						keys.add(key);
					}
				} else if (type != DamageType.NO_DAMAGE) {
					text.add(type.getCyrillicName());
				}
			}
		}
		defenses.put(field, keys);
		if (!text.isEmpty()) {
			defenses.put(field + "Text", String.join("; ", text));
		}
	}

	private static String senses(Creature creature) {
		List<String> senses = new ArrayList<>();
		String own = creature.getSense();
		if (!VttgText.isBlank(own)) {
			senses.add(own);
		}
		String passive = "пассивная Внимательность " + creature.getPassivePerception();
		if (!VttgText.isBlank(creature.getPassivePerceptionBonus())) {
			passive += " " + creature.getPassivePerceptionBonus().trim();
		}
		senses.add(passive);
		return String.join(", ", senses);
	}

	/** Языки: названия из справочника и свободный текст статблока («понимает, но не говорит»). */
	private static List<String> languages(Creature creature) {
		Set<String> languages = new LinkedHashSet<>();
		if (creature.getLanguages() != null) {
			creature.getLanguages().stream()
				.filter(Objects::nonNull)
				.map(Language::getName)
				.filter(name -> !VttgText.isBlank(name))
				.map(String::trim)
				.forEach(languages::add);
		}
		String text = VttgText.optional(creature.getLanguage() == null ? null
			: creature.getLanguage().replaceAll("<[^>]+>", ""));
		if (text != null && !"—".equals(text) && !"-".equals(text)) {
			languages.add(text);
		}
		return new ArrayList<>(languages);
	}

	/** Места обитания в словаре VTTG; деревня — город, остальное без пары — строкой. */
	private static List<String> environments(List<HabitatType> habitats) {
		if (habitats == null) {
			return Collections.emptyList();
		}
		return habitats.stream()
			.filter(Objects::nonNull)
			.map(habitat -> habitat == HabitatType.VILLAGE ? "urban" : habitat.getXmlName())
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
	}

	private static String customEnvironments(List<HabitatType> habitats) {
		if (habitats == null) {
			return "";
		}
		return habitats.stream()
			.filter(Objects::nonNull)
			.filter(habitat -> habitat != HabitatType.VILLAGE && habitat.getXmlName() == null)
			.map(HabitatType::getName)
			.distinct()
			.collect(Collectors.joining("; "));
	}

	// ── Боевые блоки ─────────────────────────────────────────────

	private List<Map<String, Object>> traits(Creature creature) {
		List<Map<String, Object>> result = new ArrayList<>();
		if (creature.getFeats() == null) {
			return result;
		}
		for (CreatureFeat feat : creature.getFeats()) {
			if (feat != null && !VttgText.isBlank(feat.getName())) {
				result.add(VttgCreatureActions.entry(feat.getName(), feat.getEnglishName(),
					markup(feat.getDescription(), feat.isMarkdown(), creature)));
			}
		}
		return result;
	}

	private List<Map<String, Object>> actions(Creature creature, ActionType type) {
		List<Map<String, Object>> result = new ArrayList<>();
		if (creature.getActions() == null) {
			return result;
		}
		for (Action action : creature.getActions()) {
			if (action != null && action.getActionType() == type && !VttgText.isBlank(action.getName())) {
				result.add(VttgCreatureActions.entry(action.getName(), action.getEnglishName(),
					markup(action.getDescription(), action.isMarkdown(), creature)));
			}
		}
		return result;
	}

	private static String markup(String description, boolean markdown, Creature creature) {
		return markdown ? MarkdownUtil.toHtml(description, creature.getName()) : description;
	}

	/** Легендарные действия: число за раунд берётся из текста блока, иначе три. */
	private Map<String, Object> legendary(Creature creature) {
		List<Map<String, Object>> actions = actions(creature, ActionType.LEGENDARY);
		int count = 0;
		if (!actions.isEmpty()) {
			count = DEFAULT_LEGENDARY_COUNT;
			String text = creature.getLegendary() == null ? "" : creature.getLegendary();
			Matcher matcher = Pattern.compile("(\\d+)\\s+легендарн").matcher(text);
			if (matcher.find()) {
				count = Integer.parseInt(matcher.group(1));
			}
		}
		Map<String, Object> legendary = new LinkedHashMap<>();
		legendary.put("count", count);
		legendary.put("actions", actions);
		String description = VttgText.html(creature.getLegendary());
		if (!description.isEmpty()) {
			legendary.put("description", description);
		}
		return legendary;
	}

	/**
	 * Логово: описание с действиями логова текстом, отдельные действия логова — записями,
	 * региональные эффекты — концовкой.
	 */
	private Map<String, Object> lair(Creature creature) {
		Lair lair = creature.getLair();
		List<Map<String, Object>> effects = actions(creature, ActionType.LAIR);
		if (lair == null && effects.isEmpty()) {
			return null;
		}
		StringBuilder description = new StringBuilder();
		String ending = "";
		if (lair != null) {
			description.append(VttgText.html(lair.getDescription()));
			String action = VttgText.html(lair.getAction());
			if (!action.isEmpty()) {
				description.append("<h4>Действия логова</h4>").append(action);
			}
			String effect = VttgText.html(lair.getEffect());
			if (!effect.isEmpty()) {
				ending = "<h4>Региональные эффекты</h4>" + effect;
			}
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("name", "Логово");
		result.put("description", description.toString());
		result.put("effects", effects);
		result.put("ending", ending);
		return result;
	}

	// ── Заклинания ───────────────────────────────────────────────

	/** Блок заклинаний вместе со сводкой заклинателя и выгруженными заклинаниями. */
	private static final class SpellBlock {
		private final Map<String, Object> summary = new LinkedHashMap<>();
		private final Map<String, Object> block = new LinkedHashMap<>();
		private final List<Map<String, Object>> spells = new ArrayList<>();
	}

	/**
	 * Заклинания существа. Заклинатель с ячейками (уровень заклинателя больше нуля)
	 * получает группы по кругам с запасом ячеек круга; заговоры и заклинания без ячеек —
	 * «по желанию»; заклинания с числом раз — группой «N/день».
	 */
	private SpellBlock spellcasting(Creature creature) {
		List<CreatureSpell> refs = creature.getSpells() == null ? Collections.emptyList()
			: creature.getSpells().stream()
				.filter(Objects::nonNull)
				.filter(ref -> ref.getSpell() != null && !VttgText.isBlank(ref.getSpell().getEnglishName()))
				.collect(Collectors.toList());
		if (refs.isEmpty()) {
			return null;
		}
		Spellcater caster = creature.getSpellcasters() == null ? null : creature.getSpellcasters().stream()
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		boolean slots = caster != null && caster.getLevel() != null && caster.getLevel() > 0;
		String ability = caster == null ? null : VttgDictionaries.ability(caster.getSpellAbility());

		SpellBlock result = new SpellBlock();
		putIfPresent(result.summary, "ability", ability);
		if (caster != null) {
			putIfPresent(result.summary, "saveDC", caster.getDc() == null ? null : (int) caster.getDc());
			putIfPresent(result.summary, "attackBonus", caster.getAttack() == null ? null : (int) caster.getAttack());
		}

		Map<String, List<CreatureSpell>> groups = new TreeMap<>();
		for (CreatureSpell ref : refs) {
			groups.computeIfAbsent(groupKey(ref, slots), key -> new ArrayList<>()).add(ref);
		}
		List<Map<String, Object>> blockGroups = new ArrayList<>();
		Set<String> exported = new LinkedHashSet<>();
		for (Map.Entry<String, List<CreatureSpell>> entry : groups.entrySet()) {
			CreatureSpell sample = entry.getValue().get(0);
			Map<String, Object> group = new LinkedHashMap<>();
			group.put("id", "spellgroup-" + entry.getKey());
			Map<String, Object> uses = null;
			if (sample.getCount() > 0) {
				group.put("mode", "perDayEach");
				group.put("count", (int) sample.getCount());
				group.put("label", sample.getCount() + "/" + (VttgText.isBlank(sample.getUnit()) ? "день"
					: sample.getUnit().trim()));
				uses = uses(sample.getCount(), "longRest");
			} else if (slots && sample.getSpell().getLevel() > 0) {
				int circle = sample.getSpell().getLevel();
				int count = slot(caster, circle);
				group.put("mode", count > 0 ? "perDayPool" : "atWill");
				if (count > 0) {
					group.put("count", count);
				}
				group.put("label", circle + " круг" + (count > 0 ? " (" + count + " ячеек)" : ""));
			} else {
				group.put("mode", "atWill");
				group.put("label", slots ? "Заговоры" : "Неограниченно");
				uses = uses(0, "atWill");
			}
			List<Map<String, Object>> spells = new ArrayList<>();
			for (CreatureSpell ref : entry.getValue()) {
				Spell spell = ref.getSpell();
				Map<String, Object> spellRef = new LinkedHashMap<>();
				spellRef.put("spellId", spell.getUrlName());
				spells.add(spellRef);
				if (exported.add(spell.getUrlName())) {
					Map<String, Object> record = new LinkedHashMap<>(spellMapper.toVttg(spell));
					if (uses != null) {
						record.put("uses", uses);
					}
					if (ability != null) {
						record.put("spellcastingAbility", ability);
					}
					result.spells.add(record);
				}
			}
			group.put("spells", spells);
			blockGroups.add(group);
		}

		result.block.put("id", "spellblock-1");
		result.block.put("name", slots ? "Использование заклинаний" : "Врождённое колдовство");
		putIfPresent(result.block, "ability", ability);
		putIfPresent(result.block, "saveDC", result.summary.get("saveDC"));
		putIfPresent(result.block, "attackBonus", result.summary.get("attackBonus"));
		result.block.put("groups", blockGroups);
		return result;
	}

	/** Ключ группы с порядком показа: по желанию, круги по возрастанию, затем «N/день». */
	private static String groupKey(CreatureSpell ref, boolean slots) {
		if (ref.getCount() > 0) {
			return "per-day-" + String.format("%02d", 99 - ref.getCount()) + "-"
				+ VttgDictionaries.translit(VttgText.orEmpty(ref.getUnit()));
		}
		if (slots && ref.getSpell().getLevel() > 0) {
			return "circle-" + ref.getSpell().getLevel();
		}
		return "at-will";
	}

	private static Map<String, Object> uses(int count, String recovery) {
		Map<String, Object> uses = new LinkedHashMap<>();
		uses.put("max", count);
		uses.put("current", count);
		uses.put("recovery", recovery);
		return uses;
	}

	private static int slot(Spellcater caster, int circle) {
		Byte[] slots = {caster.getSlot1(), caster.getSlot2(), caster.getSlot3(), caster.getSlot4(),
			caster.getSlot5(), caster.getSlot6(), caster.getSlot7(), caster.getSlot8(), caster.getSlot9()};
		Byte value = circle < 1 || circle > slots.length ? null : slots[circle - 1];
		return value == null ? 0 : value;
	}

	// ── Мелочи ───────────────────────────────────────────────────

	private static int proficiencyBonus(String text) {
		Matcher matcher = NUMBER.matcher(text == null ? "" : text);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : 2;
	}

	private static int modifier(int score) {
		return Math.floorDiv(score - 10, 2);
	}

	private static int value(Short speed) {
		return speed == null ? 0 : speed;
	}

	private static String signed(int value) {
		if (value == 0) {
			return "";
		}
		return value > 0 ? " + " + value : " - " + Math.abs(value);
	}

	private static void putIfPresent(Map<String, Object> target, String key, Object value) {
		if (value != null) {
			target.put(key, value);
		}
	}
}
