package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.item.MagicItemApi;
import club.dnd5.portal.dto.api.item.MagicItemDetailApi;
import club.dnd5.portal.dto.api.item.MagicItemRequesApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.fvtt.export.FCreature;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.items.MagicItem;
import club.dnd5.portal.model.items.MagicThingType;
import club.dnd5.portal.model.items.Rarity;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.MagicItemRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Magic Item", description = "The Magic Item API")
@RestController
public class MagicItemApiController {
	@Autowired
	private MagicItemRepository magicItemRepository;
	@Autowired
	private ImageRepository imageRepository;

	@PostMapping(value = "/api/v1/items/magic", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<MagicItemApi> getItems(@RequestBody MagicItemRequesApi request) {
		Specification<MagicItem> specification = null;
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
			if (!request.getFilter().getRarity().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> root.get("rarity").in(request.getFilter().getRarity().stream().map(Rarity::valueOf).collect(Collectors.toList())));
			}
			if (!request.getFilter().getType().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> root.get("type").in(request.getFilter().getType()
						.stream()
						.map(MagicThingType::valueOf)
						.collect(Collectors.toList())));
			}
			if (!request.getFilter().getCustomization().isEmpty()) {
				if (request.getFilter().getCustomization().contains("1")) {
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.equal(root.get("customization"), 1));
				}
				if (request.getFilter().getCustomization().contains("2")) {
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.equal(root.get("customization"), 0));
				}
			}
			if (!request.getFilter().getConsumable().isEmpty()) {
				if (request.getFilter().getConsumable().contains("1")) {
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.equal(root.get("consumed"), 1));
				}
				if (request.getFilter().getConsumable().contains("2")) {
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.equal(root.get("consumed"), 0));
				}
			}
			if (!request.getFilter().getCharge().isEmpty()) {
				if (request.getFilter().getCharge().contains("1")) {
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.isNotNull(root.get("charge")));
				}
				if (request.getFilter().getCharge().contains("2")) {
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.isNull(root.get("charge")));
				}
			}
		}
		if (request.getOrders() != null && !request.getOrders().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				List<Order> orders = request.getOrders().stream()
						.map(order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField()))
								: cb.desc(root.get(order.getField())))
						.collect(Collectors.toList());
				query.orderBy(orders);
				return cb.and();
			});
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return magicItemRepository.findAll(specification, pageable).toList()
			.stream()
			.map(MagicItemApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/items/magic/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MagicItemDetailApi getItem(@PathVariable String englishName) {
		MagicItem item = magicItemRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		MagicItemDetailApi itemApi = new MagicItemDetailApi(item);
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.MAGIC_ITEM, item.getId());
		if (!images.isEmpty()) {
			itemApi.setImages(images);
		}
		return itemApi;
	}

	@PostMapping("/api/v1/filters/items/magic")
	public FilterApi getMagicItemsFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = magicItemRepository.findBook(typeBook);
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

		FilterApi rarityFilter = new FilterApi("Редкость", "rarity");
		rarityFilter.setValues(
				Arrays.stream(Rarity.values())
				 .map(value -> new FilterValueApi(value.getNames()[1], value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(rarityFilter);

		FilterApi typeFilter = new FilterApi("Тип предмета", "type");
		typeFilter.setValues(
				Arrays.stream(MagicThingType.values())
				 .map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(typeFilter);

		FilterApi attumentFilter = new FilterApi("Настройка", "customization");
		List<FilterValueApi> values = new ArrayList<>(2);
		values.add(new FilterValueApi("требуется", 1));
		values.add(new FilterValueApi("не требуется", 2));
		attumentFilter.setValues(values);
		otherFilters.add(attumentFilter);

		FilterApi consumableFilter = new FilterApi("Расходуемый при использовании", "consumable");
		values = new ArrayList<>(2);
		values.add(new FilterValueApi("да", 1));
		values.add(new FilterValueApi("нет", 2));
		consumableFilter.setValues(values);
		otherFilters.add(consumableFilter);

		FilterApi chargeFilter = new FilterApi("Есть заряды", "charge");
		values = new ArrayList<>(2);
		values.add(new FilterValueApi("да", 1));
		values.add(new FilterValueApi("нет", 2));
		chargeFilter.setValues(values);
		otherFilters.add(chargeFilter);

		filters.setOther(otherFilters);
		return filters;
	}

	@GetMapping("//api/fvtt/v1/magic-item/{id}")
	public ResponseEntity<FCreature> getCreature(HttpServletResponse response, @PathVariable Integer id){
		MagicItem item = magicItemRepository.findById(id).get();
		response.setContentType("application/json");
		String file = String.format("attachment; filename=\"%s.json\"", item.getEnglishName());
		response.setHeader("Content-Disposition", file);
		return null;//ResponseEntity.ok(new FCreature(creature));
	}
}
