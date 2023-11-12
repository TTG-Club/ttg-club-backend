package club.dnd5.portal.util;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.Order;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сортировка
 */
public class SortUtil {
	public static Sort getSort(RequestApi request) {
		if (request.getOrders() == null) {
			return Sort.unsorted();
		}
		return Sort.by(
			request.getOrders()
				.stream()
				.filter(Objects::nonNull)
				.map(SortUtil::getOrder)
				.collect(Collectors.toList())
		);
	}

	public static Sort getSort(List<String> order) {
		if (CollectionUtils.isEmpty(order)) {
			return Sort.unsorted();
		}

		return Sort.by(
			order
				.stream()
				.filter(Objects::nonNull)
				.map(Order::new)
				.map(SortUtil::getOrder)
				.collect(Collectors.toList())
		);
	}

	public static Sort.Order getOrder(Order order) {
		return order.getDirection().equalsIgnoreCase("asc") ? Sort.Order.asc(order.getField()) : Sort.Order.desc(order.getField());
	}

	public static Sort.Order getOrder(@NonNull String field, String direction) {
		return Optional.of(direction).orElse("asc").equalsIgnoreCase("asc")
			? Sort.Order.asc(field)
			: Sort.Order.desc(field);
	}
}
