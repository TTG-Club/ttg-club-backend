package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.MagicThingType;
import club.dnd5.portal.model.items.Rarity;
import club.dnd5.portal.model.items.MagicItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
@Setter
public class MagicItemSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	@NotNull
	private Rarity rarity;
	@NotNull
	private MagicThingType type;
	private Boolean customization;
	private String custSpecial;
	private String special;
	@NotBlank
	private String description;
	private Boolean consumed;
	private Integer charge;
	private Boolean curse;
	private Integer cost;
	private Byte bonus;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public MagicItemSaveApi(MagicItem item) {
		name = item.getName();
		englishName = item.getEnglishName();
		altName = item.getAltName();
		rarity = item.getRarity();
		type = item.getType();
		customization = item.getCustomization();
		custSpecial = item.getCustSpecial();
		special = item.getSpecial();
		description = item.getDescription();
		consumed = item.isConsumed();
		charge = item.getCharge();
		curse = item.getCurse();
		cost = item.getCost();
		bonus = item.getBonus();
		source = item.getBook() == null ? null : item.getBook().getSource();
	}
}
