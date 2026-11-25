package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.Currency;
import club.dnd5.portal.model.items.EquipmentType;
import club.dnd5.portal.model.items.Equipment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

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
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;
	private List<EquipmentType> categories;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public ItemSaveApi(Equipment item) {
		name = item.getName();
		englishName = item.getEnglishName();
		altName = item.getAltName();
		cost = item.getCost();
		currency = item.getCurrency();
		weight = item.getWeight();
		description = item.getDescription();
		categories = item.getTypes() == null ? null : item.getTypes().stream().sorted().collect(java.util.stream.Collectors.toList());
		source = item.getBook() == null ? null : item.getBook().getSource();
	}
}
