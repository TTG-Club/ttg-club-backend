package club.dnd5.portal.dto.api.races;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@Getter
@Setter
public class RaceFeatureSaveApi {
	private Integer id;
	@NotBlank
	private String name;
	private String englishName;
	@NotBlank
	private String description;
	private boolean feature;
	private Integer replaceFeatureId;
}
