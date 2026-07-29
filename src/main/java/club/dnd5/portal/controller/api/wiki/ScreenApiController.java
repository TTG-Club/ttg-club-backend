package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.api.wiki.ScreenApi;
import club.dnd5.portal.dto.api.wiki.ScreenDetailApi;
import club.dnd5.portal.dto.api.wiki.ScreenRequestApi;
import club.dnd5.portal.dto.api.wiki.ScreenSaveApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.repository.datatable.ScreenRepository;
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
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Ширма Мастера", description = "API по ширме")
@RequiredArgsConstructor
@RestController
public class ScreenApiController {
	private static final String ENTITY_TYPE = "SCREEN";

	private final ScreenRepository screenRepository;
	private final BookResolver bookResolver;
	private final AuditService auditService;

	@PostMapping(value = "/api/v1/screens", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ScreenApi> getScreens(@RequestBody ScreenRequestApi request) {
		Specification<Screen> specification;

		Optional<RequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		} else {
			specification = Specification.where((root, query, cb) -> cb.isNull(root.get("parent")));
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Rule> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return screenRepository.findAll(specification, pageable).toList()
			.stream()
			.map(ScreenApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/screens/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ScreenDetailApi> getScreen(@PathVariable String englishName) {
		Screen screen = screenRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new ScreenDetailApi(screen));
	}

	@Operation(summary = "Обновление раздела ширмы в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/screens/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ScreenDetailApi> updateScreen(@PathVariable Integer id, @Valid @RequestBody ScreenSaveApi request) {
		Screen screen = screenRepository.findById(id).orElseThrow(PageNotFoundException::new);
		screenRepository.findByEnglishName(request.getEnglishName().trim())
			.filter(existing -> !existing.getId().equals(id))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Screen with the same englishName already exists");
			});
		Screen parent = resolveParent(id, request.getParentId());
		auditService.record(ENTITY_TYPE, id, RevisionOperation.UPDATE, new ScreenSaveApi(screen));
		applyScreenRequest(screen, request, parent);
		Screen saved = screenRepository.saveAndFlush(screen);
		return ResponseEntity.ok(new ScreenDetailApi(saved));
	}

	@Operation(summary = "История изменений раздела ширмы")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/screens/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getScreenRevisions(@PathVariable Integer id) {
		screenRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE, id);
	}

	@Operation(summary = "Состояние раздела ширмы на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/screens/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ScreenSaveApi getScreenRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		screenRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE, id, revision, ScreenSaveApi.class);
	}

	@Operation(summary = "Восстановление раздела ширмы из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/screens/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ScreenDetailApi> restoreScreenRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		ScreenSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE, id, revision, ScreenSaveApi.class);
		return updateScreen(id, snapshot);
	}

	private Screen resolveParent(Integer screenId, Integer parentId) {
		if (parentId == null) {
			return null;
		}
		Screen parent = screenRepository.findById(parentId).orElseThrow(PageNotFoundException::new);
		for (Screen current = parent; current != null; current = current.getParent()) {
			if (screenId.equals(current.getId())) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Screen cannot be its own parent");
			}
		}
		return parent;
	}

	private void applyScreenRequest(Screen screen, ScreenSaveApi request, Screen parent) {
		screen.setName(request.getName().trim());
		screen.setEnglishName(request.getEnglishName().trim());
		screen.setAltName(trimToNull(request.getAltName()));
		screen.setCategory(trimToNull(request.getCategory()));
		screen.setDescription(trimToNull(request.getDescription()));
		screen.setIcon(trimToNull(request.getIcon()));
		screen.setOrdering(request.getOrder() == null ? 0 : request.getOrder());
		screen.setParent(parent);
		bookResolver.find(request.getSource()).ifPresent(screen::setBook);
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
