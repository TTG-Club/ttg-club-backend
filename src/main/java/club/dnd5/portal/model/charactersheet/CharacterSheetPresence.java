package club.dnd5.portal.model.charactersheet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Отметка «лист сейчас открыт у пользователя» — мягкая блокировка (как в core-api): остальные видят,
 * кто ещё правит лист. Сохранение отметка не запрещает — от затирания защищает версия листа.
 * Отдельная таблица, а не колонка листа: частые отметки не должны поднимать его версию. Пишется
 * upsert'ом ({@code CharacterSheetPresenceRepository#upsert}).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "character_sheet_presence")
public class CharacterSheetPresence {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(length = 36, updatable = false, nullable = false)
	private UUID id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "sheet_id", length = 36, nullable = false, updatable = false)
	private UUID sheetId;

	/** Кто держит лист открытым; несколько вкладок одного пользователя — одна отметка. */
	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	/** Время последней отметки; свежее 45 секунд — лист открыт. */
	@Column(name = "seen_at", nullable = false)
	private Instant seenAt;
}
