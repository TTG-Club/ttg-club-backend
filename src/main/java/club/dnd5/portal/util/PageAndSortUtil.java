package club.dnd5.portal.util;

import club.dnd5.portal.dto.api.RequestApi;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

/**
 * Сортировка и пейджинацйия
 */
public class PageAndSortUtil {
	public static Pageable getPageable(RequestApi request) {
		Sort sort = Sort.unsorted();
		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = SortUtil.getSort(request);
		}
		if (Objects.nonNull(request.getPage())) {
			return PageRequest.of(request.getPage(), getLimit(request.getLimit()), sort);
		}
		return Pageable.unpaged();
	}

	private static Integer getLimit(Integer limit) {
		if (Objects.nonNull(limit) && limit > 0) {
			return limit;
		}
		else {
			return Integer.MAX_VALUE;
		}
	}
}
