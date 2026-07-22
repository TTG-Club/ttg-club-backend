package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.dto.api.item.ItemApi;
import club.dnd5.portal.dto.api.item.ItemDetailApi;
import club.dnd5.portal.dto.api.item.ItemRequestApi;
import club.dnd5.portal.dto.api.item.ItemSaveApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Currency;
import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.EquipmentType;
import club.dnd5.portal.repository.datatable.ItemRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Снаряжение и прочее
 */
@Tag(name = "Снаряжение", description = "API снаряжение")
@RequiredArgsConstructor
@RestController
public class ItemApiController {
	private static final String ENTITY_TYPE = "ITEM";

	private final ItemRepository itemRepository;
	private final BookResolver bookResolver;
	private final AuditService auditService;

	@Operation( summary = "Получение краткого списка снаряжения")
	@PostMapping(value = "/api/v1/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ItemApi> getItem(@RequestBody ItemRequestApi request) {
		Specification<Equipment> specification = null;
		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue)
				.orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Equipment> join = root.join("book", JoinType.INNER);
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
		return itemRepository.findAll(specification, pageable).toList()
			.stream()
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

	@Operation(summary = "Создание снаряжения в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/items", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemDetailApi createItem(@Valid @RequestBody ItemSaveApi request) {
		if (itemRepository.findByEnglishName(request.getEnglishName()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item with the same englishName already exists");
		}
		Equipment item = new Equipment();
		item.setBook(bookResolver.getCustomBook());
		applyItemRequest(item, request);
		Equipment saved = itemRepository.saveAndFlush(item);
		auditService.record(ENTITY_TYPE, saved.getId(), RevisionOperation.CREATE, request);
		return new ItemDetailApi(saved);
	}

	@Operation(summary = "Обновление снаряжения в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/items/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemDetailApi updateItem(@PathVariable Integer id, @Valid @RequestBody ItemSaveApi request) {
		Equipment item = itemRepository.findById(id).orElseThrow(PageNotFoundException::new);
		itemRepository.findByEnglishName(request.getEnglishName())
			.filter(existing -> !existing.getId().equals(id))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item with the same englishName already exists");
			});
		applyItemRequest(item, request);
		Equipment saved = itemRepository.saveAndFlush(item);
		auditService.record(ENTITY_TYPE, saved.getId(), RevisionOperation.UPDATE, request);
		return new ItemDetailApi(saved);
	}

	@Operation(summary = "История изменений снаряжения")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/items/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getItemRevisions(@PathVariable Integer id) {
		itemRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE, id);
	}

	@Operation(summary = "Состояние снаряжения на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/items/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemSaveApi getItemRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		itemRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE, id, revision, ItemSaveApi.class);
	}

	@Operation(summary = "Восстановление снаряжения из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/items/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemDetailApi restoreItemRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		ItemSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE, id, revision, ItemSaveApi.class);
		return updateItem(id, snapshot);
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

	private void applyItemRequest(Equipment item, ItemSaveApi request) {
		item.setName(request.getName().trim());
		item.setEnglishName(request.getEnglishName().trim());
		item.setAltName(trimToNull(request.getAltName()));
		item.setCost(request.getCost());
		// getTextCost() падает без валюты, поэтому у цены всегда есть единица измерения
		item.setCurrency(request.getCurrency() == null ? Currency.MM : request.getCurrency());
		item.setWeight(request.getWeight());
		item.setDescription(trimToNull(request.getDescription()));
		replaceTypes(item, request.getCategories());
		bookResolver.find(request.getSource()).ifPresent(item::setBook);
	}

	/** Мутирует существующую коллекцию, иначе Hibernate дублирует строки в equipments_types. */
	private void replaceTypes(Equipment item, List<EquipmentType> categories) {
		Set<EquipmentType> types = item.getTypes();
		if (types == null) {
			types = new LinkedHashSet<>();
			item.setTypes(types);
		}
		types.clear();
		if (categories != null) {
			types.addAll(categories);
		}
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
