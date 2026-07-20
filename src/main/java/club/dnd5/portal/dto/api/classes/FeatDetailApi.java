package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.dto.api.spell.ReferenceClassApi;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.trait.Trait;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Collections;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class FeatDetailApi extends FeatApi {
	private Integer id;
	private String description;
	private List<String> abilities;
	private List<String> skills;
	private List<ReferenceClassApi> classes;
	private String altName;
	private Integer level;

	public FeatDetailApi(Trait trait) {
		super(trait);
		id = trait.getId();
		url = null;
		description = trait.getDescription();
		altName = trait.getAltName();
		level = trait.getLevel();
		abilities = (trait.getAbilities() == null ? Collections.<AbilityType>emptyList() : trait.getAbilities())
			.stream().map(ability -> ability.name()).collect(java.util.stream.Collectors.toList());
		skills = (trait.getSkills() == null ? Collections.<SkillType>emptyList() : trait.getSkills())
			.stream().map(skill -> skill.name()).collect(java.util.stream.Collectors.toList());
	}
}
