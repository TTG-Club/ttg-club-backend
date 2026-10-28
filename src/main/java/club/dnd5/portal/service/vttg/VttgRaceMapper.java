package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.AbilityBonus;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.races.Feature;
import club.dnd5.portal.model.races.Race;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Раса или подраса в формате вида VTTG ({@code SpeciesDefinition}).
 *
 * <p>Подраса — самостоятельная запись со ссылкой {@code parentKey} на расу: так VTTG
 * показывает её вариантом после выбора расы и складывает дары обеих записей.</p>
 *
 * <p>Повышение характеристик в редакции 2014 даёт раса, поэтому оно уезжает блоком
 * {@code featData.abilityScoreIncrease} записи. Выбор у такого блока один, а у части
 * рас групп выбора больше («+2 к одной и +1 к другой»): лишние группы уезжают
 * отдельными умениями «Увеличение характеристик» со своим блоком.</p>
 */
@Component
public class VttgRaceMapper {
	static final String SECTION = "species";
	private static final String SITE_SECTION = "races";
	private static final String ASI_FEATURE_NAME = "Увеличение характеристик";

	public Map<String, Object> toVttg(Race race) {
		Race parent = race.getParent();
		String key = key(race);
		List<AbilityGroup> groups = abilityGroups(race.getBonuses());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("type", "species");
		data.put("id", key);
		data.put("key", key);
		data.put("section", SECTION);
		if (parent == null) {
			data.put("srcSection", SITE_SECTION);
			data.put("srcUrl", race.getUrlName());
		} else {
			data.put("parentKey", key(parent));
		}
		data.put("isSRD", false);
		data.put("name", VttgText.orEmpty(race.getName()));
		data.put("nameEn", VttgText.orEmpty(race.getEnglishName()));
		data.put("description", VttgText.html(race.getDescription()));
		data.put("sourceKey", VttgDictionaries.sourceKey(race.getBook()));
		data.put("creatureType", VttgDictionaries.creatureType(
			race.getType() == null && parent != null ? parent.getType() : race.getType()));
		data.put("size", VttgDictionaries.sizes(
			race.getSize() == null && parent != null ? parent.getSize() : race.getSize()));
		data.put("speed", speed(race));

		Map<String, Object> featData = featData();
		if (!groups.isEmpty()) {
			featData.put("abilityScoreIncrease", groups.get(0).toVttg());
		}
		if (race.getDarkvision() != null && race.getDarkvision() > 0) {
			featData.put("darkvision", race.getDarkvision());
		}
		if (featData.size() > 1) {
			data.put("featData", featData);
		}

		List<Map<String, Object>> features = new ArrayList<>();
		for (int i = 1; i < groups.size(); i++) {
			features.add(abilityFeature(key, i, groups.get(i)));
		}
		if (race.getFeatures() != null) {
			for (Feature feature : race.getFeatures()) {
				features.add(feature(key, feature));
			}
		}
		data.put("features", features);
		return data;
	}

	/** Ключ записи: слаг страницы, у подрасы — со слагом расы, как в адресе сайта. */
	static String key(Race race) {
		if (race.getParent() == null) {
			return race.getUrlName();
		}
		return race.getParent().getUrlName() + "-" + race.getUrlName();
	}

	private Map<String, Object> speed(Race race) {
		Map<String, Object> speed = new LinkedHashMap<>();
		speed.put("walk", race.getSpeed());
		putPositive(speed, "fly", race.getFly());
		putPositive(speed, "climb", race.getClimb());
		putPositive(speed, "swim", race.getSwim());
		return speed;
	}

	private Map<String, Object> feature(String recordKey, Feature feature) {
		String slug = VttgDictionaries.slug(feature.getEnglishName());
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("key", recordKey + "-" + (slug == null ? String.valueOf(feature.getId()) : slug));
		data.put("name", VttgText.orEmpty(feature.getName()));
		data.put("description", VttgText.html(feature.getDescription()));

		Map<String, Object> featData = featData();
		if (feature.getSkills() != null && !feature.getSkills().isEmpty()) {
			featData.put("skillProficiencies", feature.getSkills().stream()
				.filter(Objects::nonNull)
				.map(VttgDictionaries::skill)
				.distinct()
				.collect(Collectors.toList()));
		}
		if (feature.getLanuages() != null && !feature.getLanuages().isEmpty()) {
			featData.put("languages", feature.getLanuages().stream()
				.filter(Objects::nonNull)
				.map(Language::getName)
				.filter(name -> !VttgText.isBlank(name))
				.distinct()
				.collect(Collectors.toList()));
		}
		if (featData.size() > 1) {
			data.put("featData", featData);
		}
		return data;
	}

	private Map<String, Object> abilityFeature(String recordKey, int index, AbilityGroup group) {
		Map<String, Object> featData = featData();
		featData.put("abilityScoreIncrease", group.toVttg());

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("key", recordKey + "-asi-" + index);
		data.put("name", ASI_FEATURE_NAME);
		data.put("description", group.describe());
		data.put("featData", featData);
		return data;
	}

	/**
	 * Группы повышения характеристик. Первая собирает фиксированные прибавки и первый
	 * выбор, следующие — остальные выборы: у записи и у каждого умения выбор один.
	 */
	static List<AbilityGroup> abilityGroups(List<AbilityBonus> bonuses) {
		if (bonuses == null || bonuses.isEmpty()) {
			return Collections.emptyList();
		}
		Map<String, Integer> fixed = new LinkedHashMap<>();
		for (AbilityBonus bonus : bonuses) {
			String ability = VttgDictionaries.ability(bonus.getAbility());
			if (ability != null && bonus.getBonus() != null && bonus.getBonus() != 0) {
				fixed.merge(ability, (int) bonus.getBonus(), Integer::sum);
			}
		}

		List<String> others = VttgDictionaries.allAbilities().stream()
			.filter(ability -> !fixed.containsKey(ability))
			.collect(Collectors.toList());
		List<String> any = VttgDictionaries.allAbilities();

		List<Choice> choices = new ArrayList<>();
		for (AbilityBonus bonus : bonuses) {
			AbilityType type = bonus.getAbility();
			int amount = bonus.getBonus() == null ? 1 : bonus.getBonus();
			if (type == null || amount == 0) {
				continue;
			}
			switch (type) {
				case CHOICE:
					addChoice(choices, amount, 1, others);
					break;
				case CHOICE_UNIQUE:
					addChoice(choices, amount, 2, others);
					break;
				case ONE:
					addChoice(choices, amount, 1, any);
					break;
				case CHOICE_DOUBLE:
					addChoice(choices, 2, 1, any);
					addChoice(choices, 1, 1, any);
					break;
				default:
					break;
			}
		}

		List<AbilityGroup> groups = new ArrayList<>();
		if (!fixed.isEmpty() || !choices.isEmpty()) {
			groups.add(new AbilityGroup(fixed, choices.isEmpty() ? null : choices.get(0)));
		}
		for (int i = 1; i < choices.size(); i++) {
			groups.add(new AbilityGroup(Collections.emptyMap(), choices.get(i)));
		}
		return groups;
	}

	/** Выбор с той же прибавкой и тем же пулом — одна группа с бо́льшим числом. */
	private static void addChoice(List<Choice> choices, int amount, int count, List<String> from) {
		for (int i = 0; i < choices.size(); i++) {
			Choice choice = choices.get(i);
			if (choice.amount == amount && choice.from.equals(from)) {
				choices.set(i, new Choice(amount, choice.count + count, from));
				return;
			}
		}
		choices.add(new Choice(amount, count, from));
	}

	private static Map<String, Object> featData() {
		Map<String, Object> featData = new LinkedHashMap<>();
		featData.put("type", "feat");
		return featData;
	}

	private static void putPositive(Map<String, Object> target, String field, Integer value) {
		if (value != null && value > 0) {
			target.put(field, value);
		}
	}

	private static final class Choice {
		private final int amount;
		private final int count;
		private final List<String> from;

		private Choice(int amount, int count, List<String> from) {
			this.amount = amount;
			this.count = Math.min(count, from.size());
			this.from = from;
		}
	}

	/** Одна группа повышения: фиксированные прибавки и не больше одного выбора. */
	static final class AbilityGroup {
		private final Map<String, Integer> fixed;
		private final Choice choice;

		private AbilityGroup(Map<String, Integer> fixed, Choice choice) {
			this.fixed = fixed;
			this.choice = choice;
		}

		Map<String, Object> toVttg() {
			Map<String, Object> increase = new LinkedHashMap<>();
			if (!fixed.isEmpty()) {
				increase.put("fixed", new LinkedHashMap<>(fixed));
			}
			if (choice != null) {
				Map<String, Object> value = new LinkedHashMap<>();
				value.put("amount", choice.amount);
				value.put("count", choice.count);
				value.put("from", choice.from);
				increase.put("choice", value);
			}
			return increase;
		}

		/** Текст умения для групп сверх первой — у них есть только выбор. */
		String describe() {
			if (choice == null) {
				return "";
			}
			String what = choice.count == 1 ? "одной характеристики" : choice.count + " характеристик";
			return String.format("<p>Значение %s на ваш выбор увеличивается на %d.</p>", what, choice.amount);
		}
	}
}
