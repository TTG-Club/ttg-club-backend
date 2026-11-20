package club.dnd5.portal.dto.api.races;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.races.Feature;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Setter
@Getter
public class RaceSkillApi {
	  private String name;
	  /** Блоки разметки core-api. */
	  private List<String> description;
	  private Boolean subrace;
	  private Boolean opened;
	  public RaceSkillApi(Feature feature) {
		name = feature.getName();
		description = feature.getDescription();
		if (feature.isFeature()) {
			opened = Boolean.TRUE;
		}
	}
}