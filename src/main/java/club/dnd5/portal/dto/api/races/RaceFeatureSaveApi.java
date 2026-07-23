package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.model.races.Feature;
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

	public RaceFeatureSaveApi(Feature raceFeature) {
		id = raceFeature.getId();
		name = raceFeature.getName();
		englishName = raceFeature.getEnglishName();
		description = raceFeature.getDescription();
		feature = raceFeature.isFeature();
		replaceFeatureId = raceFeature.getReplaceFeatureId();
	}
}
