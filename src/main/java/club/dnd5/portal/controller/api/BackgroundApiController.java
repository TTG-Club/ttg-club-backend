package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.classes.BackgroundApi;
import club.dnd5.portal.dto.api.classes.BackgroundDetailApi;
import club.dnd5.portal.dto.api.classes.FeatRequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.BackgroundRepository;
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
import javax.persistence.criteria.Order;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Предыстории", description = "API по предысториям")
@RestController
public class BackgroundApiController {
	private final BackgroundRepository backgroundRepository;

	@Operation(summary = "Получения краткого списка предисторий")
	@PostMapping(value = "/api/v1/backgrounds", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<BackgroundApi> getBackgrounds(@RequestBody FeatRequestApi request) {
		Specification<Background> specification = null;
		Optional<FeatRequestApi> backgroundRequest = Optional.ofNullable(request);
		if (!backgroundRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
			if (!request.getFilter().getSkills().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<AbilityType, Background> abilityType = root.join("skills", JoinType.LEFT);
					query.distinct(true);
					return cb.and(abilityType.in(
							request.getFilter().getSkills().stream().map(SkillType::valueOf).collect(Collectors.toList())));
				});
			}
		}
		if (request.getOrders() != null && !request.getOrders().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				List<Order> orders = request.getOrders().stream()
						.map(
							order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField())) : cb.desc(root.get(order.getField()))
						)
						.collect(Collectors.toList());
				query.orderBy(orders);
				return cb.and();
			});
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return backgroundRepository.findAll(specification, pageable).toList()
			.stream()
			.map(BackgroundApi::new)
			.collect(Collectors.toList());

	}

	@PostMapping(value = "/api/v1/backgrounds/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BackgroundDetailApi> getBackground(@PathVariable String englishName) {
		Background background = backgroundRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new BackgroundDetailApi(background));
	}

	@PostMapping("/api/v1/filters/backgrounds")
	public FilterApi getBackgroundFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = backgroundRepository.findBook(typeBook);
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
		FilterApi schoolSpellFilter = new FilterApi("Навыки", "skills");
		schoolSpellFilter.setValues(
				Arrays.stream(SkillType.values())
				 .sorted(Comparator.comparing(SkillType::getCyrilicName))
				 .map(ability -> new FilterValueApi(ability.getCyrilicName(), ability.name()))
				 .collect(Collectors.toList()));

		otherFilters.add(schoolSpellFilter);
		filters.setOther(otherFilters);
		return filters;
	}
}
