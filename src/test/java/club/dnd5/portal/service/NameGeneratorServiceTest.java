package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.tools.name.GeneratedNameApi;
import club.dnd5.portal.dto.api.tools.name.NameGenerationFormat;
import club.dnd5.portal.dto.api.tools.name.NameGenerationRequest;
import club.dnd5.portal.dto.api.tools.name.NameGenerationType;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.races.RaceName;
import club.dnd5.portal.model.races.RaceNickname;
import club.dnd5.portal.model.races.RaceNickname.NicknameType;
import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.repository.datatable.RaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NameGeneratorServiceTest {
	private RaceRepository raceRepository;
	private NameGeneratorService service;
	private Race race;

	@BeforeEach
	void setUp() {
		raceRepository = mock(RaceRepository.class);
		service = new NameGeneratorService(raceRepository);
		race = createRace();
		when(raceRepository.findAll()).thenReturn(Collections.singletonList(race));
		when(raceRepository.findById(race.getId())).thenReturn(Optional.of(race));
	}

	@Test
	void shouldGenerateFamilyWithUniqueNamesAndSharedSurname() {
		NameGenerationRequest request = request(NameGenerationType.FAMILY, NameGenerationFormat.NAME_HOUSE, 3);

		List<GeneratedNameApi> result = service.generate(request);

		assertEquals(3, result.size());
		assertEquals(3, uniqueValues(result).size());
		assertEquals(1, result.stream()
			.map(item -> item.getValue().substring(item.getValue().lastIndexOf(' ') + 1))
			.collect(Collectors.toSet()).size());
	}

	@Test
	void shouldGenerateGroupWithSharedClan() {
		NameGenerationRequest request = request(NameGenerationType.CLAN, NameGenerationFormat.NICKNAME, 3);

		List<GeneratedNameApi> result = service.generate(request);

		Set<String> clans = result.stream()
			.map(item -> item.getValue().substring(item.getValue().indexOf("из клана ")))
			.collect(Collectors.toSet());
		assertEquals(1, clans.size());
		assertTrue(result.stream().allMatch(item -> item.getValue().contains("из клана")));
	}

	@Test
	void shouldGenerateGroupWithSharedHouse() {
		NameGenerationRequest request = request(NameGenerationType.HOUSE, NameGenerationFormat.NICKNAME, 3);

		List<GeneratedNameApi> result = service.generate(request);

		Set<String> houses = result.stream()
			.map(item -> item.getValue().substring(item.getValue().indexOf("из дома ")))
			.collect(Collectors.toSet());
		assertEquals(1, houses.size());
		assertTrue(result.stream().allMatch(item -> item.getValue().contains("из дома")));
	}

	@Test
	void shouldApplyRaceAndSexFilters() {
		NameGenerationRequest request = request(NameGenerationType.GROUP, NameGenerationFormat.NAME_SURNAME, 2);
		request.setRaceId(race.getId());
		request.setSexes(Collections.singleton(Sex.FEMALE));

		List<GeneratedNameApi> result = service.generate(request);

		assertEquals(2, result.size());
		assertTrue(result.stream().allMatch(item -> item.getSex() == Sex.FEMALE));
		assertTrue(result.stream().allMatch(item -> item.getRace().equals(race.getFullName())));
	}

	@Test
	void shouldGenerateAnyFormatWithoutRemovedSurnameAndHouseCombination() {
		NameGenerationRequest request = request(NameGenerationType.GROUP, NameGenerationFormat.ANY, 3);

		List<GeneratedNameApi> result = service.generate(request);

		assertEquals(3, result.size());
		assertEquals(3, uniqueValues(result).size());
		assertTrue(result.stream().noneMatch(item -> item.getValue().contains("Камнерез из дома")));
	}

	@Test
	void shouldGenerateOnlyUnisexNames() {
		NameGenerationRequest request = request(NameGenerationType.SINGLE, NameGenerationFormat.NAME_SURNAME, 1);
		request.setSexes(Collections.singleton(Sex.UNISEX));

		List<GeneratedNameApi> result = service.generate(request);

		assertEquals(1, result.size());
		assertEquals(Sex.UNISEX, result.get(0).getSex());
		assertTrue(result.get(0).getValue().startsWith("Рин "));
	}

	private NameGenerationRequest request(
		NameGenerationType type,
		NameGenerationFormat format,
		int count
	) {
		NameGenerationRequest request = new NameGenerationRequest();
		request.setType(type);
		request.setFormat(format);
		request.setCount(count);
		request.setSexes(new HashSet<>(Arrays.asList(Sex.MALE, Sex.FEMALE)));
		return request;
	}

	private Race createRace() {
		Race result = new Race();
		result.setId(42);
		result.setName("Дворф");
		result.setNames(Arrays.asList(
			new RaceName(1, "Брин", Sex.MALE, result),
			new RaceName(2, "Дорн", Sex.MALE, result),
			new RaceName(3, "Астрид", Sex.FEMALE, result),
			new RaceName(4, "Берта", Sex.FEMALE, result),
			new RaceName(5, "Хильда", Sex.FEMALE, result),
			new RaceName(6, "Рин", Sex.UNISEX, result)
		));
		result.setNicknames(Arrays.asList(
			new RaceNickname(1, "Камнерез", result, NicknameType.SURNAME),
			new RaceNickname(2, "Железный Молот", result, NicknameType.CLAN),
			new RaceNickname(3, "Северный", result, NicknameType.HOUSE),
			new RaceNickname(4, "Несокрушимый", result, NicknameType.NICKNAME)
		));
		return result;
	}

	private Set<String> uniqueValues(List<GeneratedNameApi> result) {
		return result.stream().map(GeneratedNameApi::getValue).collect(Collectors.toSet());
	}
}
