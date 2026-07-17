package club.dnd5.portal.controller.api.item;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.item.WeaponApi;
import club.dnd5.portal.dto.api.item.WeaponDetailApi;
import club.dnd5.portal.dto.api.item.WeaponRequesApi;
import club.dnd5.portal.dto.api.item.WeaponSaveApi;
import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Weapon;
import club.dnd5.portal.model.items.WeaponProperty;
import club.dnd5.portal.repository.datatable.BookRepository;
import club.dnd5.portal.repository.datatable.WeaponPropertyDatatableRepository;
import club.dnd5.portal.repository.datatable.WeaponRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Оружие", description = "API оружия")
@RestController
public class WeaponApiController {
	private static final String ENTITY_TYPE = "WEAPON";

	@Autowired
	private WeaponRepository weaponRepository;
	@Autowired
	private WeaponPropertyDatatableRepository propertyRepository;
	@Autowired
	private BookRepository bookRepository;
	@Autowired
	private AuditService auditService;

	@Operation(summary = "Получение краткого списка оружия")
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
					Join<Book, Weapon> join = root.join("book", JoinType.INNER);
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

	@Operation(summary = "Получение оружия по английскому имени")
	@PostMapping(value = "/api/v1/weapons/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public WeaponDetailApi getWeapon(@PathVariable String englishName) {
		return new WeaponDetailApi(weaponRepository.findByEnglishName(englishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new)
		);
	}

	@Operation(summary = "Создание оружия в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/weapons", produces = MediaType.APPLICATION_JSON_VALUE)
	public WeaponDetailApi createWeapon(@Valid @RequestBody WeaponSaveApi request) {
		if (weaponRepository.findByEnglishName(request.getEnglishName()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weapon with the same englishName already exists");
		}
		Weapon weapon = new Weapon();
		weapon.setBook(getCustomBook());
		applyWeaponRequest(weapon, request);
		Weapon saved = weaponRepository.saveAndFlush(weapon);
		auditService.record(ENTITY_TYPE, saved.getId(), RevisionOperation.CREATE, request);
		return new WeaponDetailApi(saved);
	}

	@Operation(summary = "Обновление оружия в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/weapons/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public WeaponDetailApi updateWeapon(@PathVariable Integer id, @Valid @RequestBody WeaponSaveApi request) {
		Weapon weapon = weaponRepository.findById(id).orElseThrow(PageNotFoundException::new);
		weaponRepository.findByEnglishName(request.getEnglishName())
			.filter(existing -> !existing.getId().equals(id))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Weapon with the same englishName already exists");
			});
		applyWeaponRequest(weapon, request);
		Weapon saved = weaponRepository.saveAndFlush(weapon);
		auditService.record(ENTITY_TYPE, saved.getId(), RevisionOperation.UPDATE, request);
		return new WeaponDetailApi(saved);
	}

	@Operation(summary = "История изменений оружия")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/weapons/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getWeaponRevisions(@PathVariable Integer id) {
		weaponRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE, id);
	}

	@Operation(summary = "Состояние оружия на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/weapons/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public WeaponSaveApi getWeaponRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		weaponRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE, id, revision, WeaponSaveApi.class);
	}

	@Operation(summary = "Восстановление оружия из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/weapons/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public WeaponDetailApi restoreWeaponRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		WeaponSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE, id, revision, WeaponSaveApi.class);
		return updateWeapon(id, snapshot);
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
				 .map(value -> new FilterValueApi(value.getCyrillicName(), value.name()))
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

	private void applyWeaponRequest(Weapon weapon, WeaponSaveApi request) {
		weapon.setName(request.getName().trim());
		weapon.setEnglishName(request.getEnglishName().trim());
		weapon.setAltName(trimToNull(request.getAltName()));
		weapon.setCost(request.getCost());
		weapon.setCurrency(request.getCurrency());
		weapon.setWeight(request.getWeight() == null ? 0 : request.getWeight());
		weapon.setDamageDice(request.getDamageDice());
		weapon.setTwoHandDamageDice(request.getTwoHandDamageDice());
		weapon.setNumberDice(request.getNumberDice());
		weapon.setDamageType(request.getDamageType());
		weapon.setType(request.getType());
		weapon.setMinDistance(request.getMinDistance());
		weapon.setMaxDistance(request.getMaxDistance());
		weapon.setProperties(request.getProperties() == null || request.getProperties().isEmpty()
			? new ArrayList<>()
			: propertyRepository.findAllById(request.getProperties()));
		weapon.setAmmo(request.getAmmo());
		weapon.setDescription(trimToNull(request.getDescription()));
		weapon.setSpecial(trimToNull(request.getSpecial()));
	}

	private Book getCustomBook() {
		return bookRepository.findFirstByType(TypeBook.CUSTOM)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CUSTOM source book is not configured"));
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
