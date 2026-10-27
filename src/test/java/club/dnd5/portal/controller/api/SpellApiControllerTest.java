package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.spell.SpellRequestApi;
import club.dnd5.portal.dto.api.spells.SpellFilter;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.classes.ArchetypeSpellRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import club.dnd5.portal.service.WorkshopPreview;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpellApiControllerTest {
	@Test
	void shouldFilterSpellsWithMaterialComponentByStoredMaterialDescription() {
		SpellRepository spellRepository = mock(SpellRepository.class);
		when(spellRepository.findAll(any(Specification.class), any(Pageable.class)))
			.thenReturn(Page.empty());
		SpellApiController controller = new SpellApiController(
			spellRepository,
			mock(ClassRepository.class),
			mock(ArchetypeSpellRepository.class),
			mock(BookResolver.class),
			mock(AuditService.class),
			mock(WorkshopPreview.class)
		);
		SpellFilter filter = new SpellFilter(
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.singletonList("3"),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			Collections.emptyList(),
			null,
			null
		);
		SpellRequestApi request = new SpellRequestApi();
		request.setFilter(filter);

		controller.getSpells(request);

		ArgumentCaptor<Specification<Spell>> specificationCaptor = ArgumentCaptor.forClass(Specification.class);
		verify(spellRepository).findAll(specificationCaptor.capture(), any(Pageable.class));
		Root<Spell> root = mock(Root.class);
		CriteriaQuery<?> query = mock(CriteriaQuery.class);
		CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
		Path<Object> materialDescription = mock(Path.class);
		Predicate predicate = mock(Predicate.class);
		when(root.get("additionalMaterialComponent")).thenReturn(materialDescription);
		when(criteriaBuilder.isNotNull(materialDescription)).thenReturn(predicate);

		specificationCaptor.getValue().toPredicate(root, query, criteriaBuilder);

		verify(root).get("additionalMaterialComponent");
		verify(criteriaBuilder).isNotNull(materialDescription);
	}
}
