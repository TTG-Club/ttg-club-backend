package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.EquipmentType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class ItemDetailApi extends ItemApi {
	private Integer id;
	private Float weight;
	private String description;
	private List<String> categories;
	private String image;
	private String altName;
	private Integer cost;
	private String currency;
	private List<String> categoriesRaw;

	public ItemDetailApi(Equipment item) {
		super(item);
		id = item.getId();
		url = null;
		weight = item.getWeight();
		if (item.getDescription() != null) {
			description = item.getDescription();
		}
		Set<EquipmentType> types = item.getTypes() == null ? Collections.emptySet() : item.getTypes();
		categories = types.stream()
				.map(EquipmentType::getCyrilicName)
				.collect(Collectors.toList());
		categoriesRaw = types.stream()
				.map(EquipmentType::name)
				.collect(Collectors.toList());
		altName = item.getAltName();
		cost = item.getCost();
		if (item.getCurrency() != null) {
			currency = item.getCurrency().name();
		}
		setPrice(item.getTextCost());
	}
}