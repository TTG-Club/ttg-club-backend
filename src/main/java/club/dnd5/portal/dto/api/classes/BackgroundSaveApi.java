package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.background.LifeStyle;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.background.Personalization;
import club.dnd5.portal.model.background.PersonalizationType;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
public class BackgroundSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	private List<SkillType> skills;
	private String otherSkills;
	private String toolOwnership;
	private String equipments;
	private Integer startGold;
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@NotEmpty
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;
	private String skillName;
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> skillDescription;
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> personalization;
	@Valid
	private List<BackgroundPersonalizationTableSaveApi> personalizationTables;
	private String language;
	private List<String> languages;
	private LifeStyle lifeStyle;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public BackgroundSaveApi(Background background) {
		name = background.getName();
		englishName = background.getEnglishName();
		altName = background.getAltName();
		skills = background.getSkills();
		otherSkills = background.getOtherSkills();
		toolOwnership = background.getToolOwnership();
		equipments = background.getEquipmentsText();
		startGold = background.getStartMoney();
		description = background.getDescription();
		skillName = background.getSkillName();
		skillDescription = background.getSkillDescription();
		personalization = background.getPersonalization();
		personalizationTables = new ArrayList<>();
		if (background.getPersonalizations() != null) {
			Map<PersonalizationType, List<Personalization>> tables = background.getPersonalizations().stream()
				.collect(Collectors.groupingBy(
					Personalization::getType,
					() -> new EnumMap<>(PersonalizationType.class),
					Collectors.toList()
				));
			personalizationTables = tables.entrySet().stream()
				.map(entry -> new BackgroundPersonalizationTableSaveApi(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
		}
		language = background.getLanguage();
		languages = background.getLanguages() == null ? java.util.Collections.emptyList()
			: background.getLanguages().stream().map(Language::getName).collect(java.util.stream.Collectors.toList());
		lifeStyle = background.getLifeStyle();
		source = background.getBook() == null ? null : background.getBook().getSource();
	}
}
