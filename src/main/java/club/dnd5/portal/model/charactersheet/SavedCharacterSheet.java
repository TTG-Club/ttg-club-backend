package club.dnd5.portal.model.charactersheet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Чужой лист персонажа, сохранённый пользователем по ссылке («Другие листы»). Хранит ссылку и имя
 * на момент сохранения: если владелец отозвал ссылку или удалил лист, запись видна недоступной.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "character_sheet_saved")
public class SavedCharacterSheet {

	@Id
	@UuidGenerator
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36, updatable = false, nullable = false)
	private UUID id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "sheet_id", length = 36, nullable = false, updatable = false)
	private UUID sheetId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "share_token", length = 36, nullable = false)
	private UUID shareToken;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;
}
