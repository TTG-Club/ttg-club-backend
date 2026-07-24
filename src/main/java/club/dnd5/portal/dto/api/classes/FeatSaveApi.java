package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.trait.Trait;
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

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public FeatSaveApi(Trait trait) {
		name = trait.getName();
		englishName = trait.getEnglishName();
		altName = trait.getAltName();
		level = trait.getLevel();
		requirement = trait.getRequirement();
		description = trait.getDescription();
		abilities = trait.getAbilities();
		skills = trait.getSkills();
		source = trait.getBook() == null ? null : trait.getBook().getSource();
	}
}
