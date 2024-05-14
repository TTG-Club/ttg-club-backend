package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.item.ItemApi;
import club.dnd5.portal.dto.api.item.ItemRequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Treasure;
import club.dnd5.portal.model.items.TreasureType;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.TreasureRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.RandomUtils;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Сокровища и безделушки", description = "API для сокровищ и безделушек")
@RestController
public class TreasureApiController {
	private final TreasureRepository treasuryRepository;

	@Operation(summary = "Получение списка сокровищ и безделушек")
	@PostMapping(value = "/api/v1/treasures", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ItemApi> getItem(@RequestBody ItemRequestApi request) {
		Specification<Treasure> specification = null;
		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
			if (!request.getFilter().getCategories().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> root.get("type").in(
								request.getFilter().getCategories().stream().map(TreasureType::valueOf).collect(Collectors.toList()))
						);
			}
		}
		if (request.getOrders() !=null && !request.getOrders().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				List<Order> orders = request.getOrders().stream()
						.map(
							order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField())) : cb.desc(root.get(order.getField()))
						)
						.collect(Collectors.toList());
				query.orderBy(orders);
				return cb.and();
			});

			if (request.getFilter().getRandom()) {
				Pageable pageable = PageAndSortUtil.getPageable(request);
				int sizeList = pageable.getPageSize();
				return RandomUtils.getRandomObjectListFromList(treasuryRepository.findAll(specification)
					.stream()
					.map(ItemApi::new)
					.collect(Collectors.toList()), sizeList);
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return treasuryRepository.findAll(specification, pageable).toList()
			.stream()
			.map(ItemApi::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Фильры для сокровищ и безделушек")
	@PostMapping("/api/v1/filters/treasures")
	public FilterApi getWeaponsFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = treasuryRepository.findBook(typeBook);
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

		FilterApi damageTypeFilter = new FilterApi("Тип", "category");
		damageTypeFilter.setValues(
				Arrays.stream(TreasureType.values())
				 .map(value -> new FilterValueApi(value.getName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(damageTypeFilter);

		filters.setOther(otherFilters);
		return filters;
	}
}
