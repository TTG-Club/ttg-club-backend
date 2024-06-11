package club.dnd5.portal.util;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.Order;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * Sorting util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SortUtil {
	/**
	 * Get sort from request.
	 *
	 * @param request request with orders
	 * @return {@link Sort}.
	 * If request is {@code null} or orders is {@code null} or empty, return unsorted sort.
	 * Otherwise, return sort specified by orders.
	 * @throws NullPointerException if directions are missing or {@code request} is {@code null}.
	 */
	public static Sort getSort(RequestApi request) {
		if (CollectionUtils.isEmpty(request.getOrders())) {
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

	/**
	 * Get sort from a list of orders.
	 *
	 * @param order order in format {@code ["field direction", ...]}.
	 *              Direction can be {@code "asc"} (case-insensitive) or any other string.
	 * @return if order is {@code null} or empty, return unsorted sort,
	 * if a direction is {@code "asc"} (case-insensitive), return ascending sort.
	 * Otherwise, return descending sort.
	 */
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

	/**
	 * Get sort from an order.
	 *
	 * @param order with specified field and direction.
	 * @return {@link Sort}.
	 * If a direction is {@code "asc"} (case-insensitive), return ascending sort.
	 * Otherwise, return descending sort.
	 * @throws NullPointerException     if a direction is missing or {@code order} is {@code null}.
	 * @throws IllegalArgumentException if a field is {@code null}.
	 */
	private static Sort.Order getOrder(Order order) {
		String field = order.getField();
		return order.getDirection().equalsIgnoreCase(ASC.name()) ? Sort.Order.asc(field) : Sort.Order.desc(field);
	}
}
