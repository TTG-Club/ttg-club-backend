package club.dnd5.portal.controller.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import club.dnd5.portal.repository.datatable.BackgroundDatatableRepository;
import club.dnd5.portal.repository.datatable.OptionDatatableRepository;
import club.dnd5.portal.repository.datatable.SpellDatatableRepository;
import club.dnd5.portal.repository.datatable.TraitDatatableRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.SearchApi;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.classes.RaceRepository;

@Tag(name = "Full search", description = "The search API")
@RestController
@RequestMapping("/api/v1/search")
public class FullSearchController {
	@Autowired
	ClassRepository classRepository;

	@Autowired
	RaceRepository raceRepository;
	@Autowired
	TraitDatatableRepository traitRepository;
	@Autowired
	OptionDatatableRepository optionRepository;
	@Autowired
	BackgroundDatatableRepository backgroundRepository;
	@Autowired
	SpellDatatableRepository spellRepository;

	@PostMapping
	public Collection<SearchApi> search(String search){
		Collection<SearchApi> result = new ArrayList<>();
		result.addAll(raceRepository.findByEnglishNameContainsOrNameContainsOrAltNameContains(search, search, search)
				.stream()
				.map(SearchApi::new)
				.collect(Collectors.toList()));
		result.addAll(traitRepository.findByEnglishNameContainsOrNameContainsOrAltNameContains(search, search, search)
			.stream()
			.map(SearchApi::new)
			.collect(Collectors.toList()));
		result.addAll(optionRepository.findByEnglishNameContainsOrNameContainsOrAltNameContains(search, search, search)
			.stream()
			.map(SearchApi::new)
			.collect(Collectors.toList()));
		result.addAll(backgroundRepository.findByEnglishNameContainsOrNameContainsOrAltNameContains(search, search, search)
			.stream()
			.map(SearchApi::new)
			.collect(Collectors.toList()));
		result.addAll(spellRepository.findByEnglishNameContainsOrNameContainsOrAltNameContains(search, search, search)
			.stream()
			.map(SearchApi::new)
			.collect(Collectors.toList()));
		return result;
	}
}
