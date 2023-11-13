package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.ArmorCategory;
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
public class ArmorFilter {
	@JsonProperty("book")
	private List<String> books;

	@JsonProperty("disadvantage")
	private Boolean disadvantage;

	@JsonProperty("type")
	private List<ArmorCategory> typeArmor;

	@JsonProperty("strengthRequirements")
	private List<Integer> strengthRequirements;
}
