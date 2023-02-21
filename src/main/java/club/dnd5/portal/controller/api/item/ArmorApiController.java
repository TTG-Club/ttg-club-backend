package club.dnd5.portal.controller.api.item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.util.SortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.dto.api.item.ArmorApi;
import club.dnd5.portal.dto.api.item.ArmorDetailApi;
import club.dnd5.portal.dto.api.item.ArmorRequesApi;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.ArmorRepository;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Armor", description = "The Armor API")
@RestController
public class ArmorApiController {
	@Autowired
	private ArmorRepository armorRepository;

	@PostMapping(value = "/api/v1/armors", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ArmorApi> getItem(@RequestBody ArmorRequesApi request) {
		Sort sort = Sort.unsorted();
		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = SortUtil.getSort(request);
		}
		Pageable pageable = null;
		if (request.getPage() != null && request.getLimit() != null && request.getLimit() != -1) {
			pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);
		}
		Specification<Armor> specification = null;
		Optional<ArmorRequesApi> optionalRequest = Optional.ofNullable(request);
		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			if (optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getExact).orElse(false)) {
				specification = (root, query, cb) -> cb.equal(root.get("name"), request.getSearch().getValue().trim().toUpperCase());
			} else {
				String likeSearch = "%" + request.getSearch().getValue() + "%";
				specification = (root, query, cb) -> cb.or(cb.like(root.get("altName"), likeSearch),
					cb.like(root.get("englishName"), likeSearch),
					cb.like(root.get("name"), likeSearch));
			}
		}
		if (request.getFilter() != null) {
			if (!request.getFilter().getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(request.getFilter().getBooks());
				});
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
		Collection<Armor> armors;
		if (pageable == null) {
			armors = armorRepository.findAll(specification, sort);
		} else {
			armors = armorRepository.findAll(specification, pageable).toList();
		}
		return armors
			.stream()
			.map(ArmorApi::new)
			.collect(Collectors.toList());
	}

	@PostMapping(value = "/api/v1/armors/{englishName}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ArmorDetailApi getOption(@PathVariable String englishName) {
		return new ArmorDetailApi(armorRepository.findByEnglishName(englishName.replace('_', ' ')).orElseThrow(PageNotFoundException::new));
	}
}
