package club.dnd5.portal.service.vttg;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Запись боевого блока существа ({@code CreatureAction}): черта, действие, реакция,
 * легендарное действие или действие логова.
 *
 * <p>Механики у действий старого сайта нет — только текст статблока («Рукопашная атака
 * оружием: +4 к попаданию, досягаемость 5 фт., одна цель. Попадание: 5 (1к6 + 2) колющего
 * урона»). Из него и разбирается то, чем VTTG кидает бросок: бонус атаки, досягаемость,
 * дистанция, урон по типам, спасбросок со сложностью, область и перезарядка. Что не
 * разобралось, остаётся в описании, и бросок мастер сделает сам.</p>
 */
final class VttgCreatureActions {
	private static final Pattern ATTACK = Pattern.compile("([+\\-−]\\s?\\d+)\\s*к попаданию");
	private static final Pattern REACH = Pattern.compile("досягаемост\\S*\\s*(\\d+)\\s*фт");
	private static final Pattern RANGE = Pattern.compile("дистанци\\S*\\s*(\\d+)(?:\\s*/\\s*(\\d+))?\\s*фт");
	private static final Pattern DAMAGE = Pattern.compile(
		"\\d+\\s*\\(\\s*(\\d*к\\d+(?:\\s*[+\\-−]\\s*\\d+)?)\\s*\\)([^()]{0,40})");
	private static final Pattern SAVE = Pattern.compile(
		"спасброс\\S*\\s+(силы|ловкости|телосложения|интеллекта|мудрости|харизмы)");
	private static final Pattern DC = Pattern.compile("(?<![а-яё])сл\\.?\\s*(\\d+)");
	private static final Pattern AREA = Pattern.compile("(\\d+)-футов\\S*\\s+(конус|куб|лини|сфер|цилиндр)");
	private static final Pattern RADIUS = Pattern.compile("радиус\\S*\\s+(\\d+)\\s*фут");
	private static final Pattern WIDTH = Pattern.compile("ширин\\S*\\s+(\\d+)\\s*фут");
	private static final Pattern RECHARGE = Pattern.compile(
		"\\s*\\((перезарядка|перезаряжается)([^)]*)\\)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private VttgCreatureActions() {
	}

	/**
	 * @param name        название из статблока, с перезарядкой в скобках
	 * @param englishName английское название
	 * @param html        описание в html сайта (разметка бестиария уже развёрнута)
	 */
	static Map<String, Object> entry(String name, String englishName, String html) {
		String description = VttgText.html(html);
		String text = Jsoup.parse(description).text().toLowerCase(Locale.ROOT).replace('ё', 'е');

		Map<String, Object> entry = new LinkedHashMap<>();
		String title = VttgText.orEmpty(name);
		Matcher recharge = RECHARGE.matcher(title);
		String rechargeKey = null;
		if (recharge.find()) {
			rechargeKey = recharge(recharge.group(2).toLowerCase(Locale.ROOT));
			if (rechargeKey != null) {
				title = (title.substring(0, recharge.start()) + title.substring(recharge.end())).trim();
			}
		}
		entry.put("name", title);
		String nameEn = VttgText.optional(englishName);
		if (nameEn != null) {
			entry.put("nameEn", nameEn);
		}
		entry.put("description", paragraphs(description));
		if (rechargeKey != null) {
			entry.put("recharge", rechargeKey);
		}

		Matcher attack = ATTACK.matcher(text);
		if (attack.find()) {
			entry.put("attackBonus", Integer.parseInt(attack.group(1).replace('−', '-').replace(" ", "")));
			putDamage(entry, hitSection(text));
			putDistance(entry, text);
			return entry;
		}

		Matcher save = SAVE.matcher(text);
		Matcher dc = DC.matcher(text);
		if (save.find()) {
			entry.put("saveType", VttgDictionaries.abilityByName(save.group(1)));
			if (dc.find()) {
				entry.put("saveDC", Integer.parseInt(dc.group(1)));
			}
			if (text.contains("половин")) {
				entry.put("saveEffect", "half");
			}
			putDamage(entry, text);
		}
		Map<String, Object> area = area(text);
		if (area != null) {
			entry.put("areaOfEffect", area);
		}
		return entry;
	}

	/** Текст после «Попадание:» до спасброска: урон спасброска — не урон попадания. */
	private static String hitSection(String text) {
		int hit = text.indexOf("попадание:");
		String section = hit < 0 ? text : text.substring(hit);
		int save = section.indexOf("спасброс");
		return save < 0 ? section : section.substring(0, save);
	}

	private static void putDamage(Map<String, Object> entry, String text) {
		List<Map<String, Object>> parts = new ArrayList<>();
		Matcher damage = DAMAGE.matcher(text);
		while (damage.find()) {
			String window = damage.group(2);
			if (!window.contains("урон")) {
				continue;
			}
			Map<String, Object> part = new LinkedHashMap<>();
			part.put("formula", damage.group(1).replace('−', '-').replace(" ", ""));
			String type = VttgDictionaries.damageTypeByWord(window);
			if (type != null) {
				part.put("type", type);
			}
			parts.add(part);
		}
		if (!parts.isEmpty()) {
			entry.put("damageParts", parts);
		}
	}

	private static void putDistance(Map<String, Object> entry, String text) {
		boolean melee = text.contains("рукопашная атака") || text.contains("рукопашная или")
			|| text.contains("или рукопашная");
		boolean ranged = text.contains("дальнобойная атака") || text.contains("дальнобойная или")
			|| text.contains("или дальнобойная");
		if (melee && ranged) {
			entry.put("rangeType", "meleeOrRanged");
		} else if (ranged) {
			entry.put("rangeType", "ranged");
		} else if (melee) {
			entry.put("rangeType", "melee");
		}
		Matcher reach = REACH.matcher(text);
		if (reach.find()) {
			entry.put("reach", Integer.parseInt(reach.group(1)));
		}
		Matcher range = RANGE.matcher(text);
		if (range.find()) {
			Map<String, Object> value = new LinkedHashMap<>();
			value.put("normal", Integer.parseInt(range.group(1)));
			if (range.group(2) != null) {
				value.put("long", Integer.parseInt(range.group(2)));
			}
			entry.put("range", value);
		}
		if (entry.containsKey("rangeType") || entry.containsKey("reach") || entry.containsKey("range")) {
			entry.put("distanceUnit", "ft");
		}
	}

	private static Map<String, Object> area(String text) {
		String shape = null;
		int size = 0;
		Matcher area = AREA.matcher(text);
		if (area.find()) {
			size = Integer.parseInt(area.group(1));
			switch (area.group(2)) {
				case "конус":
					shape = "cone";
					break;
				case "куб":
					shape = "rect";
					break;
				case "лини":
					shape = "ray";
					break;
				default:
					shape = "circle";
					break;
			}
		} else {
			Matcher radius = RADIUS.matcher(text);
			if (radius.find()) {
				size = Integer.parseInt(radius.group(1));
				shape = "circle";
			}
		}
		if (shape == null || size <= 0) {
			return null;
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("shape", shape);
		result.put("size", size);
		if ("ray".equals(shape)) {
			Matcher width = WIDTH.matcher(text);
			result.put("width", width.find() ? Integer.parseInt(width.group(1)) : 5);
		}
		result.put("unit", "ft");
		return result;
	}

	/** Перезарядка в ключах VTTG: «5–6» — {@code d5}, после отдыха — {@code slr}/{@code lr}. */
	private static String recharge(String value) {
		if (value.contains("коротк")) {
			return "slr";
		}
		if (value.contains("продолжительн") || value.contains("длинн")) {
			return "lr";
		}
		for (String die : new String[] {"3", "4", "5", "6"}) {
			if (value.contains(die)) {
				return "d" + die;
			}
		}
		return null;
	}

	/** Абзацы описания: VTTG показывает описание записи списком абзацев. */
	static List<String> paragraphs(String html) {
		List<String> result = new ArrayList<>();
		if (html.isEmpty()) {
			return result;
		}
		Document document = Jsoup.parseBodyFragment(html);
		document.outputSettings().prettyPrint(false);
		boolean onlyParagraphs = !document.body().children().isEmpty()
			&& document.body().children().stream().allMatch(child -> "p".equals(child.tagName()))
			&& document.body().textNodes().stream().allMatch(node -> node.isBlank());
		if (!onlyParagraphs) {
			result.add(html);
			return result;
		}
		for (Element paragraph : document.body().children()) {
			String inner = paragraph.html().trim();
			if (!inner.isEmpty()) {
				result.add(inner);
			}
		}
		return result;
	}
}
