package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Словари сайта в ключах VTTG: характеристики, навыки, размеры, типы существ и урона,
 * классы и источники.
 */
final class VttgDictionaries {
	/** Базовые классы VTTG: заклинание без них в фильтре по классу не найдётся. */
	private static final Set<String> CLASS_KEYS = new HashSet<>(Arrays.asList(
		"artificer", "barbarian", "bard", "cleric", "druid", "fighter",
		"monk", "paladin", "ranger", "rogue", "sorcerer", "warlock", "wizard"
	));

	/** Кириллица в латиницу для ключей, которые строятся из русских подписей. */
	private static final Map<Character, String> TRANSLIT = new HashMap<>();

	static {
		String[][] pairs = {
			{"а", "a"}, {"б", "b"}, {"в", "v"}, {"г", "g"}, {"д", "d"}, {"е", "e"}, {"ё", "e"},
			{"ж", "zh"}, {"з", "z"}, {"и", "i"}, {"й", "y"}, {"к", "k"}, {"л", "l"}, {"м", "m"},
			{"н", "n"}, {"о", "o"}, {"п", "p"}, {"р", "r"}, {"с", "s"}, {"т", "t"}, {"у", "u"},
			{"ф", "f"}, {"х", "h"}, {"ц", "c"}, {"ч", "ch"}, {"ш", "sh"}, {"щ", "sch"}, {"ъ", ""},
			{"ы", "y"}, {"ь", ""}, {"э", "e"}, {"ю", "yu"}, {"я", "ya"}
		};
		for (String[] pair : pairs) {
			TRANSLIT.put(pair[0].charAt(0), pair[1]);
		}
	}

	private VttgDictionaries() {
	}

	/** Характеристика; варианты выбора («к другой», «к одной») — {@code null}. */
	static String ability(AbilityType ability) {
		if (ability == null || !AbilityType.getBaseAbility().contains(ability)) {
			return null;
		}
		return ability.name().toLowerCase(Locale.ROOT);
	}

	/** Все шесть характеристик в порядке листа. */
	static List<String> allAbilities() {
		return Arrays.asList("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma");
	}

	/** Навык в camelCase: {@code SLEIGHT_OF_HAND} → {@code sleightOfHand}. */
	static String skill(SkillType skill) {
		String[] parts = skill.name().toLowerCase(Locale.ROOT).split("_");
		StringBuilder key = new StringBuilder(parts[0]);
		for (int i = 1; i < parts.length; i++) {
			key.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
		}
		return key.toString();
	}

	/** Размеры существа; «Средний или Маленький» — оба, в порядке источника. */
	static List<String> sizes(CreatureSize size) {
		if (size == null) {
			return Collections.singletonList("medium");
		}
		if (size == CreatureSize.SMALL_MEDIUM) {
			return Arrays.asList("medium", "small");
		}
		return Collections.singletonList(size.name().toLowerCase(Locale.ROOT));
	}

	/** Тип существа; типы, которых в VTTG нет, сводятся к ближайшему. */
	static String creatureType(CreatureType type) {
		if (type == null) {
			return "humanoid";
		}
		switch (type) {
			case DEVIL:
				return "fiend";
			case SLIME:
				return "ooze";
			case VERMIN:
				return "beast";
			case SMALL_BEAST:
			case SWARM:
				return "swarm";
			case OUTSIDER:
				return "humanoid";
			default:
				return type.name().toLowerCase(Locale.ROOT);
		}
	}

	/** Тип урона; составные защиты («физический немагический») — {@code null}. */
	static String damageType(DamageType type) {
		if (type == null) {
			return null;
		}
		switch (type) {
			case FAIR:
				return "fire";
			case SOUND:
			case THUNDER:
				return "thunder";
			case NECTOTIC:
			case NECROTIC:
				return "necrotic";
			case COLD:
			case LIGHTNING:
			case POISON:
			case ACID:
			case PSYCHIC:
			case BLUDGEONING:
			case PIERCING:
			case SLASHING:
			case RADIANT:
			case FORCE:
				return type.name().toLowerCase(Locale.ROOT);
			default:
				return null;
		}
	}

	/** Ключ класса по английскому названию; не базовый класс — {@code null}. */
	static String classKey(String englishName) {
		if (VttgText.isBlank(englishName)) {
			return null;
		}
		String key = englishName.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
		return CLASS_KEYS.contains(key) ? key : null;
	}

	/** Ключ источника: аббревиатура книги в нижнем регистре. */
	static String sourceKey(Book book) {
		if (book == null || VttgText.isBlank(book.getSource())) {
			return "hb";
		}
		return book.getSource().trim().toLowerCase(Locale.ROOT);
	}

	/** Слаг страницы сайта: так же, как его строит сам сайт. */
	static String slug(String englishName) {
		return VttgText.isBlank(englishName) ? null : StringUtil.getUrl(englishName.trim());
	}

	/**
	 * Ключ из русской подписи: транслит в нижнем регистре через дефис. Подпись у умений
	 * и колонок таблицы классов — единственное, что у них есть, кроме номера в базе.
	 */
	static String translit(String text) {
		if (VttgText.isBlank(text)) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		for (char letter : text.trim().toLowerCase(Locale.ROOT).toCharArray()) {
			String latin = TRANSLIT.get(letter);
			result.append(latin == null ? String.valueOf(letter) : latin);
		}
		return result.toString().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
	}

	/** Характеристика по названию в любом падеже: «Сила», «Силы», «Мудрости». */
	static String abilityByName(String text) {
		if (VttgText.isBlank(text)) {
			return null;
		}
		String word = text.trim().toLowerCase(Locale.ROOT);
		if (word.startsWith("сил")) {
			return "strength";
		}
		if (word.startsWith("ловк")) {
			return "dexterity";
		}
		if (word.startsWith("телослож")) {
			return "constitution";
		}
		if (word.startsWith("интеллект")) {
			return "intelligence";
		}
		if (word.startsWith("мудрост")) {
			return "wisdom";
		}
		if (word.startsWith("харизм")) {
			return "charisma";
		}
		return null;
	}

	/** Тип урона по слову описания: «колющего», «огнём», «некротической энергией». */
	static String damageTypeByWord(String text) {
		String word = text.toLowerCase(Locale.ROOT);
		String[][] stems = {
			{"колющ", "piercing"}, {"рубящ", "slashing"}, {"дробящ", "bludgeoning"}, {"огн", "fire"},
			{"холод", "cold"}, {"электричеств", "lightning"}, {"молни", "lightning"}, {"яд", "poison"},
			{"кислот", "acid"}, {"звук", "thunder"}, {"гром", "thunder"}, {"некротич", "necrotic"},
			{"психич", "psychic"}, {"излучени", "radiant"}, {"силов", "force"}
		};
		for (String[] stem : stems) {
			// Слово ищется с начала: «яд» внутри «рядом» ядом не считается
			if (Pattern.compile("(?<![а-яё])" + stem[0]).matcher(word).find()) {
				return stem[1];
			}
		}
		return null;
	}
}
