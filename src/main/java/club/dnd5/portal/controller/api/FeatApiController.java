package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.classes.FeatRequestApi;
import club.dnd5.portal.dto.api.classes.FeatApi;
import club.dnd5.portal.dto.api.classes.FeatDetailApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.trait.Trait;
import club.dnd5.portal.repository.datatable.FeatRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Черты", description = "API по чертам")
@RestController
public class FeatApiController {
	private final FeatRepository featRepository;

	@Operation(summary = "Получение краткого списка черт")
	@PostMapping(value = {"/api/v1/traits","/api/v1/feats"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<FeatApi> getTraits(@RequestBody FeatRequestApi request) {
		Specification<Trait> specification = null;
		Optional<FeatRequestApi> spellRequest = Optional.ofNullable(request);
		if (!spellRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (spellRequest.map(FeatRequestApi::getFilter).isPresent()) {
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
							(root, query, cb) -> cb.and(cb.notEqual(root.get("requirement"), "Нет"))
						);
					} else if (request.getFilter().getRequirements().contains("no") && !request.getFilter().getRequirements().contains("yes")) {
						specification = SpecificationUtil.getAndSpecification(
							specification,
							(root, query, cb) -> cb.or(cb.equal(root.get("requirement"), "Нет"))
						);
					}
				}
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return featRepository.findAll(specification, pageable).toList()
			.stream()
			.map(FeatApi::new)
			.collect(Collectors.toList());

	}

	@Operation(summary = "Получение черты по английскому названию")
	@PostMapping(value = {"/api/v1/traits/{englishName}", "/api/v1/feats/{englishName}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<FeatDetailApi> getTrait(@PathVariable String englishName) {
		Trait trait = featRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new FeatDetailApi(trait));
	}

	@Operation(summary = "Получение фильтров для черт")
	@PostMapping({"/api/v1/filters/traits", "/api/v1/filters/feats"})
	public FilterApi getTraitFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			Collection<Book> books = featRepository.findBook(typeBook);
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
