package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.item.ArmorApi;
import club.dnd5.portal.dto.api.item.ArmorDetailApi;
import club.dnd5.portal.dto.api.item.ArmorFilter;
import club.dnd5.portal.dto.api.item.ArmorRequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.ArmorRepository;
import club.dnd5.portal.util.PageAndSortUtil;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Доспехи (броня)", description = "The Armor API")
@RestController
public class ArmorApiController {
	private final ArmorRepository armorRepository;

	@Operation(summary = "Получение краткого списка доспехов")
	@PostMapping(value = "/api/v1/armors", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ArmorApi> getItem(@RequestBody ArmorRequestApi request) {
		Specification<Armor> specification = null;
		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}

		if (request.getFilter() != null) {
			ArmorFilter armorFilter = request.getFilter();
			if (!armorFilter.getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
			if (armorFilter.getDisadvantage() != null) {
				int disadvantage = armorFilter.getDisadvantage() ? 1 : 0;
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> cb.equal(root.get("stels_hindrance"), disadvantage));
			}
			if (!armorFilter.getStrengthRequirements().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> root.get("force_requirements").in(armorFilter.getStrengthRequirements()));
			}
			if (!armorFilter.getTypeArmor().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification,
					(root, query, cb) -> root.get("force_requirements").in(armorFilter.getTypeArmor()));
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return armorRepository.findAll(specification, pageable).toList()
			.stream()
			.map(ArmorApi::new)
			.collect(Collectors.toList());
	}

	@Operation(summary = "Получение доспеха по английскому имени")
	@PostMapping(value = "/api/v1/armors/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ArmorDetailApi getOption(@PathVariable String englishName) {
		return new ArmorDetailApi(armorRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new));
	}

	@Operation(summary = "Получение фильтра для снаряжение")
	@PostMapping("/api/v1/filters/armors")
	public FilterApi getArmorFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = armorRepository.findBook(typeBook);
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

		FilterApi typeFilter = new FilterApi("Тип брони", "type");
		typeFilter.setValues(
			Arrays.stream(ArmorType.values())
				.map(value -> new FilterValueApi(value.getCyrillicName(), value.name()))
				.collect(Collectors.toList()));
		otherFilters.add(typeFilter);

		FilterApi strengthFilter = new FilterApi("Требование к силе", "strength requirements");
		List<FilterValueApi> values = armorRepository.findAll().stream()
			.filter(armor -> armor.getForceRequirements() != null)
			.map(armor -> {
				int requirements = armor.getForceRequirements();
				return new FilterValueApi(requirements + " силы", requirements + " strength");
			})
			.distinct()
			.collect(Collectors.toList());
		strengthFilter.setValues(values);
		otherFilters.add(strengthFilter);

		FilterApi disadvantageFilter = new FilterApi("Помеха при ношение", "disadvantage");
		List<FilterValueApi> disadvantageValues = new ArrayList<>();
		disadvantageValues.add(new FilterValueApi("Есть", "yes"));
		disadvantageValues.add(new FilterValueApi("Нету", "no"));
		disadvantageFilter.setValues(disadvantageValues);
		otherFilters.add(disadvantageFilter);

		filters.setOther(otherFilters);
		return filters;
	}
}
