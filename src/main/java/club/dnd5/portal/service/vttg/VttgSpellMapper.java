package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.splells.TimeCast;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Заклинание в формате VTTG ({@code spell}).
 *
 * <p>Дистанция и длительность на сайте — строки («На себя (15-футовый конус)»,
 * «Концентрация, вплоть до 1 минуты»), поэтому числа и единицы из них разбираются.
 * Что не разобралось, уезжает подписью {@code rangeSpecial} или единицей
 * {@code special}: карточка покажет текст, а не выдуманное число. Урон и его усиление
 * не разбираются — броски остаются в описании.</p>
 */
@Component
public class VttgSpellMapper {
	static final String SECTION = "spells";
	private static final Pattern NUMBER = Pattern.compile("(\\d+)");
	private static final Pattern RANGE = Pattern.compile("(\\d+)\\s*(фут|фт|мил)", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
	private static final Pattern DURATION = Pattern.compile(
		"(\\d+)\\s*(раунд|ход|минут|час|дн|ден|сут)", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
	private static final Pattern MATERIAL_COST = Pattern.compile(
		"(\\d[\\d\\s]*)\\s*зм", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

	public Map<String, Object> toVttg(Spell spell) {
		String key = spell.getUrlName();
		String distance = spell.getDistance() == null ? "" : spell.getDistance().trim();
		String range = distance.toLowerCase(Locale.ROOT);
		Map<String, Object> area = area(distance);

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("type", "spell");
		data.put("id", key);
		data.put("name", VttgText.orEmpty(spell.getName()));
		data.put("nameEn", VttgText.orEmpty(spell.getEnglishName()));
		data.put("level", spell.getLevel());
		data.put("school", spell.getSchool() == null ? "evocation" : spell.getSchool().name().toLowerCase(Locale.ROOT));
		putCastingTime(data, spell.getTimes());
		data.put("components", components(spell));
		putRange(data, distance);
		putDuration(data, spell.getDuration());
		data.put("concentration", Boolean.TRUE.equals(spell.getConcentration()));
		data.put("ritual", Boolean.TRUE.equals(spell.getRitual()));
		if (area != null) {
			data.put("areaOfEffect", area);
		}
		String saveType = saveType(spell.getSavingthrows());
		data.put("targetType", targetType(spell, range, area, saveType));
		data.put("deliveryType", deliveryType(spell.getDescription(), range));
		List<String> damageTypes = damageTypes(spell);
		if (!damageTypes.isEmpty()) {
			data.put("damageTypes", damageTypes);
		}
		data.put("saveType", saveType);
		data.put("description", VttgText.html(spell.getDescription()));
		if (!VttgText.isBlank(spell.getUpperLevel())) {
			data.put("higherLevelDescription", VttgText.html(spell.getUpperLevel()));
		}
		data.put("sourceKey", VttgDictionaries.sourceKey(spell.getBook()));
		data.put("isSRD", spell.getSrd() != null && spell.getSrd());
		data.put("classKeys", classKeys(spell.getHeroClass()));
		data.put("section", SECTION);
		data.put("srcSection", SECTION);
		data.put("srcUrl", key);
		return data;
	}

	private void putCastingTime(Map<String, Object> data, List<TimeCast> times) {
		TimeCast time = times == null ? null : times.stream().filter(Objects::nonNull).findFirst().orElse(null);
		data.put("castingTimeValue", time == null || time.getNumber() < 1 ? 1 : time.getNumber());
		String unit = "action";
		if (time != null && time.getUnit() != null) {
			switch (time.getUnit()) {
				case BONUS:
					unit = "bonus-action";
					break;
				case REACTION:
					unit = "reaction";
					break;
				case MINUTE:
					unit = "minute";
					break;
				case HOUR:
					unit = "hour";
					break;
				case ON_HIT:
					unit = "bonus-action-after-hit";
					break;
				default:
					break;
			}
		}
		data.put("castingTimeUnit", unit);
		if ("reaction".equals(unit) && !VttgText.isBlank(time.getCondition())) {
			data.put("reactionTrigger", time.getCondition().trim());
		}
	}

	private Map<String, Object> components(Spell spell) {
		Map<String, Object> components = new LinkedHashMap<>();
		components.put("verbal", spell.isVerbalComponent());
		components.put("somatic", spell.isSomaticComponent());
		String material = VttgText.optional(spell.getAdditionalMaterialComponent());
		components.put("material", material != null);
		if (material != null) {
			components.put("materialDescription", material);
			Matcher cost = MATERIAL_COST.matcher(material);
			if (cost.find()) {
				components.put("materialCost", Integer.parseInt(cost.group(1).replaceAll("\\s", "")));
			}
			if (Boolean.TRUE.equals(spell.getConsumable())) {
				components.put("materialConsumed", true);
			}
		}
		return components;
	}

	/** Дистанция: «60 футов», «1 миля», «На себя», «Касание»; прочее — подписью. */
	static void putRange(Map<String, Object> data, String distance) {
		String text = distance.toLowerCase(Locale.ROOT);
		if (text.startsWith("на себя")) {
			data.put("range", 0);
			data.put("rangeUnit", "self");
			data.put("rangeSpecial", distance);
			return;
		}
		Matcher matcher = RANGE.matcher(text);
		if (matcher.find() && !text.startsWith("касание")) {
			data.put("range", Integer.parseInt(matcher.group(1)));
			data.put("rangeUnit", matcher.group(2).startsWith("мил") ? "mi" : "ft");
			return;
		}
		data.put("range", 0);
		data.put("rangeUnit", "ft");
		if (!distance.isEmpty()) {
			data.put("rangeSpecial", distance);
		}
	}

	/** Длительность: «Мгновенная», «1 час», «Концентрация, вплоть до 1 минуты». */
	static void putDuration(Map<String, Object> data, String duration) {
		String text = duration == null ? "" : duration.toLowerCase(Locale.ROOT);
		if (text.startsWith("мгновенн")) {
			data.put("durationValue", 0);
			data.put("durationUnit", "instantaneous");
			return;
		}
		if (text.startsWith("пока не будет рассеяно") || text.startsWith("до рассеивания")) {
			data.put("durationValue", 0);
			data.put("durationUnit", "until-dispelled");
			return;
		}
		Matcher matcher = DURATION.matcher(text);
		if (matcher.find()) {
			data.put("durationValue", Integer.parseInt(matcher.group(1)));
			data.put("durationUnit", durationUnit(matcher.group(2)));
			return;
		}
		data.put("durationValue", 0);
		data.put("durationUnit", "special");
	}

	private static String durationUnit(String word) {
		if (word.startsWith("раунд") || word.startsWith("ход")) {
			return "round";
		}
		if (word.startsWith("минут")) {
			return "minute";
		}
		if (word.startsWith("час")) {
			return "hour";
		}
		return "day";
	}

	/** Область из скобок дистанции: «На себя (15-футовый конус)», «(радиус 10 футов)». */
	static Map<String, Object> area(String distance) {
		int open = distance.indexOf('(');
		if (open < 0) {
			return null;
		}
		String inside = distance.substring(open).toLowerCase(Locale.ROOT);
		String shape = null;
		if (inside.contains("конус")) {
			shape = "cone";
		} else if (inside.contains("куб")) {
			shape = "rect";
		} else if (inside.contains("лини")) {
			shape = "ray";
		} else if (inside.contains("сфер") || inside.contains("радиус") || inside.contains("цилиндр")) {
			shape = "circle";
		}
		Matcher size = NUMBER.matcher(inside);
		if (shape == null || !size.find()) {
			return null;
		}
		Map<String, Object> area = new LinkedHashMap<>();
		area.put("shape", shape);
		area.put("size", Integer.parseInt(size.group(1)));
		if ("ray".equals(shape)) {
			area.put("width", 5);
		}
		area.put("unit", "ft");
		return area;
	}

	private static String targetType(Spell spell, String range, Map<String, Object> area, String saveType) {
		if (area != null) {
			return "area";
		}
		if (range.startsWith("на себя")) {
			return "self";
		}
		boolean hasDamage = spell.getDamageType() != null && !spell.getDamageType().isEmpty();
		boolean hasHeal = spell.getHealType() != null && !spell.getHealType().isEmpty();
		return hasDamage || hasHeal || !"none".equals(saveType) ? "creature" : "none";
	}

	/** Способ: атака заклинанием из описания, иначе по дистанции. */
	static String deliveryType(String description, String range) {
		String text = description == null ? "" : description.toLowerCase(Locale.ROOT);
		if (text.contains("дальнобойную атаку заклинанием") || text.contains("дальнобойной атаки заклинанием")) {
			return "ranged";
		}
		if (text.contains("рукопашную атаку заклинанием") || text.contains("рукопашной атаки заклинанием")) {
			return "melee";
		}
		if (range.startsWith("на себя")) {
			return "self";
		}
		if (range.startsWith("касание")) {
			return "touch";
		}
		return "none";
	}

	private static String saveType(List<AbilityType> savingThrows) {
		if (savingThrows == null) {
			return "none";
		}
		return savingThrows.stream()
			.map(VttgDictionaries::ability)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse("none");
	}

	private static List<String> damageTypes(Spell spell) {
		if (spell.getDamageType() == null) {
			return Collections.emptyList();
		}
		return spell.getDamageType().stream()
			.map(VttgDictionaries::damageType)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
	}

	private static List<String> classKeys(List<HeroClass> classes) {
		if (classes == null) {
			return Collections.emptyList();
		}
		return classes.stream()
			.filter(Objects::nonNull)
			.map(heroClass -> VttgDictionaries.classKey(heroClass.getEnglishName()))
			.filter(Objects::nonNull)
			.distinct()
			.sorted()
			.collect(Collectors.toList());
	}
}
