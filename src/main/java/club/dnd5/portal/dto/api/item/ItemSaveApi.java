package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.Currency;
import club.dnd5.portal.model.items.EquipmentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ItemSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	private Integer cost;
	private Currency currency;
	private Float weight;
	private String description;
	private List<EquipmentType> categories;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;
}
