package club.dnd5.portal.controller.api.wiki;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.Column;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.Search;
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
import club.dnd5.portal.repository.datatable.RuleDatatableRepository;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Rule", description = "The Rule API")
@RestController
public class RuleApiController {
	@Autowired
	private RuleDatatableRepository ruleRepository;

	@PostMapping(value = "/api/v1/rules", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<RuleApi> getRules(@RequestBody RuleRequestApi request) {
		Specification<Rule> specification = null;
		Sort sort = Sort.unsorted();
		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = Sort.by(request.getOrders()
				.stream()
				.map(order -> order.getDirection().equalsIgnoreCase("asc") ? Sort.Order.asc(order.getField()) : Sort.Order.desc(order.getField()))
				.collect(Collectors.toList()));
		}
		Pageable pageable = null;
		if (request.getPage() != null && request.getLimit() != null) {
			pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);

		}
		if (request.getSearch() != null) {
			if (request.getSearch().getValue() != null && !request.getSearch().getValue().isEmpty()) {
				if (request.getSearch().getExact() != null && request.getSearch().getExact()) {
					specification = (root, query, cb) -> cb.equal(root.get("name"), request.getSearch().getValue().trim().toUpperCase());
				} else {
					String likeSearch = "%" + request.getSearch().getValue() + "%";
					specification = SpecificationUtil.getAndSpecification(null, (root, query, cb) -> {
						cb.or(cb.like(root.get("name"), likeSearch));
						cb.or(cb.like(root.get("englishName"), likeSearch));
						return cb.or(cb.like(root.get("altName"), likeSearch));
					});
				}
			}
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getCategory().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(
					specification, (root, query, cb) -> root.get("type").in(request.getFilter().getCategory()));
			}
		}
		if (pageable == null) {
			return ruleRepository.findAll(specification, sort)
				.stream()
				.map(RuleApi::new)
				.collect(Collectors.toList());
		}
		return ruleRepository.findAll(specification, pageable)
			.stream()
			.map(RuleApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/rules/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RuleDetailApi> getRule(@PathVariable String englishName) {
		Rule rule = ruleRepository.findByEnglishName(englishName.replace('_', ' '));
		if (rule == null) {
			ResponseEntity.notFound().build();
		}
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
