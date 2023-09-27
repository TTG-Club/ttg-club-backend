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
	private String size;
	private Collection<NameValueApi> speed = new ArrayList<>(5);
	private Collection<String> images;
	private Integer darkvision;
	protected Collection<RaceSkillApi> skills;

	public RaceDetailApi(Race race, Set<String> books) {
		super(race, books);
		description = race.getDescription();
		url = null;
		type = race.getType().getCyrillicName();
		size = race.getSize().getCyrillicName();
		speed.add(new NameValueApi(null, race.getSpeed()));
		if (Objects.nonNull(race.getFly())) {
			speed.add(new NameValueApi("летая", race.getFly()));
		}
		if (Objects.nonNull(race.getClimb())) {
			speed.add(new NameValueApi("лазая", race.getClimb()));
		}
		if (Objects.nonNull(race.getSwim())) {
			speed.add(new NameValueApi("плавая", race.getSwim()));
		}
		darkvision = race.getDarkvision();
		if (!race.getSubRaces().isEmpty()) {
			subraces = race.getSubRaces()
				.stream()
				.filter(r -> books.isEmpty()? true : books.contains(r.getBook().getSource()))
				.map(race1 -> new RaceDetailApi(race1, books))
				.collect(Collectors.toList());
		}
		fillSkill(race);
	}

	protected void fillSkill(Race race) {
		if (race.getParent() != null) {
			final Set<Integer> replaceFeatureIds = race.getFeatures()
					.stream()
					.map(Feature::getReplaceFeatureId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
			List<RaceSkillApi> subraceSkills = race.getFeatures()
					.stream()
					.map(RaceSkillApi::new)
					.peek(api -> api.setSubrace(Boolean.TRUE))
					.collect(Collectors.toList());
			skills = race.getParent().getFeatures()
					.stream().filter(skill -> !replaceFeatureIds.contains(skill.getId()))
					.map(RaceSkillApi::new)
					.collect(Collectors.toList());
			skills.addAll(subraceSkills);
		} else {
			skills = race.getFeatures().stream().map(RaceSkillApi::new).collect(Collectors.toList());
		}
		raceFeatureName(skills, race.getAllNames(), race.getAllNicknames());
	}

	private void raceFeatureName(Collection<RaceSkillApi> skills, Map<Sex, Set<String>> names, List<RaceNickname> nickNames) {
		String featureName = "Имена";
		RaceSkillApi existingSkill = skills.stream()
			.filter(skill -> skill.getName().equals(featureName))
			.findFirst()
			.orElse(null);

		StringBuilder descriptionBuilder = new StringBuilder("Имена:\n");
		names.forEach((sex, nameSet) -> {
			descriptionBuilder.append(sex.getCyrilicName()).append(":\n");
			nameSet.forEach(name -> descriptionBuilder.append("- ").append(name).append("\n"));
		});

		if (!descriptionBuilder.toString().equals("Имена:\n")) {
			if (existingSkill != null) {
				// "Имена" feature already exists, update its description
				existingSkill.setDescription(existingSkill.getDescription() + descriptionBuilder.toString());
			} else {
				// "Имена" feature doesn't exist, create a new one
				Feature feature = new Feature();
				feature.setName(featureName);
				feature.setDescription(descriptionBuilder.toString());

				RaceSkillApi raceSkillApi = new RaceSkillApi(feature);
				skills.add(raceSkillApi);
			}
		}
	}



}
