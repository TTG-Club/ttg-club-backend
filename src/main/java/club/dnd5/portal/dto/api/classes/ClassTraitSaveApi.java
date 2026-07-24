package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.classes.HeroClassTrait;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
public class ClassTraitSaveApi {
	private Integer id;
	@NotBlank
	private String name;
	private String suffix;
	@Min(1)
	@Max(20)
	private byte level;
	@NotBlank
	private String description;
	private boolean optional;
	private String child;

	public ClassTraitSaveApi(HeroClassTrait trait) {
		id = trait.getId();
		name = trait.getName();
		suffix = trait.getSuffix();
		level = trait.getLevel();
		description = trait.getDescription();
		optional = trait.getOptional() != 0;
		child = trait.getChild();
	}

	public ClassTraitSaveApi(ArchetypeTrait trait) {
		id = trait.getId();
		name = trait.getName();
		suffix = trait.getSuffix();
		level = trait.getLevel();
		description = trait.getDescription();
		child = trait.getChild();
	}
}
