package club.dnd5.portal.model.audit;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Единая таблица истории изменений для всех редактируемых сущностей.
 * Состояние сущности на момент ревизии хранится JSON-снимком, что позволяет
 * восстановить запись без создания таблиц-двойников под каждую сущность.
 */
@Getter
@Setter

@Entity
@Table(
	name = "revision_history",
	indexes = @Index(name = "idx_revision_entity", columnList = "entityType,entityId,revision")
)
public class RevisionHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Логический тип сущности, например {@code MAGIC_ITEM}. */
	@Column(nullable = false, length = 64)
	private String entityType;

	/** Идентификатор сущности в её собственной таблице. */
	@Column(nullable = false)
	private Integer entityId;

	/** Порядковый номер ревизии в рамках пары (entityType, entityId), начиная с 1. */
	@Column(nullable = false)
	private Integer revision;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private RevisionOperation operation;

	/** Снимок состояния сущности в JSON. */
	@Lob
	@Column(columnDefinition = "LONGTEXT")
	private String snapshot;

	private Long userId;

	@Column(length = 255)
	private String username;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	private void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}
}
