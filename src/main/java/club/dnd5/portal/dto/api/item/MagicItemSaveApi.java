package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.MagicThingType;
import club.dnd5.portal.model.items.Rarity;
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
}
