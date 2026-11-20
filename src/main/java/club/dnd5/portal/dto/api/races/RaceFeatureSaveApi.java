package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.model.races.Feature;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class RaceFeatureSaveApi {
	private Integer id;
	@NotBlank
	private String name;
	private String englishName;
	/** Блоки разметки core-api; HTML-строку старых ревизий аудита переводит десериализатор. */
	@NotEmpty
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;
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
