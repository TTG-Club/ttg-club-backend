package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.Randomizable;
import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.model.SpecificationCommon;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.model.items.EquipmentType;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.util.SpecificationUtil;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpecificationServiceImpl implements SpecificationService {
	public <R extends RequestApi, E extends SpecificationCommon> Specification<E> buildSpecification(R request) {
		Specification<E> specification = null;
		if (Optional.ofNullable(request)
			.map(RequestApi::getSearch)
			.map(SearchRequest::getValue)
			.map(value -> !value.isEmpty())
			.orElse(false)) {
			specification = SpecificationUtil.getSearch(request);
		}

		if (request instanceof Randomizable) {
			Randomizable requestWithFilter = (Randomizable) request;
			if (requestWithFilter.getBooks() != null && !requestWithFilter.getBooks().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<Book, Spell> join = root.join("book", JoinType.INNER);
					return join.get("source").in(requestWithFilter.getBooks());
				});
			}

			if (requestWithFilter.getCategories() != null && !requestWithFilter.getCategories().isEmpty()) {
				specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
					Join<EquipmentType, Equipment> types = root.join("types", JoinType.LEFT);
					query.distinct(true);
					return types.in(requestWithFilter.getCategories()
						.stream()
						.map(EquipmentType::valueOf)
						.collect(Collectors.toList()));
				});
			}

		}

		if (request.getOrders() != null && !request.getOrders().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				List<Order> orders = request.getOrders().stream()
					.map(order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField())) : cb.desc(root.get(order.getField())))
					.collect(Collectors.toList());
				query.orderBy(orders);
				return cb.and();
			});
		}

		return specification;
	}
}
