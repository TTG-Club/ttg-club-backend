package club.dnd5.portal.model.dmscreen;

import club.dnd5.portal.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
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
import java.time.Instant;
import java.util.UUID;

/**
 * Ширма Мастера: именованная раскладка окон с записями сайта, принадлежит пользователю.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dm_screens",
		indexes = {
				@Index(name = "dm_screens_user_id_index", columnList = "user_id")
		})
public class DmScreen {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36, updatable = false, nullable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, updatable = false)
	private User user;

	@Column(nullable = false, length = 64)
	private String name;

	/**
	 * Окна ширмы — JSON-массив {@link club.dnd5.portal.dto.api.dmscreen.DmScreenWindow}.
	 * До 100 окон с путями и заголовками до 256 символов не помещаются в TEXT (64 КБ).
	 */
	@Column(nullable = false, columnDefinition = "MEDIUMTEXT")
	private String windows;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private Instant createdAt;

	/**
	 * Время последнего изменения (имени или раскладки). Проставляется сервисом явно, чтобы
	 * значение в ответе совпадало с сохранённым без дополнительного флаша.
	 */
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
