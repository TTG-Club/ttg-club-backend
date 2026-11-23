package club.dnd5.portal.model.charactersheet;

import club.dnd5.portal.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Лист персонажа пользователя. Сервер документ листа не разбирает: он хранится целиком в jsonb,
 * а имя дублируется колонкой, чтобы список не зависел от формата документа.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "character_sheets",
		indexes = {
				@Index(name = "ix_character_sheets_user_id", columnList = "user_id")
		})
public class CharacterSheet {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36, updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;

	@Column(nullable = false, length = 100)
	private String name;

	/** Документ листа (JSON-объект), формат задаёт фронтенд. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private Map<String, Object> data = new LinkedHashMap<>();

	/** Оптимистичная блокировка: автосохранение с устаревшей версией получает 409. */
	@Version
	@Column(nullable = false)
	private Long version;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	/** Проставляется сервисом явно, чтобы значение в ответе совпадало с сохранённым. */
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
