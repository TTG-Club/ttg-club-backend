package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.api.wiki.RuleApi;
import club.dnd5.portal.dto.api.wiki.RuleDetailApi;
import club.dnd5.portal.dto.api.wiki.RuleRequestApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.repository.datatable.RuleRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Rule", description = "The Rule API")
@RequiredArgsConstructor
@RestController
public class RuleApiController {
	private final RuleRepository ruleRepository;

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
}
