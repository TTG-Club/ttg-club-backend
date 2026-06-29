package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.tools.name.GeneratedNameApi;
import club.dnd5.portal.dto.api.tools.name.NameGenerationComponent;
import club.dnd5.portal.dto.api.tools.name.NameGenerationFormat;
import club.dnd5.portal.dto.api.tools.name.NameGenerationRequest;
import club.dnd5.portal.dto.api.tools.name.NameGenerationType;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.races.RaceNickname;
import club.dnd5.portal.model.races.RaceNickname.NicknameType;
import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.repository.datatable.RaceRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NameGeneratorService {
	private static final List<NameGenerationFormat> RANDOM_FORMATS = Arrays.asList(
		NameGenerationFormat.NAME_SURNAME,
		NameGenerationFormat.NAME_CLAN,
		NameGenerationFormat.NAME_HOUSE,
		NameGenerationFormat.NAME_FROM,
		NameGenerationFormat.NAME_NICKNAME,
		NameGenerationFormat.NICKNAME,
		NameGenerationFormat.NAME_CLAN_AFFILIATION
	);

	private final RaceRepository raceRepository;
	private final Random random = new Random();

	public List<GeneratedNameApi> generate(NameGenerationRequest request) {
		validateSexes(request.getSexes());

		int count = request.getType() == NameGenerationType.SINGLE ? 1 : request.getCount();
		List<RaceData> races = loadRaces(request.getRaceId(), request.getSexes());

		if (request.getType() == NameGenerationType.FAMILY) {
			return generateShared(races, count, NicknameType.SURNAME, null);
		}
		if (request.getType() == NameGenerationType.CLAN) {
			return generateShared(races, count, NicknameType.CLAN, "из клана");
		}
		if (request.getType() == NameGenerationType.HOUSE) {
			return generateShared(races, count, NicknameType.HOUSE, "из дома");
		}
		if (request.getComponents() != null) {
			return generateByComponents(races, count, request.getComponents());
		}
		if (request.getFormat() == null) {
			throw badRequest("Выберите хотя бы один компонент имени");
		}

		return generateRegular(races, count, request.getFormat());
	}

	private List<GeneratedNameApi> generateByComponents(
		List<RaceData> races,
		int count,
		Set<NameGenerationComponent> components
	) {
		if (components.isEmpty()) {
			throw badRequest("Выберите хотя бы один компонент имени");
		}

		List<NameGenerationFormat> formats = new ArrayList<>();
		if (components.contains(NameGenerationComponent.NAME)) {
			formats.add(NameGenerationFormat.NAME);
		}
		if (components.contains(NameGenerationComponent.SURNAME)) {
			formats.add(NameGenerationFormat.NAME_SURNAME);
		}
		if (components.contains(NameGenerationComponent.NICKNAME)) {
			formats.add(NameGenerationFormat.NICKNAME);
			if (components.contains(NameGenerationComponent.NAME)) {
				formats.add(NameGenerationFormat.NAME_NICKNAME);
			}
		}
		if (components.contains(NameGenerationComponent.CLAN)) {
			formats.add(NameGenerationFormat.NAME_CLAN_AFFILIATION);
		}
		if (components.contains(NameGenerationComponent.HOUSE)) {
			formats.add(NameGenerationFormat.NAME_HOUSE);
		}
		if (components.contains(NameGenerationComponent.FROM)) {
			formats.add(NameGenerationFormat.NAME_FROM);
		}

		return generateAny(races, count, formats);
	}

	private List<GeneratedNameApi> generateShared(
		List<RaceData> races,
		int count,
		NicknameType sharedType,
		String affiliation
	) {
		List<RaceData> eligibleRaces = races.stream()
			.filter(race -> race.names.size() >= count && !race.parts.get(sharedType).isEmpty())
			.collect(Collectors.toList());

		if (eligibleRaces.isEmpty()) {
			throw badRequest("Для выбранных настроек недостаточно уникальных имён");
		}

		RaceData race = randomItem(eligibleRaces);
		String shared = randomItem(race.parts.get(sharedType));
		List<PersonName> names = shuffled(race.names).subList(0, count);

		return names.stream()
			.map(name -> new GeneratedNameApi(
				affiliation == null
					? name.value + " " + shared
					: name.value + " " + affiliation + " " + shared,
				race.name,
				name.sex
			))
			.collect(Collectors.toList());
	}

	private List<GeneratedNameApi> generateRegular(
		List<RaceData> races,
		int count,
		NameGenerationFormat format
	) {
		if (format == NameGenerationFormat.ANY) {
			return generateAny(races, count, RANDOM_FORMATS);
		}

		List<RaceData> eligibleRaces = races.stream()
			.filter(race -> supports(race, format))
			.collect(Collectors.toList());
		List<Candidate> candidates = new ArrayList<>();

		for (RaceData race : eligibleRaces) {
			if (format == NameGenerationFormat.NICKNAME) {
				for (String nickname : race.parts.get(NicknameType.NICKNAME)) {
					candidates.add(new Candidate(race, null, nickname));
				}
			} else {
				for (PersonName name : race.names) {
					candidates.add(new Candidate(race, name, null));
				}
			}
		}

		Collections.shuffle(candidates, random);
		List<GeneratedNameApi> result = new ArrayList<>();
		Set<String> usedBaseNames = new HashSet<>();

		for (Candidate candidate : candidates) {
			String baseName = candidate.person == null ? candidate.nickname : candidate.person.value;
			if (!usedBaseNames.add(baseName)) {
				continue;
			}

			result.add(format(candidate, format));
			if (result.size() == count) {
				return result;
			}
		}

		throw badRequest("Для выбранных настроек недостаточно уникальных имён");
	}

	private List<GeneratedNameApi> generateAny(
		List<RaceData> races,
		int count,
		List<NameGenerationFormat> formats
	) {
		List<FormattedCandidate> candidates = new ArrayList<>();

		for (RaceData race : races) {
			for (NameGenerationFormat format : formats) {
				if (!supports(race, format)) {
					continue;
				}

				if (format == NameGenerationFormat.NICKNAME) {
					for (String nickname : race.parts.get(NicknameType.NICKNAME)) {
						candidates.add(new FormattedCandidate(
							new Candidate(race, null, nickname),
							format
						));
					}
				} else {
					for (PersonName name : race.names) {
						candidates.add(new FormattedCandidate(
							new Candidate(race, name, null),
							format
						));
					}
				}
			}
		}

		Collections.shuffle(candidates, random);
		List<GeneratedNameApi> result = new ArrayList<>();
		Set<String> usedBaseNames = new HashSet<>();

		for (FormattedCandidate formatted : candidates) {
			Candidate candidate = formatted.candidate;
			String baseName = candidate.person == null ? candidate.nickname : candidate.person.value;
			if (!usedBaseNames.add(baseName)) {
				continue;
			}

			result.add(format(candidate, formatted.format));
			if (result.size() == count) {
				return result;
			}
		}

		throw badRequest("Для выбранных настроек недостаточно уникальных имён");
	}

	private GeneratedNameApi format(Candidate candidate, NameGenerationFormat format) {
		RaceData race = candidate.race;
		String name = candidate.person == null ? null : candidate.person.value;
		String value;

		switch (format) {
			case NAME:
				value = name;
				break;
			case NAME_SURNAME:
				value = name + " " + randomPart(race, NicknameType.SURNAME);
				break;
			case NAME_SURNAME_HOUSE:
				value = name + " " + randomPart(race, NicknameType.SURNAME)
					+ " из дома " + randomPart(race, NicknameType.HOUSE);
				break;
			case NAME_CLAN:
				value = name + " " + randomPart(race, NicknameType.CLAN);
				break;
			case NAME_HOUSE:
				value = name + " из дома " + randomPart(race, NicknameType.HOUSE);
				break;
			case NAME_FROM:
				value = name + " из " + randomPart(race, NicknameType.FROM);
				break;
			case NAME_NICKNAME:
				value = name + " по прозвищу «" + randomPart(race, NicknameType.NICKNAME) + "»";
				break;
			case NICKNAME:
				value = candidate.nickname;
				break;
			case NAME_CLAN_AFFILIATION:
				value = name + " из клана " + randomPart(race, NicknameType.CLAN);
				break;
			default:
				throw badRequest("Неизвестный формат имени");
		}

		return new GeneratedNameApi(
			value,
			race.name,
			candidate.person == null ? null : candidate.person.sex
		);
	}

	private boolean supports(RaceData race, NameGenerationFormat format) {
		if (format == NameGenerationFormat.NICKNAME) {
			return !race.parts.get(NicknameType.NICKNAME).isEmpty();
		}
		if (race.names.isEmpty()) {
			return false;
		}

		switch (format) {
			case NAME:
				return true;
			case NAME_SURNAME:
				return !race.parts.get(NicknameType.SURNAME).isEmpty();
			case NAME_SURNAME_HOUSE:
				return !race.parts.get(NicknameType.SURNAME).isEmpty()
					&& !race.parts.get(NicknameType.HOUSE).isEmpty();
			case NAME_CLAN:
			case NAME_CLAN_AFFILIATION:
				return !race.parts.get(NicknameType.CLAN).isEmpty();
			case NAME_HOUSE:
				return !race.parts.get(NicknameType.HOUSE).isEmpty();
			case NAME_FROM:
				return !race.parts.get(NicknameType.FROM).isEmpty();
			case NAME_NICKNAME:
				return !race.parts.get(NicknameType.NICKNAME).isEmpty();
			default:
				return false;
		}
	}

	private List<RaceData> loadRaces(Integer raceId, Set<Sex> sexes) {
		List<Race> races;
		if (raceId == null) {
			races = raceRepository.findAll();
		} else {
			Race race = raceRepository.findById(raceId)
				.orElseThrow(() -> badRequest("Выбранная раса не найдена"));
			races = Collections.singletonList(race);
		}

		return races.stream()
			.map(race -> toRaceData(race, sexes))
			.collect(Collectors.toList());
	}

	private RaceData toRaceData(Race race, Set<Sex> sexes) {
		List<PersonName> filteredNames = race.getAllNames().entrySet().stream()
			.filter(entry -> sexes.contains(entry.getKey()))
			.flatMap(entry -> entry.getValue().stream()
				.map(name -> new PersonName(name, entry.getKey())))
			.distinct()
			.collect(Collectors.toList());

		Map<NicknameType, List<String>> parts = new EnumMap<>(NicknameType.class);
		for (NicknameType type : NicknameType.values()) {
			parts.put(type, new ArrayList<>());
		}
		for (RaceNickname nickname : race.getAllNicknames()) {
			parts.get(nickname.getType()).add(nickname.getName());
		}

		return new RaceData(race.getFullName(), filteredNames, parts);
	}

	private void validateSexes(Set<Sex> sexes) {
		if (sexes == null || sexes.isEmpty()
			|| sexes.stream().anyMatch(sex -> sex == Sex.CHILD)) {
			throw badRequest("Выберите мужские, женские и/или универсальные имена");
		}
	}

	private String randomPart(RaceData race, NicknameType type) {
		return randomItem(race.parts.get(type));
	}

	private <T> T randomItem(List<T> items) {
		return items.get(random.nextInt(items.size()));
	}

	private <T> List<T> shuffled(List<T> items) {
		List<T> result = new ArrayList<>(items);
		Collections.shuffle(result, random);
		return result;
	}

	private ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	@AllArgsConstructor
	private static class RaceData {
		private final String name;
		private final List<PersonName> names;
		private final Map<NicknameType, List<String>> parts;
	}

	@AllArgsConstructor
	private static class PersonName {
		private final String value;
		private final Sex sex;

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}
			if (!(object instanceof PersonName)) {
				return false;
			}
			PersonName other = (PersonName) object;
			return value.equals(other.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}
	}

	@AllArgsConstructor
	private static class Candidate {
		private final RaceData race;
		private final PersonName person;
		private final String nickname;
	}

	@AllArgsConstructor
	private static class FormattedCandidate {
		private final Candidate candidate;
		private final NameGenerationFormat format;
	}
}
