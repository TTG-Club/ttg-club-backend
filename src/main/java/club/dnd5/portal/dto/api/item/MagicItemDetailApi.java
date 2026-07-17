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

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class MagicItemDetailApi extends MagicItemApi {
	private Integer id;
	private String description;
	private Boolean customization;
	private Collection<UrlApi> detailType;
	private Collection<String> detailCustamization;
	private PriceApi cost;
	private Collection<String> images;
	private String rarityRaw;
	private String typeRaw;
	private String altName;
	private String custSpecial;
	private String specialRaw;
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
		specialRaw = item.getSpecial();
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
		if (Objects.nonNull(item.getSpecial())) {
			if (detailType == null) {
				detailType = new ArrayList<>();
			}
			detailType.add(new UrlApi(item.getSpecial()));
		}

		if (!item.getCustClasses().isEmpty()) {
			detailCustamization = item.getCustClasses().stream().map(HeroClass::getAblativeName).collect(Collectors.toList());
		}
		if (Objects.nonNull(item.getCustSpecial())) {
			if (Objects.isNull(detailCustamization)) {
				detailCustamization = new ArrayList<>(1);
			}
			detailCustamization.add(item.getCustSpecial());
		}
		if (item.getRarity() != Rarity.UNKNOWN && item.getRarity() != Rarity.VARIES){
			cost = new PriceApi(item);
		}
	}
}
