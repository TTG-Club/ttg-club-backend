package club.dnd5.portal.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkshopPreviewTest {
	@Test
	void shouldSaveThenReadCardAndMarkTransactionForRollback() {
		PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
		SimpleTransactionStatus status = new SimpleTransactionStatus();
		when(transactionManager.getTransaction(any())).thenReturn(status);
		List<Boolean> rollbackOnlyOnCommit = new ArrayList<>();
		doAnswer(invocation -> {
			rollbackOnlyOnCommit.add(invocation.<TransactionStatus>getArgument(0).isRollbackOnly());
			return null;
		}).when(transactionManager).commit(any());
		EntityManager entityManager = mock(EntityManager.class);
		WorkshopPreview preview = new WorkshopPreview(transactionManager);
		ReflectionTestUtils.setField(preview, "entityManager", entityManager);
		List<String> steps = new ArrayList<>();

		String card = preview.render(() -> steps.add("save"), () -> {
			steps.add("card");
			return "Карточка";
		});

		assertThat(card).isEqualTo("Карточка");
		assertThat(steps).containsExactly("save", "card");
		assertThat(rollbackOnlyOnCommit).containsExactly(true);
		InOrder order = inOrder(entityManager);
		order.verify(entityManager).flush();
		order.verify(entityManager).clear();
	}

	@Test
	void shouldRollBackWhenSaveFails() {
		PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
		SimpleTransactionStatus status = new SimpleTransactionStatus();
		when(transactionManager.getTransaction(any())).thenReturn(status);
		WorkshopPreview preview = new WorkshopPreview(transactionManager);
		ReflectionTestUtils.setField(preview, "entityManager", mock(EntityManager.class));

		assertThatThrownBy(() -> preview.render(() -> {
			throw new IllegalStateException("ошибка сохранения");
		}, () -> "Карточка")).isInstanceOf(IllegalStateException.class);

		verify(transactionManager).rollback(status);
		verify(transactionManager, never()).commit(any());
	}
}
