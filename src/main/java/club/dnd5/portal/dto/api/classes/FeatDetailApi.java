package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.dto.api.spell.ReferenceClassApi;
import club.dnd5.portal.model.trait.Trait;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class FeatDetailApi extends TraitApi {
	private String description;
	private List<ReferenceClassApi> classes;

	public FeatDetailApi(Trait trait) {
		super(trait);
		url = null;
		description = trait.getDescription();
	}
}