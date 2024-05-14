package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.dto.api.Randomizable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class MagicItemFilter implements Randomizable {
	List<String> rarity;
	List<String> type;
	List<String> customization;
	List<String> consumable;
	List<String> charge;
	@JsonProperty("book")
	private List<String> books;
	boolean random;

	@Override
	public boolean getRandom() {
		return random;
	}
}
