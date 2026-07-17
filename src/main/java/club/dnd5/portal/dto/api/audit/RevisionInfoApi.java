package club.dnd5.portal.dto.api.audit;

import club.dnd5.portal.model.audit.RevisionHistory;
import club.dnd5.portal.model.audit.RevisionOperation;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Краткая информация о ревизии для списка истории изменений.
 */
@Getter
public class RevisionInfoApi {
	private final Integer revision;
	private final RevisionOperation operation;
	private final Long userId;
	private final String username;
	private final LocalDateTime createdAt;

	public RevisionInfoApi(RevisionHistory history) {
		this.revision = history.getRevision();
		this.operation = history.getOperation();
		this.userId = history.getUserId();
		this.username = history.getUsername();
		this.createdAt = history.getCreatedAt();
	}
}
