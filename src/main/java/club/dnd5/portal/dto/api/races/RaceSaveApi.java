package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.model.CreatureSize;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.AbilityBonus;
import club.dnd5.portal.model.races.Feature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class RaceSaveApi {
	@NotBlank
	private String name;
	private String altName;
	@NotBlank
	private String englishName;
	private Integer minAge;
	private Integer maxAge;
	@NotBlank
	private String description;
	private Integer parentId;
	@NotNull
	private CreatureSize size;
	@NotNull
	private CreatureType type;
	private Integer darkvision;
	@Min(0)
	private int speed;
	private Integer fly;
	private Integer climb;
	private Integer swim;
	private Boolean origin;
	private boolean view = true;
	private String icon;
	private Short page;
	@Valid
	private List<RaceAbilitySaveApi> abilities;
	@Valid
	private List<RaceFeatureSaveApi> features;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public RaceSaveApi(Race race) {
		this(race, race.getBonuses(), race.getFeatures());
	}

	public RaceSaveApi(Race race, List<AbilityBonus> raceAbilities, List<Feature> raceFeatures) {
		name = race.getName();
		altName = race.getAltName();
		englishName = race.getEnglishName();
		minAge = race.getMinAge();
		maxAge = race.getMaxAge();
		description = race.getDescription();
		parentId = race.getParent() == null ? null : race.getParent().getId();
		size = race.getSize();
		type = race.getType();
		darkvision = race.getDarkvision();
		speed = race.getSpeed();
		fly = race.getFly();
		climb = race.getClimb();
		swim = race.getSwim();
		origin = race.getOrigin();
		view = race.isView();
		icon = race.getIcon();
		page = race.getPage();
		abilities = raceAbilities.stream().map(RaceAbilitySaveApi::new).collect(java.util.stream.Collectors.toList());
		features = raceFeatures.stream().map(RaceFeatureSaveApi::new).collect(java.util.stream.Collectors.toList());
		source = race.getBook() == null ? null : race.getBook().getSource();
	}
}
