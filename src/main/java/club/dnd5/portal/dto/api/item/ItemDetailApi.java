package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.EquipmentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class ItemDetailApi extends ItemApi {
	private Float weight;
	private String description;
	private List<String> categories;
	private String image;
	
	public ItemDetailApi(Equipment item) {
		super(item);
		url = null;
		weight = item.getWeight();
		if (item.getDescription() != null) {
			description = item.getDescription();
		}
		categories = item.getTypes().stream()
				.map(EquipmentType::getCyrilicName)
				.collect(Collectors.toList());
		setPrice(item.getTextCost());
	}
}