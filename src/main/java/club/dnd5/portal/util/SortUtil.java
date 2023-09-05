package club.dnd5.portal.util;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.Order;
import org.springframework.data.domain.Sort;

import java.util.Objects;
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

	public static Sort.Order getOrder(Order order) {
		return order.getDirection().equalsIgnoreCase("asc") ? Sort.Order.asc(order.getField()) : Sort.Order.desc(order.getField());
	}
}
