package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.background.LifeStyle;
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
	private String personalization;
	private String language;
	private List<String> languages;
	private LifeStyle lifeStyle;
}
