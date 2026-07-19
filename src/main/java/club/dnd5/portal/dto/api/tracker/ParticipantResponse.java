package club.dnd5.portal.dto.api.tracker;

import club.dnd5.portal.model.tracker.ParticipantType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantResponse {

	@Schema(description = "Идентификатор участника")
	private UUID id;

	@Schema(description = "Тип участника (ключ): PLAYER или CREATURE")
	private ParticipantType type;

	@Schema(description = "Человеко-читаемый тип: «Игрок» или «Существо»")
	private String typeName;

	@Schema(description = "Имя участника")
	private String name;

	@Schema(description = "Бонус инициативы")
	private int initiativeBonus;

	@Schema(description = "Повержен: остаётся в списке, но пропускается в порядке хода")
	private boolean dead;

	@Schema(description = "Результат броска d20. NULL — инициатива ещё не брошена")
	private Integer initiativeRoll;

	@Schema(description = "Итог инициативы: бросок + бонус. NULL — инициатива ещё не брошена")
	private Integer initiativeTotal;

	@Schema(description = "Слаг существа в бестиарии (для перехода к статблоку). NULL — игрок")
	private String creatureUrl;
}
