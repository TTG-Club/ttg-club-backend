package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.item.ItemApi;
import club.dnd5.portal.dto.api.item.ItemRequestApi;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Treasure;
import club.dnd5.portal.model.items.TreasureType;
import club.dnd5.portal.repository.datatable.TreasureRepository;
import club.dnd5.portal.service.SpecificationService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Сокровища и безделушки", description = "API для сокровищ и безделушек")
@RestController
public class TreasureApiController {
	private final TreasureRepository treasuryRepository;
	private final SpecificationService specificationService;

	@Operation(summary = "Получение списка сокровищ и безделушек")
	@PostMapping(value = "/api/v1/treasures", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ItemApi> getItem(@RequestBody ItemRequestApi request) {
		Specification<Treasure> specification = specificationService.buildSpecification(request);

		if (request.getFilter() != null) {
			if (!request.getFilter().getCategories().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> root.get("type").in(
						request.getFilter().getCategories().stream()
							.map(TreasureType::valueOf)
							.collect(Collectors.toList())));
			}
		}

		Pageable pageable = PageAndSortUtil.getPageable(request);
		List<ItemApi> items = treasuryRepository.findAll(specification, pageable).toList()
			.stream()
			.map(ItemApi::new)
			.collect(Collectors.toList());

		if (request.getFilter() != null && request.getFilter().getRandom()) {
			int sizeList = pageable.getPageSize();
			items = RandomUtils.getRandomObjectListFromList(items, sizeList);
		}

		return items;
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
