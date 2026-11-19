package club.dnd5.portal.util;

import club.dnd5.portal.dto.api.RequestApi;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.List;
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
			return PageRequest.of(request.getPage(), getSize(request.getSize()), sort);
		}
		return Pageable.unpaged();
	}

	/**
	 * То же, но с последним ключом сортировки {@code tiebreak} (обычно "id").
	 * <p>
	 * MySQL при отсутствии или равенстве ключей сортировки отдавал строки в порядке первичного
	 * ключа; PostgreSQL — в физическом порядке строк, который меняется после UPDATE.
	 */
	public static Pageable getPageable(RequestApi request, String tiebreak) {
		Sort sort = CollectionUtils.isEmpty(request.getOrders()) ? Sort.unsorted() : SortUtil.getSort(request);
		sort = sort.and(Sort.by(tiebreak));
		if (Objects.nonNull(request.getPage())) {
			return PageRequest.of(request.getPage(), getSize(request.getSize()), sort);
		}
		// как и раньше, без страницы пользовательская сортировка не применяется — только порядок по ключу
		return Pageable.unpaged(Sort.by(tiebreak));
	}

	/**
	 * Как {@link #getPageable(RequestApi, String)}, но пользовательская сортировка применяется
	 * и без страницы — для разделов, где она раньше задавалась в спецификации ({@code query.orderBy}).
	 */
	public static Pageable getPageableWithOrders(RequestApi request, String tiebreak) {
		Sort sort = SortUtil.getSort(request).and(Sort.by(tiebreak));
		if (Objects.nonNull(request.getPage())) {
			return PageRequest.of(request.getPage(), getSize(request.getSize()), sort);
		}
		return Pageable.unpaged(sort);
	}

	public static Pageable getPageable(Integer page, Integer size) {
		if (Objects.isNull(page)) {
			return Pageable.unpaged();
		}

		return PageRequest.of(page, getSize(size), Sort.unsorted());
	}

	public static Pageable getPageable(Integer page, Integer size, List<String> order) {
		Sort sort = SortUtil.getSort(order);

		if (Objects.isNull(page)) {
			return Pageable.unpaged();
		}

		return PageRequest.of(page, getSize(size), sort);
	}

	private static Integer getSize(Integer size) {
		if (Objects.nonNull(size) && size > 0) {
			return size;
		} else {
			return Integer.MAX_VALUE;
		}
	}
}
