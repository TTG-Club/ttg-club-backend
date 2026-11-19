package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.spell.SpellRequestApi;
import club.dnd5.portal.dto.api.spells.SpellFilter;
import club.dnd5.portal.model.splells.QSpell;
import club.dnd5.portal.repository.classes.ArchetypeSpellRepository;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import club.dnd5.portal.service.WorkshopPreview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpellApiControllerTest {
	@Test
	void shouldFilterSpellsWithMaterialComponentByStoredMaterialDescription() {
		SpellRepository spellRepository = mock(SpellRepository.class);
		when(spellRepository.findAll(any(Predicate.class), any(Pageable.class)))
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

		// «Материальный компонент» (3) — заклинания, у которых записано описание материала.
		ArgumentCaptor<Predicate> predicateCaptor = ArgumentCaptor.forClass(Predicate.class);
		verify(spellRepository).findAll(predicateCaptor.capture(), any(Pageable.class));
		Predicate expected = new BooleanBuilder(QSpell.spell.additionalMaterialComponent.isNotNull());
		assertThat(predicateCaptor.getValue()).isEqualTo(expected);
	}
}
