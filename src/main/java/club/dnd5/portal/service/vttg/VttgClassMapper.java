package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Rest;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.classes.FeatureLevelDefinition;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.SpellLevelDefinition;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpell;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpellLevelType;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Класс в формате VTTG ({@code ClassDefinition}) с архетипами внутри записи.
 *
 * <p>Владения у класса на сайте — строки («Лёгкие доспехи, средние доспехи, щиты»), поэтому
 * категории из них разбираются, а то, что категорией не выражается, уезжает припиской.
 * Таблица класса — колонки с числами по уровням и ячейки заклинаний; из колонок известных
 * заговоров и заклинаний считается, сколько новых выбирать на уровне, а колонки известных
 * ресурсов (ярость, ки, очки чародейства) становятся счётчиками.</p>
 *
 * <p>Варианты умений (воззвания, метамагия, боевые стили, манёвры) приезжают справочным
 * списком у своего умения: сколько их выбирать, на сайте не записано.</p>
 */
@Component
public class VttgClassMapper {
	static final String SECTION = "classes";
	private static final int MAX_LEVEL = 20;
	private static final int DEFAULT_SUBCLASS_LEVEL = 3;
	private static final String ASI_NAME = "увеличение характеристик";
	private static final String NO_VALUE = "—";
	private static final String UNLIMITED = "∞";

	/** Колонки-ресурсы по подписи и их восстановление. */
	private static final Map<String, String> RESOURCES = new HashMap<>();
	/** Умение, у которого лежит список вариантов, — по корню его названия. */
	private static final Map<Option.OptionType, String> OPTION_FEATURES = new HashMap<>();

	static {
		RESOURCES.put("ярость", "long");
		RESOURCES.put("ки", "short");
		RESOURCES.put("очки ки", "short");
		RESOURCES.put("очки ци", "short");
		RESOURCES.put("очки чародейства", "long");

		OPTION_FEATURES.put(Option.OptionType.FIGHTING_STYLE, "боевой стиль");
		OPTION_FEATURES.put(Option.OptionType.FIGHTING_STYLE_RANGER, "боевой стиль");
		OPTION_FEATURES.put(Option.OptionType.FIGHTING_STYLE_PALADIN, "боевой стиль");
		OPTION_FEATURES.put(Option.OptionType.FIGHTING_STYLE_BARD, "боевой стиль");
		OPTION_FEATURES.put(Option.OptionType.METAMAGIC, "метамаги");
		OPTION_FEATURES.put(Option.OptionType.ELDRITCH_INVOCATION, "воззвани");
		OPTION_FEATURES.put(Option.OptionType.BONE, "договор");
		OPTION_FEATURES.put(Option.OptionType.MANEUVER, "превосходств");
		OPTION_FEATURES.put(Option.OptionType.ARCANE_SHOT, "выстрел");
		OPTION_FEATURES.put(Option.OptionType.RUNE, "рун");
		OPTION_FEATURES.put(Option.OptionType.ELEMENTAL_DISCIPLINE, "стихи");
		OPTION_FEATURES.put(Option.OptionType.ARTIFICER_INFUSION, "инфузи");
	}

	/**
	 * @param heroClass класс сайта
	 * @param options   варианты умений по типу списка
	 */
	public Map<String, Object> toVttg(HeroClass heroClass, Map<Option.OptionType, List<Option>> options) {
		String key = heroClass.getUrlName();
		List<Map<String, Object>> features = classFeatures(heroClass);
		attachOptions(features, optionTypes(heroClass.getEnglishName(), null), options);
		List<Map<String, Object>> subclasses = subclasses(heroClass, options);
		Table table = table(heroClass.getFeatureLevelDefenitions(), slotDefinitions(heroClass));

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("type", "class");
		data.put("id", key);
		data.put("key", key);
		data.put("section", SECTION);
		data.put("srcSection", SECTION);
		data.put("srcUrl", key);
		data.put("isSRD", false);
		data.put("name", VttgText.orEmpty(heroClass.getName()));
		data.put("nameEn", VttgText.orEmpty(heroClass.getEnglishName()));
		data.put("description", VttgText.html(heroClass.getDescription()));
		data.put("sourceKey", VttgDictionaries.sourceKey(heroClass.getBook()));
		data.put("hitDie", (int) heroClass.getDiceHp());
		List<String> primary = abilities(heroClass.getPrimaryAbilities());
		if (!primary.isEmpty()) {
			data.put("primaryAbilities", primary);
		}
		Proficiency armor = armor(heroClass.getArmor());
		data.put("armorProficiencies", armor.categories);
		putIfPresent(data, "armorProficienciesCustom", armor.custom);
		Proficiency weapon = weapon(heroClass.getWeapon());
		data.put("weaponProficiencies", weapon.categories);
		putIfPresent(data, "weaponProficienciesCustom", weapon.custom);
		data.put("toolProficiencies", tools(heroClass.getTools()));
		data.put("savingThrowProficiencies", savingThrows(heroClass.getSavingThrows()));
		data.put("skillChoices", skillChoices(heroClass));
		String equipment = VttgText.html(heroClass.getEquipment());
		if (!equipment.isEmpty()) {
			Map<String, Object> option = new LinkedHashMap<>();
			option.put("key", "A");
			option.put("description", equipment);
			data.put("startingEquipment", Collections.singletonList(option));
		}
		data.put("spellcasting", classSpellcasting(heroClass));
		data.put("subclassLevel", subclassLevel(heroClass));
		data.put("subclassLabel", VttgText.isBlank(heroClass.getArchetypeName())
			? "Подкласс" : heroClass.getArchetypeName().trim());
		data.put("subclasses", subclasses);
		data.put("features", features);
		data.put("levelTable", levelTable(table, features));
		if (!table.columns.isEmpty()) {
			data.put("tableColumns", table.columns);
		}
		List<Map<String, Object>> counters = counters(heroClass.getFeatureLevelDefenitions());
		if (!counters.isEmpty()) {
			data.put("counters", counters);
		}
		return data;
	}

	// ── Умения ───────────────────────────────────────────────────

	/**
	 * Умения класса по уровням. Повтор умения на сайте — отдельная строка со ссылкой на
	 * первую ({@code child}): здесь это своё умение на своём уровне с текстом первой, если
	 * своего нет. Строки «умение архетипа» и необязательные умения уезжают справкой для
	 * мастера уровня — на лист сами по себе они ничего не кладут.
	 */
	private List<Map<String, Object>> classFeatures(HeroClass heroClass) {
		List<HeroClassTrait> traits = heroClass.getTraits();
		Map<String, HeroClassTrait> byId = new HashMap<>();
		for (HeroClassTrait trait : traits) {
			byId.put(String.valueOf(trait.getId()), trait);
		}
		Set<String> used = new HashSet<>();
		List<Map<String, Object>> features = new ArrayList<>();
		for (HeroClassTrait trait : traits) {
			if (VttgText.isBlank(trait.getName())) {
				continue;
			}
			String description = trait.getDescription();
			HeroClassTrait origin = trait.getChild() == null ? null : byId.get(trait.getChild().trim());
			if (VttgText.isBlank(description) && origin != null) {
				description = origin.getDescription();
			}
			boolean informational = trait.isArchitype() || trait.getOptional() == 1;
			features.add(feature(used, null, featureName(trait.getName(), trait.getSuffix()), description,
				trait.getLevel(), informational));
		}
		return features;
	}

	private Map<String, Object> feature(Set<String> used, String prefix, String name, String description,
										int level, boolean informational) {
		boolean asi = name.toLowerCase(Locale.ROOT).startsWith(ASI_NAME);
		String base = asi ? "asi-" + level : VttgDictionaries.translit(name);
		if (base.isEmpty()) {
			base = "feature";
		}
		if (prefix != null) {
			base = prefix + "-" + base;
		}
		String key = unique(used, base, level);

		Map<String, Object> feature = new LinkedHashMap<>();
		feature.put("key", key);
		feature.put("name", name);
		feature.put("description", VttgText.html(description));
		feature.put("level", Math.max(level, 1));
		if (prefix != null) {
			feature.put("subclassKey", prefix);
		}
		if (asi) {
			feature.put("abilityImprovement", true);
		}
		if (informational) {
			feature.put("isInformationalOnly", true);
		}
		return feature;
	}

	/** Ключ, уникальный внутри записи: повтор получает уровень, потом номер. */
	private static String unique(Set<String> used, String base, int level) {
		if (used.add(base)) {
			return base;
		}
		String candidate = base + "-" + level;
		for (int ordinal = 2; !used.add(candidate); ordinal++) {
			candidate = base + "-" + level + "-" + ordinal;
		}
		return candidate;
	}

	private static String featureName(String name, String suffix) {
		String base = name.trim();
		return VttgText.isBlank(suffix) ? base : base + " " + suffix.trim();
	}

	// ── Варианты умений ──────────────────────────────────────────

	/** Типы списков вариантов класса ({@code archetype == null}) или его архетипа. */
	private static List<Option.OptionType> optionTypes(String className, String archetypeName) {
		if (VttgText.isBlank(className)) {
			return Collections.emptyList();
		}
		String archetype = archetypeName == null ? null : archetypeName.trim().replace(' ', '_');
		return Arrays.stream(Option.OptionType.values())
			.filter(type -> className.trim().equalsIgnoreCase(type.getClassName()))
			.filter(type -> archetype == null
				? type.getArhetypeName() == null
				: type.getArhetypeName() != null && archetype.contains(type.getArhetypeName()))
			.collect(Collectors.toList());
	}

	/** Список вариантов ложится в первое умение, чьё название с ним совпадает. */
	private void attachOptions(List<Map<String, Object>> features, List<Option.OptionType> types,
							   Map<Option.OptionType, List<Option>> options) {
		for (Option.OptionType type : types) {
			String stem = OPTION_FEATURES.get(type);
			List<Option> list = options.getOrDefault(type, Collections.emptyList());
			if (stem == null || list.isEmpty()) {
				continue;
			}
			features.stream()
				.filter(feature -> !feature.containsKey("choices"))
				.filter(feature -> Objects.toString(feature.get("name")).toLowerCase(Locale.ROOT).contains(stem))
				.findFirst()
				.ifPresent(feature -> feature.put("choices", choices(list)));
		}
	}

	private List<Map<String, Object>> choices(List<Option> options) {
		Set<String> used = new HashSet<>();
		List<Map<String, Object>> result = new ArrayList<>();
		for (Option option : options) {
			String slug = VttgDictionaries.slug(option.getEnglishName());
			String base = slug == null ? VttgDictionaries.translit(option.getName()) : slug;
			Map<String, Object> choice = new LinkedHashMap<>();
			choice.put("key", unique(used, base.isEmpty() ? "option" : base, 0));
			choice.put("name", VttgText.orEmpty(option.getName()));
			putIfPresent(choice, "nameEn", VttgText.optional(option.getEnglishName()));
			choice.put("description", VttgText.html(option.getDescription()));
			String prerequisite = VttgText.optional(option.getPrerequisite());
			if (prerequisite != null && !"нет".equalsIgnoreCase(prerequisite)) {
				choice.put("prerequisite", prerequisite);
			}
			if (option.getLevel() != null && option.getLevel() > 1) {
				choice.put("requiredLevel", option.getLevel());
			}
			result.add(choice);
		}
		return result;
	}

	// ── Архетипы ─────────────────────────────────────────────────

	/**
	 * Архетипы в порядке названия. Ключ — слаг страницы архетипа; два выпуска одного
	 * архетипа (UA и книга) разводятся источником, как и на новом сайте.
	 */
	private List<Map<String, Object>> subclasses(HeroClass heroClass, Map<Option.OptionType, List<Option>> options) {
		if (heroClass.getArchetypes() == null) {
			return Collections.emptyList();
		}
		List<Archetype> archetypes = heroClass.getArchetypes().stream()
			.filter(Objects::nonNull)
			.filter(archetype -> !VttgText.isBlank(archetype.getEnglishName()))
			.sorted(Comparator.comparing(Archetype::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
			.collect(Collectors.toList());
		Map<String, Long> occurrences = archetypes.stream()
			.collect(Collectors.groupingBy(Archetype::getUrlName, Collectors.counting()));

		Set<String> used = new HashSet<>();
		List<Map<String, Object>> result = new ArrayList<>();
		for (Archetype archetype : archetypes) {
			String base = archetype.getUrlName();
			if (occurrences.get(base) > 1) {
				base = base + "-" + VttgDictionaries.sourceKey(archetype.getBook());
			}
			String key = unique(used, base, 0);
			result.add(subclass(heroClass, archetype, key, options));
		}
		return result;
	}

	private Map<String, Object> subclass(HeroClass heroClass, Archetype archetype, String key,
										 Map<Option.OptionType, List<Option>> options) {
		List<Map<String, Object>> features = subclassFeatures(archetype, key);
		attachOptions(features, optionTypes(heroClass.getEnglishName(), archetype.getEnglishName()), options);
		attachSpells(features, archetype);
		int unlockLevel = archetype.getLevel() > 0 ? archetype.getLevel() : subclassLevel(heroClass);

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("key", key);
		data.put("name", VttgText.orEmpty(archetype.getName()));
		data.put("nameEn", VttgText.orEmpty(archetype.getEnglishName()));
		data.put("description", VttgText.html(archetype.getDescription()));
		data.put("unlockLevel", unlockLevel);
		data.put("sourceKey", VttgDictionaries.sourceKey(archetype.getBook()));
		Map<String, Object> spellcasting = subclassSpellcasting(heroClass, archetype, unlockLevel);
		if (spellcasting != null) {
			data.put("spellcasting", spellcasting);
		}
		data.put("features", features);
		Table table = table(archetype.getFeatureLevelDefenitions(), archetype.getLevelDefenitions());
		if (!table.columns.isEmpty()) {
			data.put("levelTable", levelTable(table, features));
			data.put("tableColumns", table.columns);
		}
		List<Map<String, Object>> counters = counters(archetype.getFeatureLevelDefenitions());
		if (!counters.isEmpty()) {
			counters.forEach(counter -> counter.put("subclassKey", key));
			data.put("counters", counters);
		}
		return data;
	}

	private List<Map<String, Object>> subclassFeatures(Archetype archetype, String subclassKey) {
		if (archetype.getFeats() == null) {
			return new ArrayList<>();
		}
		Set<String> used = new HashSet<>();
		List<Map<String, Object>> features = new ArrayList<>();
		for (ArchetypeTrait trait : archetype.getFeats()) {
			if (trait == null || VttgText.isBlank(trait.getName())) {
				continue;
			}
			features.add(feature(used, subclassKey, featureName(trait.getName(), trait.getSuffix()),
				trait.getDescription(), trait.getLevel(), false));
		}
		return features;
	}

	/**
	 * Заклинания архетипа. По уровню класса (домены, клятвы) — выдача первому умению
	 * архетипа, всегда подготовленная; по кругу заклинания (покровители колдуна) —
	 * расширение списка, круг открывается вместе с ячейками колдуна. Заклинания с
	 * пометкой (земля круга друидов) остаются только в описании: выдавать их все разом
	 * нельзя.
	 */
	private void attachSpells(List<Map<String, Object>> features, Archetype archetype) {
		if (archetype.getSpells() == null || features.isEmpty()) {
			return;
		}
		List<ArchetypeSpell> spells = archetype.getSpells().stream()
			.filter(Objects::nonNull)
			.filter(spell -> spell.getSpell() != null && spell.getLevel() > 0)
			.filter(spell -> VttgText.isBlank(spell.getAdvenced()))
			.filter(spell -> !VttgText.isBlank(spell.getSpell().getEnglishName()))
			.sorted(Comparator.comparingInt(ArchetypeSpell::getLevel))
			.collect(Collectors.toList());
		if (spells.isEmpty()) {
			return;
		}
		Map<String, Object> first = features.stream()
			.min(Comparator.comparingInt(feature -> (Integer) feature.get("level")))
			.orElse(features.get(0));
		Map<String, Object> featData = new LinkedHashMap<>();
		featData.put("type", "feat");

		if (archetype.getSpellLevelType() == ArchetypeSpellLevelType.SPELL_LEVEL) {
			Map<Integer, List<Map<String, Object>>> byCircle = new TreeMap<>();
			for (ArchetypeSpell spell : spells) {
				Map<String, Object> ref = new LinkedHashMap<>();
				ref.put("name", VttgText.orEmpty(spell.getSpell().getName()));
				ref.put("spellId", spell.getSpell().getUrlName());
				byCircle.computeIfAbsent(spell.getLevel(), circle -> new ArrayList<>()).add(ref);
			}
			List<Map<String, Object>> groups = new ArrayList<>();
			byCircle.forEach((circle, refs) -> {
				Map<String, Object> group = new LinkedHashMap<>();
				group.put("requiredLevel", Math.max(1, circle * 2 - 1));
				group.put("spells", refs);
				groups.add(group);
			});
			Map<String, Object> spellList = new LinkedHashMap<>();
			spellList.put("groups", groups);
			featData.put("spellList", spellList);
			first.put("featData", featData);
			return;
		}

		int level = (Integer) first.get("level");
		List<String> now = new ArrayList<>();
		Map<String, List<String>> later = new LinkedHashMap<>();
		for (ArchetypeSpell spell : spells) {
			String id = spell.getSpell().getUrlName();
			if (spell.getLevel() <= level) {
				now.add(id);
			} else {
				later.computeIfAbsent(String.valueOf(spell.getLevel()), key -> new ArrayList<>()).add(id);
			}
		}
		if (!now.isEmpty()) {
			first.put("grantedSpells", now);
		}
		if (!later.isEmpty()) {
			first.put("grantedSpellsByLevel", later);
		}
		featData.put("grantedSpellsAlwaysPrepared", true);
		first.put("featData", featData);
	}

	// ── Заклинательство ──────────────────────────────────────────

	/**
	 * Заклинательство класса. Колдун узнаётся по ячейкам, которые возвращает короткий
	 * отдых; уровень начала — первый уровень с ячейками в таблице.
	 */
	private Map<String, Object> classSpellcasting(HeroClass heroClass) {
		String ability = VttgDictionaries.ability(heroClass.getSpellAbility());
		if (ability == null) {
			return null;
		}
		String type = heroClass.getSlotsReset() == Rest.SHORT ? "pact" : casterType(heroClass.getSpellcasterType());
		if (type == null) {
			return null;
		}
		return spellcasting(type, ability, firstSlotLevel(heroClass.getLevelDefenitions(), 1));
	}

	/** Свои ячейки архетипа (мистический рыцарь, мистический ловкач) — от Интеллекта. */
	private Map<String, Object> subclassSpellcasting(HeroClass heroClass, Archetype archetype, int unlockLevel) {
		String type = casterType(archetype.getSpellcasterType());
		if (type == null) {
			return null;
		}
		String ability = VttgDictionaries.ability(heroClass.getSpellAbility());
		return spellcasting(type, ability == null ? "intelligence" : ability,
			firstSlotLevel(archetype.getLevelDefenitions(), unlockLevel));
	}

	private static Map<String, Object> spellcasting(String type, String ability, int startLevel) {
		Map<String, Object> spellcasting = new LinkedHashMap<>();
		spellcasting.put("type", type);
		spellcasting.put("ability", ability);
		spellcasting.put("startLevel", startLevel);
		return spellcasting;
	}

	private static String casterType(SpellcasterType type) {
		if (type == null) {
			return null;
		}
		switch (type) {
			case FULL:
				return "full";
			case HALF:
				return "half";
			case PARTLY:
				return "third";
			default:
				return null;
		}
	}

	private static int firstSlotLevel(List<SpellLevelDefinition> definitions, int fallback) {
		if (definitions == null) {
			return fallback;
		}
		return definitions.stream()
			.filter(Objects::nonNull)
			.filter(definition -> definition.getLevel() != null)
			.filter(definition -> slots(definition).stream().anyMatch(slot -> slot > 0))
			.map(definition -> (int) definition.getLevel())
			.min(Integer::compareTo)
			.orElse(fallback);
	}

	/** Ячейки класса: у классов без своей сетки (полузаклинатель на треть) их нет. */
	private static List<SpellLevelDefinition> slotDefinitions(HeroClass heroClass) {
		SpellcasterType type = heroClass.getSpellcasterType();
		if ((type == null || type == SpellcasterType.NONE || type == SpellcasterType.PARTLY)
			&& heroClass.getSlotsReset() != Rest.SHORT) {
			return Collections.emptyList();
		}
		return heroClass.getLevelDefenitions();
	}

	private static List<Integer> slots(SpellLevelDefinition definition) {
		return Arrays.asList(definition.getSlot1(), definition.getSlot2(), definition.getSlot3(),
				definition.getSlot4(), definition.getSlot5(), definition.getSlot6(), definition.getSlot7(),
				definition.getSlot8(), definition.getSlot9()).stream()
			.map(slot -> slot == null ? 0 : (int) slot)
			.collect(Collectors.toList());
	}

	private int subclassLevel(HeroClass heroClass) {
		if (heroClass.getEnabledArhitypeLevel() > 0) {
			return heroClass.getEnabledArhitypeLevel();
		}
		if (heroClass.getArchetypes() == null) {
			return DEFAULT_SUBCLASS_LEVEL;
		}
		return heroClass.getArchetypes().stream()
			.filter(Objects::nonNull)
			.map(archetype -> (int) archetype.getLevel())
			.filter(level -> level > 0)
			.min(Integer::compareTo)
			.orElse(DEFAULT_SUBCLASS_LEVEL);
	}

	// ── Таблица ──────────────────────────────────────────────────

	/** Колонки таблицы и значения по уровням: ключ колонки → (уровень → значение). */
	private static final class Table {
		private final List<Map<String, Object>> columns = new ArrayList<>();
		private final Map<String, Map<Integer, String>> values = new LinkedHashMap<>();
		private final Map<Integer, Integer> newCantrips = new HashMap<>();
		private final Map<Integer, Integer> newSpells = new HashMap<>();
	}

	private Table table(List<FeatureLevelDefinition> definitions, List<SpellLevelDefinition> slots) {
		Table table = new Table();
		if (definitions != null) {
			Set<String> used = new HashSet<>();
			for (FeatureLevelDefinition definition : definitions) {
				if (definition == null || VttgText.isBlank(definition.getName())) {
					continue;
				}
				String key = unique(used, columnKey(definition.getName()), 0);
				Map<Integer, String> byLevel = new LinkedHashMap<>();
				for (int level = 1; level <= MAX_LEVEL; level++) {
					byLevel.put(level, cell(definition, level));
				}
				table.values.put(key, byLevel);
				Map<String, Object> column = new LinkedHashMap<>();
				column.put("key", key);
				column.put("label", definition.getName().trim());
				table.columns.add(column);
				if ("cantripsKnown".equals(key)) {
					gains(definition, table.newCantrips);
				} else if ("spellsKnown".equals(key)) {
					gains(definition, table.newSpells);
				}
			}
		}
		addSlots(table, slots);
		return table;
	}

	/** Ключ колонки: известные заговоры и заклинания — под ключами, которые читает лист. */
	private static String columnKey(String name) {
		String label = name.toLowerCase(Locale.ROOT);
		if (label.contains("заговор")) {
			return "cantripsKnown";
		}
		if (label.contains("известн") && label.contains("заклинан")) {
			return "spellsKnown";
		}
		String key = VttgDictionaries.translit(name);
		return key.isEmpty() ? "column" : key;
	}

	/** Ячейка как на сайте: 0 — прочерк, −1 — без ограничений. */
	private static String cell(FeatureLevelDefinition definition, int level) {
		Byte value = byLevel(definition, level);
		if (value == null || value == 0) {
			return NO_VALUE;
		}
		if (value == -1) {
			return UNLIMITED;
		}
		return Objects.toString(definition.getPrefix(), "") + value + Objects.toString(definition.getSufix(), "");
	}

	/** Значение колонки на уровне; пустая ячейка базы — {@code null}. */
	private static Byte byLevel(FeatureLevelDefinition definition, int level) {
		Byte[] values = {
			definition.getL1(), definition.getL2(), definition.getL3(), definition.getL4(), definition.getL5(),
			definition.getL6(), definition.getL7(), definition.getL8(), definition.getL9(), definition.getL10(),
			definition.getL11(), definition.getL12(), definition.getL13(), definition.getL14(), definition.getL15(),
			definition.getL16(), definition.getL17(), definition.getL18(), definition.getL19(), definition.getL20()
		};
		return level < 1 || level > values.length ? null : values[level - 1];
	}

	/** Прирост итоговой колонки по уровням: сколько новых выбирать на уровне. */
	private static void gains(FeatureLevelDefinition definition, Map<Integer, Integer> target) {
		int previous = 0;
		for (int level = 1; level <= MAX_LEVEL; level++) {
			Byte value = byLevel(definition, level);
			if (value == null || value <= 0) {
				continue;
			}
			if (value > previous) {
				target.merge(level, value - previous, Integer::sum);
			}
			previous = value;
		}
	}

	/** Ячейки заклинаний — одна колонка с подколонками по кругам до старшего занятого. */
	private static void addSlots(Table table, List<SpellLevelDefinition> definitions) {
		if (definitions == null || definitions.isEmpty()) {
			return;
		}
		Map<Integer, List<Integer>> byLevel = new HashMap<>();
		int maxCircle = 0;
		for (SpellLevelDefinition definition : definitions) {
			if (definition == null || definition.getLevel() == null) {
				continue;
			}
			List<Integer> slots = slots(definition);
			byLevel.put((int) definition.getLevel(), slots);
			for (int circle = slots.size(); circle > maxCircle; circle--) {
				if (slots.get(circle - 1) > 0) {
					maxCircle = circle;
				}
			}
		}
		if (maxCircle == 0) {
			return;
		}
		List<Map<String, Object>> children = new ArrayList<>();
		for (int circle = 1; circle <= maxCircle; circle++) {
			String key = "slot" + circle;
			Map<String, Object> child = new LinkedHashMap<>();
			child.put("key", key);
			child.put("label", String.valueOf(circle));
			children.add(child);
			Map<Integer, String> values = new LinkedHashMap<>();
			for (int level = 1; level <= MAX_LEVEL; level++) {
				List<Integer> slots = byLevel.get(level);
				int value = slots == null ? 0 : slots.get(circle - 1);
				values.put(level, value == 0 ? NO_VALUE : String.valueOf(value));
			}
			table.values.put(key, values);
		}
		Map<String, Object> column = new LinkedHashMap<>();
		column.put("label", "Ячейки заклинаний");
		column.put("children", children);
		table.columns.add(column);
	}

	private List<Map<String, Object>> levelTable(Table table, List<Map<String, Object>> features) {
		List<Map<String, Object>> rows = new ArrayList<>();
		for (int level = 1; level <= MAX_LEVEL; level++) {
			int current = level;
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("level", level);
			row.put("proficiencyBonus", 2 + (level - 1) / 4);
			row.put("featureKeys", features.stream()
				.filter(feature -> Objects.equals(feature.get("level"), current))
				.map(feature -> (String) feature.get("key"))
				.collect(Collectors.toList()));
			putPositive(row, "newCantrips", table.newCantrips.get(level));
			putPositive(row, "newSpells", table.newSpells.get(level));
			for (Map.Entry<String, Map<Integer, String>> column : table.values.entrySet()) {
				row.put(column.getKey(), column.getValue().get(level));
			}
			rows.add(row);
		}
		return rows;
	}

	/**
	 * Счётчики из колонок-ресурсов: прогрессия — числовые ячейки, «без ограничений» и
	 * прочерк в максимум не переводятся.
	 */
	private List<Map<String, Object>> counters(List<FeatureLevelDefinition> definitions) {
		List<Map<String, Object>> result = new ArrayList<>();
		if (definitions == null) {
			return result;
		}
		Set<String> used = new HashSet<>();
		for (FeatureLevelDefinition definition : definitions) {
			if (definition == null || VttgText.isBlank(definition.getName())) {
				continue;
			}
			String key = unique(used, columnKey(definition.getName()), 0);
			String recovery = RESOURCES.get(definition.getName().trim().toLowerCase(Locale.ROOT));
			if (recovery == null) {
				continue;
			}
			Map<String, Integer> progression = new LinkedHashMap<>();
			for (int level = 1; level <= MAX_LEVEL; level++) {
				Byte value = byLevel(definition, level);
				if (value != null && value > 0) {
					progression.put(String.valueOf(level), (int) value);
				}
			}
			if (progression.isEmpty()) {
				continue;
			}
			Map<String, Object> counter = new LinkedHashMap<>();
			counter.put("key", key);
			counter.put("name", definition.getName().trim());
			counter.put("startLevel", Integer.parseInt(progression.keySet().iterator().next()));
			counter.put("recovery", recovery);
			counter.put("progression", progression);
			result.add(counter);
		}
		return result;
	}

	// ── Владения ─────────────────────────────────────────────────

	/** Разобранные категории владения и то, что категорией не выражается. */
	private static final class Proficiency {
		private final List<String> categories = new ArrayList<>();
		private String custom;
	}

	private static Proficiency armor(String text) {
		Proficiency proficiency = new Proficiency();
		List<String> rest = new ArrayList<>();
		for (String item : items(text)) {
			String value = item.toLowerCase(Locale.ROOT).replace('ё', 'е');
			if (value.startsWith("все доспехи")) {
				add(proficiency.categories, "light", "medium", "heavy");
			} else if (value.startsWith("легк") && value.contains("доспех") && !value.contains("(")) {
				add(proficiency.categories, "light");
			} else if (value.startsWith("средн") && value.contains("доспех") && !value.contains("(")) {
				add(proficiency.categories, "medium");
			} else if (value.startsWith("тяжел") && value.contains("доспех") && !value.contains("(")) {
				add(proficiency.categories, "heavy");
			} else if (value.equals("щиты") || value.equals("щит")) {
				add(proficiency.categories, "shield");
			} else {
				rest.add(item);
			}
		}
		proficiency.custom = rest.isEmpty() ? null : String.join(", ", rest);
		return proficiency;
	}

	private static Proficiency weapon(String text) {
		Proficiency proficiency = new Proficiency();
		List<String> rest = new ArrayList<>();
		for (String item : items(text)) {
			String value = item.toLowerCase(Locale.ROOT);
			if (value.startsWith("простое") && value.contains("оружие") && !value.contains("(")) {
				add(proficiency.categories, "simple");
			} else if (value.startsWith("воинское") && value.contains("оружие") && !value.contains("(")) {
				add(proficiency.categories, "martial");
			} else {
				rest.add(item);
			}
		}
		proficiency.custom = rest.isEmpty() ? null : String.join(", ", rest);
		return proficiency;
	}

	private static List<String> tools(String text) {
		List<String> items = items(text);
		return items.isEmpty() ? Collections.emptyList() : Collections.singletonList(String.join(", ", items));
	}

	/** Перечисление сайта по запятым; «Нет» — пусто. */
	private static List<String> items(String text) {
		String plain = VttgText.optional(text == null ? null : text.replaceAll("<[^>]+>", ""));
		if (plain == null || "нет".equalsIgnoreCase(plain.replace(".", "").trim())) {
			return Collections.emptyList();
		}
		return Arrays.stream(plain.split(","))
			.map(String::trim)
			.map(item -> item.endsWith(".") ? item.substring(0, item.length() - 1) : item)
			.filter(item -> !item.isEmpty())
			.collect(Collectors.toList());
	}

	private static List<String> savingThrows(String text) {
		return items(text).stream()
			.map(VttgDictionaries::abilityByName)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
	}

	private static List<String> abilities(List<AbilityType> abilities) {
		if (abilities == null) {
			return Collections.emptyList();
		}
		return abilities.stream()
			.map(VttgDictionaries::ability)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
	}

	private static Map<String, Object> skillChoices(HeroClass heroClass) {
		Map<String, Object> choices = new LinkedHashMap<>();
		choices.put("count", (int) heroClass.getSkillAvailableCount());
		choices.put("from", heroClass.getAvailableSkills() == null ? Collections.emptyList()
			: heroClass.getAvailableSkills().stream()
				.filter(Objects::nonNull)
				.map(VttgDictionaries::skill)
				.distinct()
				.collect(Collectors.toList()));
		return choices;
	}

	private static void add(List<String> target, String... values) {
		for (String value : values) {
			if (!target.contains(value)) {
				target.add(value);
			}
		}
	}

	private static void putIfPresent(Map<String, Object> target, String key, Object value) {
		if (value != null) {
			target.put(key, value);
		}
	}

	private static void putPositive(Map<String, Object> target, String key, Integer value) {
		if (value != null && value > 0) {
			target.put(key, value);
		}
	}
}
