package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.item.ItemApi;
import club.dnd5.portal.dto.api.item.ItemDetailApi;
import club.dnd5.portal.dto.api.item.ItemRequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.EquipmentType;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.ItemRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.RandomUtils;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Снаряжение и прочее
 */
@Tag(name = "Снаряжение", description = "API снаряжение")
@RequiredArgsConstructor
@RestController
public class ItemApiController {
	private final ItemRepository itemRepository;

	@Operation( summary = "Получение краткого списка снаряжения")
	@PostMapping(value = "/api/v1/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ItemApi> getItem(@RequestBody ItemRequestApi request) {
		Specification<Equipment> specification = buildSpecification(request);

		if (request.getFilter() != null && request.getFilter().getRandom()) {
			Pageable pageable = PageAndSortUtil.getPageable(request);
			int sizeList = pageable.getPageSize();
			return RandomUtils.getRandomObjectListFromList(
				itemRepository.findAll(specification).stream()
					.map(ItemApi::new)
					.collect(Collectors.toList()), sizeList);
		}

		Pageable pageable = PageAndSortUtil.getPageable(request);
		return itemRepository.findAll(specification, pageable).toList().stream()
			.map(ItemApi::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Получение снаряжения по английскому имени")
	@PostMapping(value = "/api/v1/items/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemDetailApi getOption(@PathVariable String englishName) {
		Equipment item = itemRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		return new ItemDetailApi(item);
	}

	@Operation(summary = "Получение фильтров для снаряжения")
	@PostMapping("/api/v1/filters/items")
	public FilterApi getItemsFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = itemRepository.findBook(typeBook);
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

		List<FilterValueApi> sortedEquipmentTypes = Arrays.stream(EquipmentType.values())
			.sorted(Comparator.comparing(EquipmentType::getCyrilicName))
			.map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
			.collect(Collectors.toList());

		FilterApi equipmentTypeFilter = new FilterApi("Категория", "category");
		equipmentTypeFilter.setValues(sortedEquipmentTypes);

		otherFilters.add(equipmentTypeFilter);

		filters.setOther(otherFilters);
		return filters;
	}

	private Specification<Equipment> buildSpecification(ItemRequestApi request) {
		Specification<Equipment> specification = null;

		if (Optional.ofNullable(request)
			.map(RequestApi::getSearch)
			.map(SearchRequest::getValue)
			.map(value -> !value.isEmpty())
			.orElse(false)) {
			specification = SpecificationUtil.getSearch(request);
		}

		if (request.getFilter() != null) {
			specification = addFilterSpecifications(specification, request);
		}

		if (request.getOrders() != null && !request.getOrders().isEmpty()) {
			specification = addOrderSpecifications(specification, request);
		}

		return specification;
	}

	private Specification<Equipment> addFilterSpecifications(Specification<Equipment> specification, ItemRequestApi request) {
		if (!request.getFilter().getBooks().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<Book, Spell> join = root.join("book", JoinType.INNER);
				return join.get("source").in(request.getFilter().getBooks());
			});
		}

		if (!request.getFilter().getCategories().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				Join<EquipmentType, Equipment> types = root.join("types", JoinType.LEFT);
				query.distinct(true);
				return types.in(request.getFilter().getCategories()
					.stream()
					.map(EquipmentType::valueOf)
					.collect(Collectors.toList()));
			});
		}

		return specification;
	}

	private Specification<Equipment> addOrderSpecifications(Specification<Equipment> specification, ItemRequestApi request) {
		return SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
			List<Order> orders = request.getOrders().stream()
				.map(order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField())) : cb.desc(root.get(order.getField())))
				.collect(Collectors.toList());
			query.orderBy(orders);
			return cb.and();
		});
	}
}
