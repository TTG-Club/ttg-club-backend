package club.dnd5.portal.dto.api.item;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.items.MagicItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PriceApi {
	private String dmg;
	private String xge;
	public PriceApi(MagicItem item) {
		dmg = item.getRangeCostDMG();
		if (item.isConsumed()) {
			xge = String.format("(%s) / 2", item.getRangeCostXGE());
		} else {
			xge = item.getRangeCostXGE();
		}
	}
}
