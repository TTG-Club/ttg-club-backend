package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.audit.RevisionInfoApi;
import club.dnd5.portal.model.audit.RevisionOperation;

import java.util.List;

/**
 * Универсальный сервис истории изменений редактируемых сущностей.
 * Хранит JSON-снимок состояния сущности на каждую ревизию в единой таблице
 * {@code revision_history} и позволяет восстановить состояние из ревизии.
 */
public interface AuditService {

	/**
	 * Фиксирует новую ревизию сущности со снимком её текущего состояния.
	 *
	 * @param entityType  логический тип сущности (например, {@code MAGIC_ITEM})
	 * @param entityId    идентификатор сущности
	 * @param operation   тип операции
	 * @param snapshotDto объект состояния, сериализуемый в JSON
	 */
	void record(String entityType, Integer entityId, RevisionOperation operation, Object snapshotDto);

	/**
	 * Список ревизий сущности от новой к старой.
	 */
	List<RevisionInfoApi> getRevisions(String entityType, Integer entityId);

	/**
	 * Снимок состояния сущности на указанной ревизии, десериализованный в заданный тип.
	 */
	<T> T getSnapshot(String entityType, Integer entityId, Integer revision, Class<T> type);
}
