package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.Armor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class ArmorDetailApi extends ArmorApi {
	private Integer id;
	private Integer armorClassRaw;
	private Integer cost;
	private String typeRaw;
	private Float weight;
	private String description;
	private Boolean disadvantage;
	private Integer requirement;
	private String duration;
	
	public ArmorDetailApi(Armor armor) {
		super(armor);
		id = armor.getId();
		url = null;
		armorClassRaw = armor.getAC();
		cost = armor.getCost();
		typeRaw = armor.getType().name();
		weight = armor.getWeight();
		description = armor.getDescription();
		if (armor.isStelsHindrance()) {
			disadvantage = Boolean.TRUE;
		}
		if (armor.getForceRequirements() != null) {
			requirement = armor.getForceRequirements();
		}
		duration = String.format("%s/%s", armor.getType().getPutting(), armor.getType().getRemoval());
	}
}
