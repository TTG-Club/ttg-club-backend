package club.dnd5.portal.dto.api.races;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.model.races.Feature;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.races.RaceNickname;
import club.dnd5.portal.model.races.Sex;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@Setter
@Getter
public class RaceDetailApi extends RaceApi {
	private String description;
	private String altName;
	private Integer minAge;
	private Integer maxAge;
	private String size;
	private String sizeRaw;
	private String typeRaw;
	private Collection<NameValueApi> speed = new ArrayList<>(5);
	private Collection<String> images;
	private Integer darkvision;
	private Integer fly;
	private Integer climb;
	private Integer swim;
	private Boolean origin;
	private boolean view;
	private String icon;
	private Short page;
	private Integer parentId;
	private Collection<RaceFeatureApi> features;
	protected Collection<RaceSkillApi> skills;

	public RaceDetailApi(Race race, Set<String> books) {
		super(race, books);
		description = race.getDescription();
		altName = race.getAltName();
		minAge = race.getMinAge();
		maxAge = race.getMaxAge();
		url = null;
		type = race.getType().getCyrillicName();
		typeRaw = race.getType().name();
		size = race.getSize().getCyrillicName();
		sizeRaw = race.getSize().name();
		speed.add(NameValueApi.builder().value(race.getSpeed()).build());
		if (Objects.nonNull(race.getFly())) {
			fly = race.getFly();
			speed.add(NameValueApi.builder()
				.name("летая")
				.value(race.getFly())
				.build());
		}
		if (Objects.nonNull(race.getClimb())) {
			climb = race.getClimb();
			speed.add(NameValueApi.builder()
				.name("лазая")
				.value(race.getClimb())
				.build());
		}
		if (Objects.nonNull(race.getSwim())) {
			swim = race.getSwim();
			speed.add(NameValueApi.builder()
				.name("плавая")
				.value(race.getSwim())
				.build());
		}
		darkvision = race.getDarkvision();
		origin = race.getOrigin();
		view = race.isView();
		icon = race.getIcon();
		page = race.getPage();
		parentId = race.getParent() == null ? null : race.getParent().getId();
		features = race.getFeatures() == null
			? Collections.emptyList()
			: race.getFeatures().stream().map(RaceFeatureApi::new).collect(Collectors.toList());
		if (race.getSubRaces() != null && !race.getSubRaces().isEmpty()) {
			subraces = race.getSubRaces()
				.stream()
				.filter(r -> books.isEmpty() || books.contains(r.getBook().getSource()))
				.map(race1 -> new RaceDetailApi(race1, books))
				.collect(Collectors.toList());
		}
		fillSkill(race);
	}

	@Getter
	public static class RaceFeatureApi {
		private final Integer id;
		private final String name;
		private final String englishName;
		private final String description;
		private final boolean feature;
		private final Integer replaceFeatureId;

		private RaceFeatureApi(Feature feature) {
			id = feature.getId();
			name = feature.getName();
			englishName = feature.getEnglishName();
			description = feature.getDescription();
			this.feature = feature.isFeature();
			replaceFeatureId = feature.getReplaceFeatureId();
		}
	}

	protected void fillSkill(Race race) {
		if (race.getParent() != null) {
			List<Feature> raceFeatures = race.getFeatures() == null ? Collections.emptyList() : race.getFeatures();
			List<Feature> parentFeatures = race.getParent().getFeatures() == null ? Collections.emptyList() : race.getParent().getFeatures();
			final Set<Integer> replaceFeatureIds = raceFeatures
					.stream()
					.map(Feature::getReplaceFeatureId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
			List<RaceSkillApi> subraceSkills = raceFeatures
					.stream()
					.map(RaceSkillApi::new)
					.peek(api -> api.setSubrace(Boolean.TRUE))
					.collect(Collectors.toList());
			skills = parentFeatures
					.stream().filter(skill -> !replaceFeatureIds.contains(skill.getId()))
					.map(RaceSkillApi::new)
					.collect(Collectors.toList());
			skills.addAll(subraceSkills);
		} else {
			skills = race.getFeatures() == null
				? new ArrayList<>()
				: race.getFeatures().stream().map(RaceSkillApi::new).collect(Collectors.toList());
		}
		raceFeatureName(skills, race.getAllNames(), race.getAllNicknames());
	}

	private void raceFeatureName(Collection<RaceSkillApi> skills, Map<Sex, Set<String>> names, List<RaceNickname> nickNames) {
		String featureName = "Имена";
		RaceSkillApi existingSkill = skills.stream()
			.filter(skill -> skill.getName().equals(featureName))
			.findFirst()
			.orElse(null);

		StringBuilder descriptionBuilder = new StringBuilder();

		names.forEach((sex, nameSet) -> {
			descriptionBuilder.append("<p><strong>")
				.append(sex.getCyrilicName())
				.append(" имена:</strong> ")
				.append(String.join(", ", nameSet))
				.append("</p>");
		});

		if (nickNames != null && !nickNames.isEmpty()) {
			for (RaceNickname.NicknameType nicknameType : RaceNickname.NicknameType.values()) {
				String displayName = nicknameType.getDisplay();

				List<String> nicknamesOfType = nickNames.stream()
					.filter(nickname -> nickname.getType() == nicknameType)
					.map(RaceNickname::getName)
					.sorted()
					.collect(Collectors.toList());

				if (!nicknamesOfType.isEmpty()) {
					descriptionBuilder.append("<p><strong>")
						.append(displayName)
						.append("</strong> ");

					String formattedNicknames = String.join(", ", nicknamesOfType);

					descriptionBuilder.append(formattedNicknames)
						.append("</p>");
				}
			}
		}

		if (!descriptionBuilder.toString().isEmpty()) {
			if (existingSkill != null) {
				existingSkill.setDescription(existingSkill.getDescription() + descriptionBuilder);
			} else {
				Feature feature = new Feature();
				feature.setName(featureName);
				feature.setDescription(descriptionBuilder.toString());

				RaceSkillApi raceSkillApi = new RaceSkillApi(feature);
				skills.add(raceSkillApi);
			}
		}
	}
}
