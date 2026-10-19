package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.trait.Trait;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Черта в формате VTTG ({@code feat}).
 *
 * <p>Требование уезжает строкой сайта, уровень — числом. Характеристики черты на сайте —
 * фильтр «какие характеристики затрагивает», поэтому повышением они становятся, только
 * когда описание его называет: «значение … увеличивается на 1».</p>
 */
@Component
public class VttgFeatMapper {
	static final String SECTION = "feats";
	private static final String NO_REQUIREMENT = "нет";

	public Map<String, Object> toVttg(Trait trait) {
		String key = trait.getUrlName();

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("type", "feat");
		data.put("id", key);
		data.put("name", VttgText.orEmpty(trait.getName()));
		data.put("nameEn", VttgText.orEmpty(trait.getEnglishName()));
		data.put("section", SECTION);
		data.put("srcSection", SECTION);
		data.put("srcUrl", key);
		data.put("isSRD", false);
		data.put("sourceKey", VttgDictionaries.sourceKey(trait.getBook()));
		data.put("featureType", "feat");
		data.put("typeLabel", "Черты");
		data.put("description", VttgText.html(trait.getDescription()));
		data.put("repeatable", false);

		Map<String, Object> featData = new LinkedHashMap<>();
		featData.put("type", "feat");
		Map<String, Object> increase = abilityScoreIncrease(trait);
		if (increase != null) {
			featData.put("abilityScoreIncrease", increase);
		}
		Map<String, Object> prerequisite = prerequisite(trait);
		if (prerequisite != null) {
			featData.put("prerequisite", prerequisite);
		}
		if (featData.size() > 1) {
			data.put("featData", featData);
		}
		return data;
	}

	static Map<String, Object> abilityScoreIncrease(Trait trait) {
		if (trait.getAbilities() == null || trait.getAbilities().isEmpty() || !mentionsIncrease(trait.getDescription())) {
			return null;
		}
		List<String> abilities = trait.getAbilities().stream()
			.map(VttgDictionaries::ability)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
		if (abilities.isEmpty()) {
			return null;
		}

		Map<String, Object> increase = new LinkedHashMap<>();
		if (abilities.size() == 1) {
			Map<String, Object> fixed = new LinkedHashMap<>();
			fixed.put(abilities.get(0), 1);
			increase.put("fixed", fixed);
		} else {
			Map<String, Object> choice = new LinkedHashMap<>();
			choice.put("amount", 1);
			choice.put("count", 1);
			choice.put("from", abilities);
			increase.put("choice", choice);
		}
		increase.put("upto", 20);
		return increase;
	}

	private static boolean mentionsIncrease(String description) {
		if (description == null) {
			return false;
		}
		String text = description.toLowerCase(Locale.ROOT);
		return text.contains("увеличивается на 1") || text.contains("увеличьте");
	}

	private static Map<String, Object> prerequisite(Trait trait) {
		Map<String, Object> prerequisite = new LinkedHashMap<>();
		String requirement = VttgText.optional(trait.getRequirement());
		if (requirement != null && !NO_REQUIREMENT.equalsIgnoreCase(requirement)) {
			prerequisite.put("text", requirement);
		}
		if (trait.getLevel() != null && trait.getLevel() > 1) {
			prerequisite.put("minLevel", trait.getLevel());
		}
		return prerequisite.isEmpty() ? null : prerequisite;
	}
}
