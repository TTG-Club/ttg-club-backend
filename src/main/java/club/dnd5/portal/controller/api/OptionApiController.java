package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.classes.OptionApi;
import club.dnd5.portal.dto.api.classes.OptionDetailApi;
import club.dnd5.portal.dto.api.classes.OptionRequesApi;
import club.dnd5.portal.dto.api.classes.OptionSaveApi;
import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.Option.OptionType;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.OptionRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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

@Tag(name = "Опции классов и архетипов", description = "API по опций классов и архетипоы")
@RequiredArgsConstructor
@RestController
public class OptionApiController {
	private static final String ENTITY_TYPE = "OPTION";

	private final OptionRepository optionRepository;
	private final ClassRepository classRepository;
	private final BookResolver bookResolver;
	private final AuditService auditService;

	@PostMapping(value = "/api/v1/options", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OptionApi> getOptions(@RequestBody OptionRequesApi request) {
		Specification<Option> specification = null;
		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!CollectionUtils.isEmpty(request.getFilter().getBooks())) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
			if (!CollectionUtils.isEmpty(request.getFilter().getClassOption())) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<OptionType, Option> join = root.join("optionTypes", JoinType.LEFT);
					query.distinct(true);
					return join.in(request.getFilter().getClassOption());
				});
			}
			if (!CollectionUtils.isEmpty(request.getFilter().getLevels())) {
				if(request.getFilter().getLevels().contains("Нет")) {
					specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> cb.isNull(root.get("level")));
					request.getFilter().getLevels().remove("Нет");
				}
				if (!request.getFilter().getLevels().isEmpty()) {
					specification = SpecificationUtil.getAndSpecification(
							specification, (root, query, cb) -> root.get("level").in(request.getFilter().getLevels().stream().map(Integer::valueOf).collect(Collectors.toList())));
				}
			}
			if (!CollectionUtils.isEmpty(request.getFilter().getPrerequsite())) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> root.get("prerequisite").in(request.getFilter().getPrerequsite()));
			}
		}
		if (request.getOrders()!=null && !request.getOrders().isEmpty()) {
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
		return optionRepository.findAll(specification, pageable).toList()
			.stream()
			.map(OptionApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/options/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OptionDetailApi> getOption(@PathVariable String englishName) {
		Option option = optionRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new OptionDetailApi(option));
	}

	@Operation(summary = "Создание особенности класса в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/options", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OptionDetailApi> createOption(@Valid @RequestBody OptionSaveApi request) {
		if (optionRepository.findByEnglishName(request.getEnglishName().trim()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option with the same englishName already exists");
		}
		Option option = new Option();
		option.setBook(bookResolver.getCustomBook());
		applyOptionRequest(option, request);
		Option saved = optionRepository.saveAndFlush(option);
		auditService.record(ENTITY_TYPE, saved.getId(), RevisionOperation.CREATE, request);
		return ResponseEntity.ok(new OptionDetailApi(saved));
	}

	@Operation(summary = "Обновление особенности класса в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/options/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OptionDetailApi> updateOption(@PathVariable Integer id, @Valid @RequestBody OptionSaveApi request) {
		Option option = optionRepository.findById(id).orElseThrow(PageNotFoundException::new);
		optionRepository.findByEnglishName(request.getEnglishName().trim())
			.filter(existing -> !existing.getId().equals(id))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option with the same englishName already exists");
			});
		auditService.record(ENTITY_TYPE, id, RevisionOperation.UPDATE, new OptionSaveApi(option));
		applyOptionRequest(option, request);
		Option saved = optionRepository.saveAndFlush(option);
		return ResponseEntity.ok(new OptionDetailApi(saved));
	}

	@Operation(summary = "История изменений особенности класса")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/options/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getOptionRevisions(@PathVariable Integer id) {
		optionRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE, id);
	}

	@Operation(summary = "Состояние особенности класса на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/options/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public OptionSaveApi getOptionRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		optionRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE, id, revision, OptionSaveApi.class);
	}

	@Operation(summary = "Восстановление особенности класса из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/options/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OptionDetailApi> restoreOptionRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		OptionSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE, id, revision, OptionSaveApi.class);
		return updateOption(id, snapshot);
	}

	@PostMapping("/api/v1/filters/options")
	public FilterApi getOptionFilter() {
		FilterApi filters = new FilterApi();
		List<FilterApi> sources = new ArrayList<>();
		for (TypeBook typeBook : TypeBook.values()) {
			List<Book> books = optionRepository.findBook(typeBook);
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

		FilterApi classOptionFilter = new FilterApi("Классовые особености", "classOption");
		classOptionFilter.setValues(
				Arrays.stream(Option.OptionType.values())
				 .map(ability -> new FilterValueApi(ability.getName(), ability.name()))
				 .collect(Collectors.toList()));
		otherFilters.add(classOptionFilter);

		otherFilters.add(getLevelsFilter());
		otherFilters.add(getPrerequisiteFilter(optionRepository.findAlldPrerequisite()));

		filters.setOther(otherFilters);
		return filters;
	}

	@PostMapping("/api/v1/filters/options/{englishClassName}")
	public FilterApi getByClassFilter(@PathVariable String englishClassName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		List<FilterApi> otherFilters = new ArrayList<>();
		otherFilters.add(getLevelsFilter(heroClass.getOptionType()));
		otherFilters.add(getPrerequisiteFilter(optionRepository.findAlldPrerequisite(heroClass.getOptionType())));

		List<FilterApi> customFilters = new ArrayList<>();
		FilterApi customFilter = new FilterApi();
		customFilter.setName("Классовые особенности");
		customFilter.setKey("classOption");
		customFilter.setHidden(Boolean.TRUE);

		FilterValueApi customValue = new FilterValueApi();
		customValue.setLabel(heroClass.getCapitalazeName());
		customValue.setDefaultValue(Boolean.TRUE);
		customValue.setKey(heroClass.getOptionType().name());
		customFilter.setValues(Collections.singletonList(customValue));
		customFilters.add(customFilter);
		otherFilters.add(customFilter);

		filters.setOther(otherFilters);
		return filters;
	}

	@PostMapping("/api/v1/filters/options/{englishClassName}/{englishArchetypeName}")
	public FilterApi getByArchitypeFilter(@PathVariable String englishClassName, @PathVariable String englishArchetypeName) {
		FilterApi filters = new FilterApi();

		HeroClass heroClass = classRepository.findByEnglishName(englishClassName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		Archetype archetype = heroClass.getArchetypes()
			.stream()
			.filter(a -> a.getEnglishName().equalsIgnoreCase(englishArchetypeName.replace('_', ' ')))
			.findFirst().get();

		List<FilterApi> otherFilters = new ArrayList<>();
		otherFilters.add(getLevelsFilter(heroClass.getOptionType()));
		otherFilters.add(getPrerequisiteFilter(optionRepository.findAlldPrerequisite(heroClass.getOptionType())));

		List<FilterApi> customFilters = new ArrayList<>();
		FilterApi customFilter = new FilterApi();
		customFilter.setName("Классовые особенности");
		customFilter.setKey("classOption");
		customFilter.setHidden(Boolean.TRUE);

		FilterValueApi customValue = new FilterValueApi();
		customValue.setLabel(heroClass.getCapitalazeName());
		customValue.setDefaultValue(Boolean.TRUE);
		customValue.setKey(archetype.getOptionType().name());
		customFilter.setValues(Collections.singletonList(customValue));
		customFilters.add(customFilter);
		otherFilters.add(customFilter);

		filters.setOther(otherFilters);
		return filters;
	}

	private FilterApi getLevelsFilter() {
		FilterApi levelsFilter = new FilterApi("Требования к уровню", "levels");
		List<String> levels =  optionRepository.findAllLevels();
		levelsFilter.setValues(
				levels.stream()
				.map(v -> v == null ? "Нет" : v)
				.map(value -> new FilterValueApi(value, value))
				.collect(Collectors.toList()));
		return levelsFilter;
	}

	private FilterApi getLevelsFilter(OptionType optionType) {
		FilterApi levelsFilter = new FilterApi("Требования к уровню", "levels");
		List<String> levels =  optionRepository.findAllLevel(optionType);
		levelsFilter.setValues(
				levels.stream()
				.map(v -> v == null ? "Нет" : v)
				.map(value -> new FilterValueApi(value, value))
				.collect(Collectors.toList()));
		return levelsFilter;
	}

	private FilterApi getPrerequisiteFilter(Collection<String> prerequisite) {
		FilterApi prerequisiteFilter = new FilterApi("Требования", "prerequsite");
		if (prerequisite.size() == 1) {
			prerequisiteFilter.setHidden(Boolean.TRUE);
		}
		prerequisiteFilter.setValues(
				prerequisite.stream()
				 .map(ability -> new FilterValueApi(ability, ability))
				 .collect(Collectors.toList()));
		return prerequisiteFilter;
	}

	private void applyOptionRequest(Option option, OptionSaveApi request) {
		option.setName(request.getName().trim());
		option.setEnglishName(request.getEnglishName().trim());
		option.setAltName(trimToNull(request.getAltName()));
		option.setLevel(request.getLevel());
		String prerequisite = trimToNull(request.getPrerequisite());
		option.setPrerequisite(prerequisite == null ? "Нет" : prerequisite);
		option.setDescription(request.getDescription().trim());
		option.setOptionTypes(new ArrayList<>(request.getOptionTypes()));
		bookResolver.find(request.getSource()).ifPresent(option::setBook);
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
