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
 * Участник трекера инициативы: игрок (имя + бонус вручную) или существо из бестиария.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "initiative_participant",
		indexes = {
				@Index(name = "initiative_participant_tracker_index", columnList = "tracker_id")
		})
public class InitiativeParticipant {

	@Id
	@GeneratedValue(generator = "initiative-participant-uuid")
	@GenericGenerator(name = "initiative-participant-uuid", strategy = "org.hibernate.id.UUIDGenerator")
	@Type(type = "uuid-char")
	@Column(length = 36, updatable = false, nullable = false)
	private UUID id;

	@Type(type = "uuid-char")
	@Column(name = "tracker_id", length = 36, nullable = false)
	private UUID trackerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ParticipantType type;

	@Column(nullable = false)
	private String name;

	/**
	 * Бонус инициативы: игроку задаёт пользователь, существу — снапшот из бестиария
	 * на момент добавления (правка бестиария не влияет на идущий бой).
	 */
	@Column(name = "initiative_bonus", nullable = false)
	private int initiativeBonus;

	/**
	 * Слаг существа в бестиарии. Ссылка мягкая, без FK — по конвенции проекта на bestiary
	 * никто не ссылается; нужна фронту для перехода к статблоку. NULL — игрок.
	 */
	@Column(name = "creature_url")
	private String creatureUrl;

	/**
	 * Результат броска d20. NULL — инициатива ещё не брошена.
	 */
	@Column(name = "initiative_roll")
	private Integer initiativeRoll;

	/**
	 * Итог инициативы: бросок + бонус. NULL — инициатива ещё не брошена.
	 */
	@Column(name = "initiative_total")
	private Integer initiativeTotal;

	/**
	 * «Монетка» финального тай-брейка: случайное число, назначаемое при броске. Хранится,
	 * чтобы порядок хода был детерминированным — участник, добавленный в идущий бой,
	 * не пересортировывает остальных.
	 */
	@Column(name = "tie_roll")
	private Integer tieRoll;

	/**
	 * Порядковый номер добавления в трекер: стабильный порядок списка до броска
	 * и последний тай-брейк после.
	 */
	@Column(nullable = false)
	private int seq;

	/**
	 * Повержен: участник остаётся в списке (виден, помечен мёртвым), но пропускается в порядке хода.
	 * Снимается при завершении боя (reset).
	 */
	@Column(nullable = false)
	private boolean dead;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt;
}
