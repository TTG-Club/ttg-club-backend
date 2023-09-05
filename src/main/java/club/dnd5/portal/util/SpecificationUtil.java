package club.dnd5.portal.util;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

/**
 * Получение спецификаций
 */
public final class SpecificationUtil {
	public static <T> Specification<T> getAndSpecification(Specification<T> specification, Specification<T> addSpecification) {
		if (specification == null) {
			return Specification.where(addSpecification);
		}
		return specification.and(addSpecification);
	}

	public static <T> Specification<T> getOrSpecification(Specification<T> specification, Specification<T> addSpecification) {
		if (specification == null) {
			return Specification.where(addSpecification);
		}
		return specification.or(addSpecification);
	}
	public static <T> Specification<T> getSearch(RequestApi request) {
		if (Optional.of(request).map(RequestApi::getSearch).map(SearchRequest::getExact).orElse(false)) {
			return (root, query, cb) -> cb.equal(
				root.get("name"),
				request.getSearch().getValue().trim().toUpperCase()
			);
		} else {
			String likeSearch = "%" + Optional.of(request)
				.map(RequestApi::getSearch)
				.map(SearchRequest::getValue)
				.orElse("")
				.trim()
				.toUpperCase() + "%";
			return  (root, query, cb) ->
				cb.or(
					cb.like(cb.upper(root.get("altName")), likeSearch),
					cb.like(cb.upper(root.get("englishName")), likeSearch),
					cb.like(cb.upper(root.get("name")), likeSearch)
				);
		}
	}
}
