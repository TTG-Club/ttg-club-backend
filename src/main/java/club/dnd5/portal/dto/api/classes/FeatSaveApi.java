package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class FeatSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	private Integer level;
	private String requirement;
	@NotBlank
	private String description;
	private List<AbilityType> abilities;
	private List<SkillType> skills;
}
