package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.model.SpecificationCommon;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationService {
	<R extends RequestApi, E extends SpecificationCommon> Specification<E> buildSpecification(R request);
}
