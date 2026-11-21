package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.dto.api.UrlApi;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.items.MagicItem;
import club.dnd5.portal.model.items.Rarity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.List;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class MagicItemDetailApi extends MagicItemApi {
	private Integer id;
	/** Разметка core-api: массив строк-блоков. */
	private List<String> description;
	private Boolean customization;
	private Collection<UrlApi> detailType;
	private Collection<String> detailCustamization;
	private PriceApi cost;
	private Collection<String> images;
	private String rarityRaw;
	private String typeRaw;
	private String altName;
	/** Условие настройки: блоки разметки core-api, выводится в строку после классов. */
	private List<String> custSpecial;
	/** Уточнение типа: блоки разметки core-api, выводится в строку после доспехов и оружия. */
	private List<String> special;
	private Boolean consumed;
	private Integer charge;
	private Boolean curse;
	private Integer costRaw;
	private Byte bonus;

	public MagicItemDetailApi(MagicItem item) {
		super(item);
		id = item.getId();
		url = null;
		description = item.getDescription();
		rarityRaw = item.getRarity().name();
		typeRaw = item.getType().name();
		altName = item.getAltName();
		custSpecial = item.getCustSpecial();
		special = item.getSpecial();
		consumed = item.isConsumed();
		charge = item.getCharge();
		curse = item.getCurse();
		costRaw = item.getRawCost();
		bonus = item.getBonus();
		if (item.getCustomization()) {
			customization = Boolean.TRUE;
		}
		if (!item.getArmors().isEmpty()) {
			detailType = item.getArmors().stream().map(UrlApi::new).collect(Collectors.toList());
		}
		if (!item.getWeapons().isEmpty()) {
			detailType = item.getWeapons().stream().map(UrlApi::new).collect(Collectors.toList());
		}

		if (!item.getCustClasses().isEmpty()) {
			detailCustamization = item.getCustClasses().stream().map(HeroClass::getAblativeName).collect(Collectors.toList());
		}
		if (item.getRarity() != Rarity.UNKNOWN && item.getRarity() != Rarity.VARIES){
			cost = new PriceApi(item);
		}
	}
}
