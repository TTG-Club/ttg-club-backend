package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.classes.FeatRequestApi;
import club.dnd5.portal.dto.api.classes.TraitApi;
import club.dnd5.portal.dto.api.classes.TraitDetailApi;
import club.dnd5.portal.dto.api.spell.SpellApi;
import club.dnd5.portal.dto.api.spell.SpellRequesApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.trait.Trait;
import club.dnd5.portal.repository.datatable.TraitRepository;
import club.dnd5.portal.util.SortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Trait", description = "The Trait API")
@RestController
public class TraitApiController {
	@Autowired
	private TraitRepository traitRepository;

	@Operation(summary = "Gets all traits")
	@PostMapping(value = "/api/v1/traits", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TraitApi> getTraits(@RequestBody FeatRequestApi request) {
		Specification<Trait> specification = null;
		Optional<FeatRequestApi> spellRequest = Optional.ofNullable(request);
		if (!spellRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Trait> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
			if (!request.getFilter().getAbilities().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<AbilityType, Trait> join = root.join("abilities", JoinType.LEFT);
					query.distinct(true);
					return cb.and(join.in(request.getFilter().getAbilities().stream().map(AbilityType::valueOf).collect(Collectors.toList())));
				});
			}
			if (request.getFilter().getRequirements() != null && !request.getFilter().getRequirements().isEmpty()) {
				if (request.getFilter().getRequirements().contains("level")) {
					if (request.getFilter().getRequirements().contains("no")) {
						specification = SpecificationUtil.getAndSpecification(
							specification,
							(root, query, cb) -> cb.like(root.get("requirement"), "% уровень")
						);
					} else {
						specification = SpecificationUtil.getAndSpecification(
							specification,
							(root, query, cb) -> cb.like(root.get("requirement"), "% уровень%")
						);
					}
				} else {
					if (request.getFilter().getRequirements().contains("yes") && !request.getFilter().getRequirements().contains("no")) {
						specification = SpecificationUtil.getAndSpecification(
							specification,
							(root, query, cb) -> cb.and(cb.notEqual(root.get("requirement"), "Нет"), cb.isNotNull(root.get("requirement")))
						);
					} else if (request.getFilter().getRequirements().contains("no") && !request.getFilter().getRequirements().contains("yes")) {
						specification = SpecificationUtil.getAndSpecification(
							specification,
							(root, query, cb) -> cb.or(cb.equal(root.get("requirement"), "Нет"), cb.isNull(root.get("requirement")))
						);
					}
				}
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
		Collection<Trait> traits;
		if (pageable == null) {
			traits = traitRepository.findAll(specification, sort);
		} else {
			traits = traitRepository.findAll(specification, pageable).toList();
		}
		return traits
			.stream()
			.map(TraitApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/traits/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TraitDetailApi> getTrait(@PathVariable String englishName) {
		Trait trait = traitRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new TraitDetailApi(trait));
	}

	@PostMapping("/api/v1/filters/traits")
	public FilterApi getTraitFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			Collection<Book> books = traitRepository.findBook(typeBook);
			if (!books.isEmpty()) {
				FilterApi filter = new FilterApi(typeBook.getName(), typeBook.name());
				filter.setValues(books.stream()
					.map(book -> new FilterValueApi(book.getSource(), book.getSource(), Boolean.TRUE, book.getName()))
					.collect(Collectors.toList()));
				sources.add(filter);
			}
		}
		filters.setSources(sources);

		List<FilterApi> otherFilters = new ArrayList<>();
		FilterApi abilitiesFilter = new FilterApi("Характеристики", "abilities");
		abilitiesFilter.setValues(
			AbilityType.getBaseAbility().stream()
				.map(ability -> new FilterValueApi(ability.getCyrilicName(), ability.name()))
				.collect(Collectors.toList()));
		otherFilters.add(abilitiesFilter);

		FilterApi requirementFilter = new FilterApi("Дополнительные требования", "requirements");

		List<FilterValueApi> values = new ArrayList<>(3);

		values.add(new FilterValueApi("да", "yes"));
		values.add(new FilterValueApi("нет", "no"));
		values.add(new FilterValueApi("уровень", "level"));

		requirementFilter.setValues(values);
		otherFilters.add(requirementFilter);

		filters.setOther(otherFilters);
		return filters;
	}
}
