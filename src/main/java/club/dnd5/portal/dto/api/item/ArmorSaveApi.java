package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.ArmorCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class ArmorSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	private Integer armorClass;
	private Integer cost;
	private Float weight;
	private Integer forceRequirements;
	private Boolean stealthHindrance;
	@NotNull
	private ArmorCategory type;
	private String description;
}
