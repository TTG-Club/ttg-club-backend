package club.dnd5.portal.model.tracker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Трекер инициативы (инструмент): энкаунтер с участниками, порядком хода и раундами.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "initiative_tracker",
		indexes = {
				@Index(name = "initiative_tracker_owner_username_index", columnList = "owner_username")
		})
public class InitiativeTracker {

	@Id
	@GeneratedValue(generator = "initiative-tracker-uuid")
	@GenericGenerator(name = "initiative-tracker-uuid", strategy = "org.hibernate.id.UUIDGenerator")
	@Type(type = "uuid-char")
	@Column(length = 36, updatable = false, nullable = false)
	private UUID id;

	@Column(nullable = false)
	private String name;

	/**
	 * Логин владельца. NULL — анонимный трекер: владельца нет, доступ только по ключу
	 * {@link #accessKey}; такие трекеры удаляются по TTL без активности.
	 */
	@Column(name = "owner_username")
	private String ownerUsername;

	/**
	 * Секретный ключ доступа. Возвращается клиенту при создании; аноним хранит его в localStorage
	 * и передаёт в заголовке X-Tracker-Key. Для трекера с владельцем не используется —
	 * доступ проверяется по логину из JWT.
	 */
	@Type(type = "uuid-char")
	@Column(name = "access_key", length = 36, nullable = false)
	private UUID accessKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TrackerStatus status;

	/**
	 * Номер раунда боя: 0 — бой не начат, с 1 — идёт бой.
	 */
	@Column(nullable = false)
	private int round;

	/**
	 * Опция «новая инициатива каждый раунд»: если true — при переходе на новый раунд всем живым
	 * участникам инициатива перебрасывается заново, и порядок хода пересобирается.
	 */
	@Column(name = "reroll_each_round", nullable = false)
	private boolean rerollEachRound;

	/**
	 * id участника, чей сейчас ход. NULL — бой не начат (или текущий участник был единственным и удалён).
	 */
	@Type(type = "uuid-char")
	@Column(name = "current_participant_id", length = 36)
	private UUID currentParticipantId;

	/**
	 * Мягкое удаление: трекер скрыт из списка, но остаётся в истории создания владельца.
	 * Анонимные трекеры удаляются физически (истории у анонима нет).
	 */
	@Column(nullable = false)
	private boolean deleted;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}
