package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.api.wiki.RuleApi;
import club.dnd5.portal.dto.api.wiki.RuleDetailApi;
import club.dnd5.portal.dto.api.wiki.RuleRequestApi;
import club.dnd5.portal.dto.api.wiki.RuleSaveApi;
import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.repository.datatable.RuleRepository;
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

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Правила", description = "API по терминам и правилам")
@RequiredArgsConstructor
@RestController
public class RuleApiController {
	private static final String ENTITY_TYPE = "RULE";

	private final RuleRepository ruleRepository;
	private final BookResolver bookResolver;
	private final AuditService auditService;

	@PostMapping(value = "/api/v1/rules", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RuleApi> getRules(@RequestBody RuleRequestApi request) {
		Specification<Rule> specification = null;
		Optional<RuleRequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			specification = SpecificationUtil.getSearch(request);
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getCategory().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(
					specification, (root, query, cb) -> root.get("type").in(request.getFilter().getCategory()));
			}
		}
		Pageable pageable = PageAndSortUtil.getPageable(request);
		return ruleRepository.findAll(specification, pageable).toList()
			.stream()
			.map(RuleApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/rules/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RuleDetailApi> getRule(@PathVariable String englishName) {
		Rule rule = ruleRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new RuleDetailApi(rule));
	}

	@Operation(summary = "Обновление правила или термина в мастерской")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PatchMapping(value = "/api/v1/workshop/rules/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RuleDetailApi> updateRule(@PathVariable Integer id, @Valid @RequestBody RuleSaveApi request) {
		Rule rule = ruleRepository.findById(id).orElseThrow(PageNotFoundException::new);
		ruleRepository.findByEnglishName(request.getEnglishName().trim())
			.filter(existing -> !existing.getId().equals(id))
			.ifPresent(existing -> {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rule with the same englishName already exists");
			});
		auditService.record(ENTITY_TYPE, id, RevisionOperation.UPDATE, new RuleSaveApi(rule));
		applyRuleRequest(rule, request);
		Rule saved = ruleRepository.saveAndFlush(rule);
		return ResponseEntity.ok(new RuleDetailApi(saved));
	}

	@Operation(summary = "История изменений правила или термина")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/rules/{id}/revisions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RevisionInfoApi> getRuleRevisions(@PathVariable Integer id) {
		ruleRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getRevisions(ENTITY_TYPE, id);
	}

	@Operation(summary = "Состояние правила или термина на указанной ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@GetMapping(value = "/api/v1/workshop/rules/{id}/revisions/{revision}", produces = MediaType.APPLICATION_JSON_VALUE)
	public RuleSaveApi getRuleRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		ruleRepository.findById(id).orElseThrow(PageNotFoundException::new);
		return auditService.getSnapshot(ENTITY_TYPE, id, revision, RuleSaveApi.class);
	}

	@Operation(summary = "Восстановление правила или термина из ревизии")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@Transactional
	@PostMapping(value = "/api/v1/workshop/rules/{id}/revisions/{revision}/restore", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RuleDetailApi> restoreRuleRevision(@PathVariable Integer id, @PathVariable Integer revision) {
		RuleSaveApi snapshot = auditService.getSnapshot(ENTITY_TYPE, id, revision, RuleSaveApi.class);
		return updateRule(id, snapshot);
	}

	@PostMapping("/api/v1/filters/rules")
	public FilterApi getFilter() {
		FilterApi filters = new FilterApi();

		List<FilterApi> otherFilters = new ArrayList<>();

		FilterApi categoryFilter = new FilterApi("Категория", "category");
		categoryFilter.setValues(
				ruleRepository.findAllCategories().stream()
				 .map(value -> new FilterValueApi(value, value))
				 .collect(Collectors.toList()));
		otherFilters.add(categoryFilter);

		filters.setOther(otherFilters);
		return filters;
	}

	private void applyRuleRequest(Rule rule, RuleSaveApi request) {
		rule.setName(request.getName().trim());
		rule.setEnglishName(request.getEnglishName().trim());
		rule.setAltName(trimToNull(request.getAltName()));
		rule.setType(request.getType().trim());
		rule.setDescription(request.getDescription().trim());
		rule.setPage(request.getPage());
		bookResolver.find(request.getSource()).ifPresent(rule::setBook);
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
