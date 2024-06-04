package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.ArmorCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class ArmorFilter {
	@JsonProperty("book")
	private Set<String> books;

	@JsonProperty("disadvantage")
	private Set<String> disadvantage;

	@JsonProperty("type")
	private Set<ArmorCategory> typeArmor;

	@JsonProperty("strengthRequirements")
	private Set<Integer> strengthRequirements;
}
