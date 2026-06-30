package club.dnd5.portal.controller.api.tools;

import club.dnd5.portal.dto.api.tools.name.GeneratedNameApi;
import club.dnd5.portal.dto.api.tools.name.NameGenerationRequest;
import club.dnd5.portal.dto.api.tools.name.NameRaceApi;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.service.NameGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class NameGeneratorApiController {
	private final RaceRepository raceRepository;
	private final NameGeneratorService nameGeneratorService;

	@GetMapping("/api/v1/tools/names")
	public List<NameRaceApi> getRaces() {
		List<Race> races = raceRepository.findAll();
		Set<Integer> availableIds = races.stream()
			.filter(this::hasNames)
			.map(Race::getId)
			.collect(Collectors.toSet());
		Set<Integer> groupIds = races.stream()
			.filter(race -> availableIds.contains(race.getId()) && race.getParent() != null)
			.map(race -> race.getParent().getId())
			.collect(Collectors.toCollection(HashSet::new));

		return races.stream()
			.filter(race -> availableIds.contains(race.getId()) || groupIds.contains(race.getId()))
			.map(race -> new NameRaceApi(
				race.getId(),
				race.getName(),
				race.getParent() == null ? null : race.getParent().getId(),
				availableIds.contains(race.getId())
			))
			.sorted(Comparator.comparing(NameRaceApi::getName))
			.collect(Collectors.toList());
	}

	@PostMapping("/api/v1/tools/names")
	public List<GeneratedNameApi> generate(@Valid @RequestBody NameGenerationRequest request) {
		return nameGeneratorService.generate(request);
	}

	private boolean hasNames(Race race) {
		return !race.getNames().isEmpty();
	}
}
