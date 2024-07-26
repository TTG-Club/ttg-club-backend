package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.tags.GeneratorNameApi;
import club.dnd5.portal.model.CreatureType;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.races.RaceNickname;
import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.repository.datatable.NameRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NameGeneratorService {
	private final NameRepository nameRepository;
	private final RaceRepository raceRepository;

	public List<String> generateNames(GeneratorNameApi generatorNameApi) {
		return new ArrayList<>();
	}

	//in Race
	//getAllNames
	//getNicknamesGroup
	//getAllNicknames
	public List<String> getListNames(GeneratorNameApi generatorNameApi) {
		List<String> allNames = new ArrayList<>();
		List<Race> races = raceRepository.findRaceByType(CreatureType.parse(generatorNameApi.getCreatureTag()));
		for (Race race : races) {
			Map<Sex, Set<String>> namesBySex = race.getAllNames();
			for (Map.Entry entry : namesBySex.entrySet()) {
				if (generatorNameApi.getGenderTags().contains(entry.getKey())) {
					//todo во-первых подумать над этим, во-вторых подправить иф, он точно не работает
					allNames.addAll((Collection<? extends String>) entry.getValue());
				}
			}
			List<String> raceNicknames = race.getAllNicknames().stream()
				.map(RaceNickname::getName)
				.collect(Collectors.toList());

			allNames.addAll(raceNicknames);
		}
		return allNames;
	}
}
