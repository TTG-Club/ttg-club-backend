package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.item.WeaponApi;
import club.dnd5.portal.dto.api.item.WeaponDetailApi;
import club.dnd5.portal.dto.api.item.WeaponRequesApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Weapon;
import club.dnd5.portal.model.items.WeaponProperty;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.WeaponPropertyDatatableRepository;
import club.dnd5.portal.repository.datatable.WeaponRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Weapon", description = "The Weapon API")
@RestController
public class WeaponApiController {
	@Autowired
	private WeaponRepository weaponRepository;
	@Autowired
	private WeaponPropertyDatatableRepository propertyRepository;

	@PostMapping(value = "/api/v1/weapons", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<WeaponApi> getWeapon(@RequestBody WeaponRequesApi request) {
		Specification<Weapon> specification = null;
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
			if (!request.getFilter().getDamageType().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification,
						(root, query, cb) -> root.get("damageType").in(request.getFilter().getDamageType()));
			}
			if (!request.getFilter().getProperrty().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<WeaponProperty, Weapon> join = root.join("properties", JoinType.LEFT);
					query.distinct(true);
					return cb.and(join.get("id").in(request.getFilter().getProperrty()));
				});
			}
			if (!request.getFilter().getDice().isEmpty()) {

				Set<Integer> damages = new HashSet<>(2);
				List<Dice> filterDamageDices = request.getFilter().getDice().stream()
						.filter(s -> !s.isEmpty())
						.map(d -> {
							if(d.startsWith("2")) {
								damages.add(2);
								return d.replace("2", "");
							}
							return d;
						})
						.map(Dice::valueOf)
						.collect(Collectors.toList());
				if (!filterDamageDices.isEmpty()) {
					specification = SpecificationUtil.getAndSpecification(specification,
							(root, query, cb) -> root.get("damageDice").in(filterDamageDices));
				}
				if (damages.contains(2)) {
					specification = SpecificationUtil.getAndSpecification(specification,
							(root, query, cb) -> root.get("numberDice").in(2));
				}
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return weaponRepository.findAll(specification, pageable).toList()
			.stream()
			.map(WeaponApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/weapons/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public WeaponDetailApi getWeapon(@PathVariable String englishName) {
		return new WeaponDetailApi(weaponRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new)
		);
	}

	@PostMapping("/api/v1/filters/weapons")
	public FilterApi getWeaponsFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = weaponRepository.findBook(typeBook);
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

		FilterApi damageTypeFilter = new FilterApi("По типу урона", "damageType");
		damageTypeFilter.setValues(
				DamageType.getWeaponDamage().stream()
				 .map(value -> new FilterValueApi(value.getCyrilicName(), value.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(damageTypeFilter);

		FilterApi properetyTypeFilter = new FilterApi("По свойствам", "properrty");
		properetyTypeFilter.setValues(
				propertyRepository.findAll().stream()
				 .map(value -> new FilterValueApi(value.getName(), value.getId()))
				 .collect(Collectors.toList()));
		otherFilters.add(properetyTypeFilter);

		FilterApi diceFilter = new FilterApi("По кости урона", "dice");
		diceFilter.setValues(
				Arrays.stream(new String[]{"к4", "2к4", "к6", "2к6", "к8", "к10", "к12"})
				 .map(value -> new FilterValueApi(value, value.replace('к', 'd')))
				 .collect(Collectors.toList()));
		otherFilters.add(diceFilter);

		filters.setOther(otherFilters);
		return filters;
	}
}
