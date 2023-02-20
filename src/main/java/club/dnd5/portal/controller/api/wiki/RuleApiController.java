package club.dnd5.portal.controller.api.wiki;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.util.SortUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.FilterApi;
import club.dnd5.portal.dto.api.FilterValueApi;
import club.dnd5.portal.dto.api.wiki.RuleApi;
import club.dnd5.portal.dto.api.wiki.RuleDetailApi;
import club.dnd5.portal.dto.api.wiki.RuleRequestApi;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.repository.datatable.RuleRepository;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Rule", description = "The Rule API")
@RestController
public class RuleApiController {
	@Autowired
	private RuleRepository ruleRepository;

	@PostMapping(value = "/api/v1/rules", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RuleApi> getRules(@RequestBody RuleRequestApi request) {
		Sort sort = Sort.unsorted();
		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = SortUtil.getSort(request);
		}
		Pageable pageable = null;
		if (request.getPage() != null && request.getLimit() != null) {
			pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);
		}
		Specification<Rule> specification = null;
		Optional<RuleRequestApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			if (optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getExact).orElse(false)) {
				specification = (root, query, cb) -> cb.equal(root.get("name"), request.getSearch().getValue().trim().toUpperCase());
			} else {
				String likeSearch = "%" + request.getSearch().getValue() + "%";
				specification = SpecificationUtil.getAndSpecification(null, (root, query, cb) -> {
					return cb.or(cb.like(root.get("altName"), likeSearch),
						cb.like(root.get("englishName"), likeSearch),
						cb.like(root.get("name"), likeSearch));
				});
			}
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getCategory().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(
					specification, (root, query, cb) -> root.get("type").in(request.getFilter().getCategory()));
			}
		}
		Collection<Rule> rules;
		if (pageable == null) {
			rules = ruleRepository.findAll(specification, sort);
		} else {
			rules = ruleRepository.findAll(specification, pageable).toList();
		}
		return rules
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
