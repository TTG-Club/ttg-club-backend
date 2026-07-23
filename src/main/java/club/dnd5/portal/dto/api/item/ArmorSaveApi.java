package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.ArmorCategory;
import club.dnd5.portal.model.items.Armor;
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

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public ArmorSaveApi(Armor armor) {
		name = armor.getName();
		englishName = armor.getEnglishName();
		altName = armor.getAltName();
		armorClass = armor.getAC();
		cost = armor.getCost();
		weight = armor.getWeight();
		forceRequirements = armor.getForceRequirements();
		stealthHindrance = armor.isStelsHindrance();
		type = armor.getType();
		description = armor.getDescription();
		source = armor.getBook() == null ? null : armor.getBook().getSource();
	}
}
