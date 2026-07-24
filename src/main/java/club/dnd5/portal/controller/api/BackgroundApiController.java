package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.classes.BackgroundApi;
import club.dnd5.portal.dto.api.classes.BackgroundDetailApi;
import club.dnd5.portal.dto.api.classes.BackgroundSaveApi;
import club.dnd5.portal.dto.api.classes.FeatRequestApi;
import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.LanguageRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Предыстории", description = "API по предысториям")
@RestController
public class BackgroundApiController {
	private static final String ENTITY_TYPE = "BACKGROUND";

	private final BackgroundRepository backgroundRepository;
	private final BookResolver bookResolver;
	private final LanguageRepository languageRepository;
	private final AuditService auditService;

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

	@Operation(summary = "Создание предыстории в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/backgrounds", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BackgroundDetailApi> createBackground(@Valid @RequestBody BackgroundSaveApi request) {
		if (backgroundRepository.findByEnglishName(request.getEnglishName()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Background with the same englishName already exists");
		}
		Background background = new Background();
		background.setBook(bookResolver.getCustomBook());
		applyBackgroundRequest(background, request);
		Background saved = backgroundRepository.saveAndFlush(background);
		auditService.record(ENTITY_TYPE, saved.getId(), RevisionOperation.CREATE, request);
		return ResponseEntity.ok(new BackgroundDetailApi(saved));
	}

	@Operation(summary = "Обновление предыстории в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/backgrounds/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BackgroundDetailApi> updateBackground(@PathVariable Integer id, @Valid @RequestBody BackgroundSaveApi request) {
		Background background = backgroundRepository.findById(id).orElseThrow(PageNotFoundException::new);
		backgroundRepository.findByEnglishName(request.getEnglishName())
			.filter(existing -> !existing.getId().equals(id))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Background with the same englishName already exists");
			});
		auditService.record(ENTITY_TYPE, id, RevisionOperation.UPDATE, new BackgroundSaveApi(background));
		applyBackgroundRequest(background, request);
		Background saved = backgroundRepository.saveAndFlush(background);
		return ResponseEntity.ok(new BackgroundDetailApi(saved));
	}

	@Operation(summary = "История изменений предыстории")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/backgrounds/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getBackgroundRevisions(@PathVariable Integer id) {
		backgroundRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE, id);
	}

	@Operation(summary = "Состояние предыстории на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/backgrounds/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public BackgroundSaveApi getBackgroundRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		backgroundRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE, id, revision, BackgroundSaveApi.class);
	}

	@Operation(summary = "Восстановление предыстории из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/backgrounds/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BackgroundDetailApi> restoreBackgroundRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		BackgroundSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE, id, revision, BackgroundSaveApi.class);
		return updateBackground(id, snapshot);
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

	private void applyBackgroundRequest(Background background, BackgroundSaveApi request) {
		background.setName(request.getName().trim());
		background.setEnglishName(request.getEnglishName().trim());
		background.setAltName(trimToNull(request.getAltName()));
		background.setSkills(request.getSkills() == null ? new ArrayList<>() : new ArrayList<>(request.getSkills()));
		background.setOtherSkills(trimToNull(request.getOtherSkills()));
		background.setToolOwnership(trimToNull(request.getToolOwnership()));
		background.setEquipmentsText(trimToNull(request.getEquipments()));
		background.setStartMoney(request.getStartGold() == null ? 0 : request.getStartGold());
		background.setDescription(request.getDescription().trim());
		background.setSkillName(trimToNull(request.getSkillName()));
		background.setSkillDescription(trimToNull(request.getSkillDescription()));
		background.setPersonalization(trimToNull(request.getPersonalization()));
		background.setLanguage(trimToNull(request.getLanguage()));
		background.setLifeStyle(request.getLifeStyle());
		background.setLanguages(request.getLanguages() == null || request.getLanguages().isEmpty()
			? new ArrayList<>()
			: languageRepository.findByNameIn(request.getLanguages()));
		bookResolver.find(request.getSource()).ifPresent(background::setBook);
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
