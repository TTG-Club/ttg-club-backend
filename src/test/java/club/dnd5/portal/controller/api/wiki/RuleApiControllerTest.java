package club.dnd5.portal.controller.api.wiki;

import club.dnd5.portal.dto.api.wiki.RuleSaveApi;
import club.dnd5.portal.model.audit.RevisionOperation;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.repository.datatable.RuleRepository;
import club.dnd5.portal.service.AuditService;
import club.dnd5.portal.service.BookResolver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuleApiControllerTest {
	@Test
	void shouldUpdateRuleAndRecordPreviousState() {
		Rule rule = existingRule();
		RuleRepository ruleRepository = mock(RuleRepository.class);
		AuditService auditService = mock(AuditService.class);
		BookResolver bookResolver = mock(BookResolver.class);
		when(ruleRepository.findById(1)).thenReturn(Optional.of(rule));
		when(ruleRepository.findByEnglishName(any())).thenReturn(Optional.empty());
		when(ruleRepository.saveAndFlush(any(Rule.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RuleSaveApi request = new RuleSaveApi();
		request.setName("Новое правило");
		request.setEnglishName("New rule");
		request.setAltName(" Альтернативное ");
		request.setType(" Термин ");
		request.setDescription(" Новое описание ");
		request.setPage((short) 42);

		RuleApiController controller = new RuleApiController(ruleRepository, bookResolver, auditService);
		controller.updateRule(1, request);

		ArgumentCaptor<RuleSaveApi> snapshot = ArgumentCaptor.forClass(RuleSaveApi.class);
		verify(auditService).record(eq("RULE"), eq(1), eq(RevisionOperation.UPDATE), snapshot.capture());
		assertThat(snapshot.getValue().getName()).isEqualTo("Старое правило");
		assertThat(snapshot.getValue().getEnglishName()).isEqualTo("Old rule");
		assertThat(snapshot.getValue().getPage()).isEqualTo((short) 12);
		assertThat(rule.getName()).isEqualTo("Новое правило");
		assertThat(rule.getEnglishName()).isEqualTo("New rule");
		assertThat(rule.getAltName()).isEqualTo("Альтернативное");
		assertThat(rule.getType()).isEqualTo("Термин");
		assertThat(rule.getDescription()).isEqualTo("Новое описание");
		assertThat(rule.getPage()).isEqualTo((short) 42);
	}

	private Rule existingRule() {
		Book book = new Book("PHB");
		book.setName("Книга игрока");
		book.setType(TypeBook.OFFICAL);

		Rule rule = new Rule();
		rule.setId(1);
		rule.setName("Старое правило");
		rule.setEnglishName("Old rule");
		rule.setType("Правило");
		rule.setDescription("Старое описание");
		rule.setBook(book);
		rule.setPage((short) 12);
		return rule;
	}
}
