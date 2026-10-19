package club.dnd5.portal.service.vttg;

import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.background.Personalization;
import club.dnd5.portal.model.background.PersonalizationType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
 * Предыстория в формате VTTG ({@code BackgroundDefinition}).
 *
 * <p>В редакции 2014 предыстория не даёт ни характеристик, ни черты: вместо черты у неё
 * своё умение («Убежище верующих»), которое уезжает полем {@code feature}. Владение
 * инструментами, языки и снаряжение на сайте записаны текстом — они остаются в описании,
 * а в дары уходит то, что на сайте задано справочником: навыки и названные языки.</p>
 */
@Component
public class VttgBackgroundMapper {
	static final String SECTION = "backgrounds";
	private static final Pattern LANGUAGE_COUNT = Pattern.compile(
		"(один|одного|два|двух|три|трёх|трех|любой|любого|1|2|3)\\s+(?:\\S+\\s+)?язык", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

	public Map<String, Object> toVttg(Background background) {
		String key = background.getUrlName();

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("type", "background");
		data.put("id", key);
		data.put("key", key);
		data.put("section", SECTION);
		data.put("srcSection", SECTION);
		data.put("srcUrl", key);
		data.put("isSRD", false);
		data.put("name", VttgText.orEmpty(background.getName()));
		data.put("nameEn", VttgText.orEmpty(background.getEnglishName()));
		data.put("description", description(background));
		data.put("sourceKey", VttgDictionaries.sourceKey(background.getBook()));

		Map<String, Object> skillGrant = new LinkedHashMap<>();
		skillGrant.put("skills", background.getSkills() == null
			? Collections.emptyList()
			: background.getSkills().stream()
				.filter(Objects::nonNull)
				.map(VttgDictionaries::skill)
				.distinct()
				.collect(Collectors.toList()));
		data.put("skillGrant", skillGrant);

		Map<String, Object> toolGrant = new LinkedHashMap<>();
		toolGrant.put("items", Collections.emptyList());
		data.put("toolGrant", toolGrant);

		data.put("equipmentOptions", equipment(background));

		if (!VttgText.isBlank(background.getSkillName())) {
			Map<String, Object> feature = new LinkedHashMap<>();
			feature.put("name", background.getSkillName().trim());
			feature.put("description", VttgText.html(background.getSkillDescription()));
			data.put("feature", feature);
		}

		Map<String, Object> featData = featData(background);
		if (featData.size() > 1) {
			data.put("featData", featData);
		}
		return data;
	}

	private List<Map<String, Object>> equipment(Background background) {
		if (VttgText.isBlank(background.getEquipmentsText()) && background.getStartMoney() <= 0) {
			return Collections.emptyList();
		}
		Map<String, Object> option = new LinkedHashMap<>();
		option.put("description", VttgText.html(background.getEquipmentsText()));
		if (background.getStartMoney() > 0) {
			option.put("coins", background.getStartMoney());
		}
		return Collections.singletonList(option);
	}

	private Map<String, Object> featData(Background background) {
		Map<String, Object> featData = new LinkedHashMap<>();
		featData.put("type", "feat");

		List<String> languages = background.getLanguages() == null
			? Collections.emptyList()
			: background.getLanguages().stream()
				.filter(Objects::nonNull)
				.map(Language::getName)
				.filter(name -> !VttgText.isBlank(name))
				.distinct()
				.collect(Collectors.toList());
		if (!languages.isEmpty()) {
			featData.put("languages", languages);
		}

		int languageChoices = languageChoiceCount(background.getLanguage());
		if (languageChoices > 0) {
			Map<String, Object> choice = new LinkedHashMap<>();
			choice.put("key", "language");
			choice.put("type", "language");
			choice.put("label", "Выберите язык");
			choice.put("count", languageChoices);
			List<Map<String, Object>> choices = new ArrayList<>();
			choices.add(choice);
			featData.put("choices", choices);
		}
		return featData;
	}

	/**
	 * Сколько языков игрок выбирает сам: «Два языка на ваш выбор» → 2. Названные
	 * языки уезжают справочником, поэтому считается только выбор.
	 */
	static int languageChoiceCount(String text) {
		if (VttgText.isBlank(text) || !text.toLowerCase(Locale.ROOT).contains("выбор")) {
			return 0;
		}
		Matcher matcher = LANGUAGE_COUNT.matcher(text);
		if (!matcher.find()) {
			return 1;
		}
		String word = matcher.group(1).toLowerCase(Locale.ROOT);
		if (word.startsWith("дв") || word.equals("2")) {
			return 2;
		}
		if (word.startsWith("тр") || word.equals("3")) {
			return 3;
		}
		return 1;
	}

	/**
	 * Описание с тем, что на сайте показано отдельными блоками: владения и снаряжение
	 * текстом и таблицы характера. Снаряжение здесь не повторяется: оно уезжает вариантом
	 * {@code equipmentOptions}, и VTTG показывает его сам.
	 */
	private String description(Background background) {
		StringBuilder html = new StringBuilder();
		appendLine(html, "Владение навыками", skillsText(background));
		appendLine(html, "Владение инструментами", background.getToolOwnership());
		appendLine(html, "Языки", background.getLanguage());
		html.append(VttgText.html(background.getDescription()));
		if (!VttgText.isBlank(background.getPersonalization())) {
			html.append("<h3>Персонализация</h3>").append(VttgText.html(background.getPersonalization()));
		}
		appendPersonalization(html, background.getPersonalizations());
		return html.toString();
	}

	private String skillsText(Background background) {
		String skills = background.getSkills() == null
			? ""
			: background.getSkills().stream()
				.filter(Objects::nonNull)
				.map(SkillType::getCyrilicName)
				.collect(Collectors.joining(", "));
		if (!VttgText.isBlank(background.getOtherSkills())) {
			skills = skills.isEmpty() ? background.getOtherSkills() : skills + ", " + background.getOtherSkills();
		}
		return skills;
	}

	private void appendLine(StringBuilder html, String label, String value) {
		if (VttgText.isBlank(value)) {
			return;
		}
		html.append("<p><strong>").append(label).append(":</strong> ")
			.append(VttgText.inline(value)).append("</p>");
	}

	/** Таблицы характера: по таблице на тип, строки нумеруются под бросок кости. */
	private void appendPersonalization(StringBuilder html, List<Personalization> personalizations) {
		if (personalizations == null || personalizations.isEmpty()) {
			return;
		}
		Map<PersonalizationType, List<String>> byType = new LinkedHashMap<>();
		for (Personalization personalization : personalizations) {
			if (personalization.getType() != null && !VttgText.isBlank(personalization.getText())) {
				byType.computeIfAbsent(personalization.getType(), type -> new ArrayList<>())
					.add(personalization.getText().trim());
			}
		}
		for (Map.Entry<PersonalizationType, List<String>> entry : byType.entrySet()) {
			List<String> rows = entry.getValue();
			html.append("<h3>").append(VttgText.escape(entry.getKey().getName())).append("</h3>")
				.append("<table><thead><tr><th>к").append(rows.size()).append("</th><th>")
				.append(VttgText.escape(entry.getKey().getName())).append("</th></tr></thead><tbody>");
			for (int i = 0; i < rows.size(); i++) {
				html.append("<tr><td>").append(i + 1).append("</td><td>")
					.append(VttgText.inline(rows.get(i))).append("</td></tr>");
			}
			html.append("</tbody></table>");
		}
	}
}
