package club.dnd5.portal.model.charactersheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Право другого пользователя редактировать лист персонажа — от запроса до решения владельца (как в
 * core-api). Просить может только тот, кто сохранил лист по ссылке, поэтому перебирать логины или
 * почты некому. Связи с {@link CharacterSheet} через JPA нет, как и у {@link SavedCharacterSheet}:
 * записи чистятся явно — при отзыве ссылки и вытеснении листа из истории.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "character_sheet_editors")
public class CharacterSheetEditor {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36, updatable = false, nullable = false)
	private UUID id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "sheet_id", length = 36, nullable = false, updatable = false)
	private UUID sheetId;

	/** Кто просит; уникален в паре с листом — повторный запрос не плодит записей. */
	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private CharacterSheetEditorStatus status;

	/** Когда отправлен запрос. */
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	/** Когда владелец разрешил; null — ещё не отвечал. */
	@Column(name = "decided_at")
	private Instant decidedAt;
}
