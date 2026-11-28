package club.dnd5.portal.model.tracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Привязка участника-игрока к листу персонажа (как в core-api): по ней трекер пишет хиты из боя
 * обратно в лист. Хранится в jsonb-колонке участника; сами листы сервер трекера не читает —
 * привязку присылает фронтенд, права на запись в лист проверяет ручка листа.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantSheetLink {

	public static final String SOURCE_OWN = "own";
	public static final String SOURCE_SAVED = "saved";

	@NotNull
	@Schema(description = "Идентификатор листа персонажа")
	private UUID sheetId;

	@NotNull
	@Pattern(regexp = SOURCE_OWN + "|" + SOURCE_SAVED)
	@Schema(description = "Откуда лист: own — свой, saved — сохранённый чужой по ссылке")
	private String source;

	@Schema(description = "Токен ссылки «поделиться». NULL — свой лист")
	private UUID shareToken;

	@Schema(description = "Идентификатор сохранённой записи чужого листа — по нему пишутся его хиты. "
			+ "NULL — свой лист")
	private UUID savedId;
}
