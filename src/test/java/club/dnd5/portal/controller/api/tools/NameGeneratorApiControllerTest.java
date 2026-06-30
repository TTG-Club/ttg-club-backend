package club.dnd5.portal.controller.api.tools;

import club.dnd5.portal.dto.api.tools.name.NameRaceApi;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.races.RaceName;
import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.service.NameGeneratorService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NameGeneratorApiControllerTest {
	@Test
	void shouldReturnOnlyRacesWithOwnNameList() {
		RaceRepository raceRepository = mock(RaceRepository.class);
		Race parent = race(1, "Эльф");
		parent.setNames(Collections.singletonList(new RaceName(1, "Адар", Sex.MALE, parent)));
		Race subrace = race(2, "Высший");
		subrace.setParent(parent);
		subrace.setNames(Collections.emptyList());
		when(raceRepository.findAll()).thenReturn(Arrays.asList(parent, subrace));
		NameGeneratorApiController controller = new NameGeneratorApiController(
			raceRepository,
			mock(NameGeneratorService.class)
		);

		List<NameRaceApi> result = controller.getRaces();

		assertEquals(1, result.size());
		assertEquals(parent.getId(), result.get(0).getId());
		assertTrue(result.get(0).isAvailable());
	}

	@Test
	void shouldIncludeParentAsGroupWhenOnlySubraceHasNames() {
		RaceRepository raceRepository = mock(RaceRepository.class);
		Race parent = race(1, "Эльф");
		parent.setNames(Collections.emptyList());
		Race subrace = race(2, "Высший эльф");
		subrace.setParent(parent);
		subrace.setNames(Collections.singletonList(new RaceName(1, "Адар", Sex.MALE, subrace)));
		when(raceRepository.findAll()).thenReturn(Arrays.asList(parent, subrace));
		NameGeneratorApiController controller = new NameGeneratorApiController(
			raceRepository,
			mock(NameGeneratorService.class)
		);

		List<NameRaceApi> result = controller.getRaces();

		assertEquals(2, result.size());
		assertEquals(1, result.stream().filter(NameRaceApi::isAvailable).count());
		assertEquals(parent.getId(), result.stream()
			.filter(item -> !item.isAvailable())
			.findFirst()
			.orElseThrow(AssertionError::new)
			.getId());
	}

	private Race race(int id, String name) {
		Race race = new Race();
		race.setId(id);
		race.setName(name);
		return race;
	}
}
