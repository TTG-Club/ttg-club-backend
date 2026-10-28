package club.dnd5.portal.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Предпросмотр записи мастерской: запись сохраняется тем же кодом, что и в ручках
 * сохранения, карточка собирается так же, как для страницы записи, а транзакция
 * откатывается. В базе ничего не меняется, история изменений не пишется.
 */
@RequiredArgsConstructor
@Service
public class WorkshopPreview {
	private final PlatformTransactionManager transactionManager;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Сохраняет запись в транзакции, которая всегда откатывается, и возвращает её карточку.
	 *
	 * @param save сохранение записи, как в ручке мастерской
	 * @param card карточка записи, как на странице записи; читается заново из базы
	 * @return карточка записи с несохранёнными изменениями
	 */
	public <T> T render(Runnable save, Supplier<T> card) {
		return new TransactionTemplate(transactionManager).execute(status -> {
			status.setRollbackOnly();
			save.run();
			// Карточку читаем заново: связанные списки (умения, особенности) сохраняются
			// отдельными репозиториями и в уже загруженных сущностях устарели бы
			entityManager.flush();
			entityManager.clear();
			return card.get();
		});
	}
}
