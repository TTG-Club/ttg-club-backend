package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.classes.RaceFilter;
import club.dnd5.portal.dto.api.classes.RaceRequestApi;
import club.dnd5.portal.dto.api.races.RaceApi;
import club.dnd5.portal.dto.api.races.RaceDetailApi;
import club.dnd5.portal.dto.api.spell.SpellApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.util.SortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Race", description = "The Race API")
@RestController
public class RacesApiController {
	@Autowired
	private RaceRepository raceRepository;

	@Autowired
	private ImageRepository imageRepository;

	@PostMapping(value = "/api/v1/races", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RaceApi> getRaces(@RequestBody RaceRequestApi request) {
		Specification<Race> specification;

		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		} else {
			specification = Specification.where((root, query, cb) -> cb.isNull(root.get("parent")));
		}

		if (request.getFilter() != null && request.getFilter().getBooks() != null && !request.getFilter().getBooks().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Book, Race> join = root.join("book", JoinType.INNER);
				return join.get("source").in(request.getFilter().getBooks());
			});
		}

		if (request.getFilter() != null) {
			for (String ability : request.getFilter().getAbilities()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<AbilityType, Race> join = root.join("bonuses", JoinType.LEFT);
					query.distinct(true);
					return cb.equal(join.get("ability"), AbilityType.valueOf(ability));
				});
			}
			if (request.getFilter().getSkills().contains("darkvision")) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> cb.isNotNull(root.get("darkvision")));
			}
			if (request.getFilter().getSkills().contains("fly")) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> cb.isNotNull(root.get("fly")));
			}
			if (request.getFilter().getSkills().contains("swim")) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> cb.isNotNull(root.get("swim")));
			}
			if (request.getFilter().getSkills().contains("climb")) {
				specification = SpecificationUtil.getAndSpecification(
						specification, (root, query, cb) -> cb.isNotNull(root.get("climb")));
			}
		}
		Sort sort = Sort.unsorted();
		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = SortUtil.getSort(request);
		}
		Pageable pageable = null;
		if (request.getPage() != null && request.getLimit() != null) {
			pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);
		}
		Collection<Race> races;
		if (pageable == null) {
			races = raceRepository.findAll(specification, sort);
		} else {
			races = raceRepository.findAll(specification, pageable).toList();
		}
		return races
			.stream()
			.map(race -> new RaceApi(race, Optional.of(request)
				.map(RaceRequestApi::getFilter)
				.map(RaceFilter::getBooks)
				.orElse(Collections.emptySet()))
			)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/races/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RaceDetailApi> getRace(
		@PathVariable String englishName,
		@RequestBody RaceRequestApi request) {
		Race race = raceRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		RaceDetailApi raceApi = new RaceDetailApi(race, Optional.of(request)
			.map(RaceRequestApi::getFilter)
			.map(RaceFilter::getBooks)
			.orElse(Collections.emptySet()
		));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, race.getId());
		if (!images.isEmpty()) {
			raceApi.setImages(images);
		}
		return ResponseEntity.ok(raceApi);
	}

	@PostMapping(value = "/api/v1/races/{englishRaceName}/{englishSubraceName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public RaceDetailApi getSubrace(
		@PathVariable String englishRaceName,
		@PathVariable String englishSubraceName,
		@RequestBody RaceRequestApi request) {
		Optional<Race> race = raceRepository.findBySubrace(englishRaceName.replace('_', ' '), englishSubraceName.replace('_', ' '));
		RaceDetailApi raceApi = new RaceDetailApi(race.get(), Optional.of(request).map(RaceRequestApi::getFilter).map(RaceFilter::getBooks).orElse(Collections.emptySet()));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, race.get().getId());
		if (!images.isEmpty()) {
			raceApi.setImages(images);
		}
		return raceApi;
	}

	@PostMapping("/api/v1/filters/races")
	public FilterApi getRacesFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = raceRepository.findBook(typeBook);
			if (!books.isEmpty()) {
				FilterApi filter = new FilterApi(typeBook.getName(), typeBook.name());
				filter.setValues(books.stream()
						.map(book -> new FilterValueApi(book.getSource(), book.getSource(),	Boolean.TRUE, book.getName()))
						.collect(Collectors.toList()));
				sources.add(filter);
			}
		}
		filters.setSources(sources);

		List<FilterApi> otherFilters = new ArrayList<>();
		FilterApi levelFilter = new FilterApi("Увеличение характеристик", "abilities");
		levelFilter.setValues(
				EnumSet.of(AbilityType.STRENGTH, AbilityType.DEXTERITY,AbilityType.CONSTITUTION,AbilityType.INTELLIGENCE,AbilityType.WISDOM,AbilityType.CHARISMA)
				.stream()
				.map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				.collect(Collectors.toList()));
		otherFilters.add(levelFilter);

		FilterApi skillFilter = new FilterApi("Способности", "skills");
		List<FilterValueApi> values = new ArrayList<>();
		values.add(new FilterValueApi("тёмное зрение", "darkvision"));
		values.add(new FilterValueApi("полет", "fly"));
		values.add(new FilterValueApi("плавание", "swim"));
		values.add(new FilterValueApi("лазание", "climb"));

		skillFilter.setValues(values);
		otherFilters.add(skillFilter);
		filters.setOther(otherFilters);
		return filters;
	}
}
