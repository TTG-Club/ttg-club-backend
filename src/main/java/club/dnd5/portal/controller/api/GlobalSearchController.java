package club.dnd5.portal.controller.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import club.dnd5.portal.repository.datatable.TraitDatatableRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.SearchApi;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.classes.RaceRepository;
import io.swagger.v3.oas.annotations.Hidden;

@Tag(name = "Global search", description = "The search API")
@RestController
@RequestMapping("/api/v1/search")
public class GlobalSearchController {
	@Autowired
	ClassRepository classRepository;

	@Autowired
	RaceRepository raceRepository;
	@Autowired
	TraitDatatableRepository traitRepository;

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
		return result;
	}
}
