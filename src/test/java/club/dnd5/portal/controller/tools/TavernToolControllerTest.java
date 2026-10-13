package club.dnd5.portal.controller.tools;

import club.dnd5.portal.dto.api.tools.name.GeneratedNameApi;
import club.dnd5.portal.dto.api.tools.name.NameGenerationRequest;
import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.service.NameGeneratorService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TavernToolControllerTest {

	@Mock private RaceRepository raceRepository;
	@Mock private NameGeneratorService nameGeneratorService;
	@InjectMocks private TavernToolController controller;

	@Test
	void smallVisitorPoolIncludesDifferentRaces() {
		List<RaceRepository.RaceWithName> races = Arrays.asList(race(1), race(2), race(3));
		when(raceRepository.findRacesWithNames()).thenReturn(races);
		List<GeneratedNameApi> people = generate(6);

		assertEquals(6, people.size());
		assertEquals(3, people.stream().map(GeneratedNameApi::getRace).collect(Collectors.toSet()).size());
	}

	@Test
	void singleAvailableRaceStillFillsVisitorPool() {
		List<RaceRepository.RaceWithName> races = Collections.singletonList(race(1));
		when(raceRepository.findRacesWithNames()).thenReturn(races);

		assertEquals(6, generate(6).size());
	}

	private List<GeneratedNameApi> generate(int count) {
		AtomicInteger sequence = new AtomicInteger();
		when(nameGeneratorService.generate(any())).thenAnswer(invocation -> {
			NameGenerationRequest request = invocation.getArgument(0);
			List<GeneratedNameApi> names = new ArrayList<>();
			for (int i = 0; i < request.getCount(); i++) {
				names.add(new GeneratedNameApi("Guest " + sequence.incrementAndGet(),
					"Race " + request.getRaceId(), Sex.MALE));
			}
			return names;
		});
		return ReflectionTestUtils.invokeMethod(controller, "generateVisitorEntries", count, null);
	}

	private RaceRepository.RaceWithName race(int id) {
		RaceRepository.RaceWithName race = mock(RaceRepository.RaceWithName.class);
		when(race.getId()).thenReturn(id);
		when(race.getEnglishName()).thenReturn("race-" + id);
		return race;
	}
}
