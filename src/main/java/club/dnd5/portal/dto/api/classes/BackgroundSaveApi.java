package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.background.LifeStyle;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.Language;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

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
	@NotBlank
	private String description;
	private String skillName;
	private String skillDescription;
	private String personalization;
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
		language = background.getLanguage();
		languages = background.getLanguages() == null ? java.util.Collections.emptyList()
			: background.getLanguages().stream().map(Language::getName).collect(java.util.stream.Collectors.toList());
		lifeStyle = background.getLifeStyle();
		source = background.getBook() == null ? null : background.getBook().getSource();
	}
}
