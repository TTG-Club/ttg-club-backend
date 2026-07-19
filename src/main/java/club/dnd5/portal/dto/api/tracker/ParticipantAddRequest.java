package club.dnd5.portal.dto.api.tracker;

import club.dnd5.portal.model.tracker.ParticipantType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParticipantAddRequest {

	@NotNull
	@Schema(description = "Тип участника: PLAYER (игрок) или CREATURE (существо из бестиария)")
	private ParticipantType type;

	@Size(max = 100)
	@Schema(description = "Имя: для игрока обязательно; для существа — переопределяет название из бестиария")
	private String name;

	@Min(-20)
	@Max(30)
	@Schema(description = "Бонус инициативы игрока (по умолчанию 0). Для существа не учитывается — "
			+ "берётся из статблока бестиария")
	private Integer initiativeBonus;

	@Schema(description = "Слаг существа из бестиария (обязателен для type=CREATURE)")
	private String creatureUrl;

	@Min(1)
	@Max(100)
	@Schema(description = "Сколько существ добавить одной пачкой (только для CREATURE, по умолчанию 1). "
			+ "Имена нумеруются автоматически: «Гоблин 1», «Гоблин 2», ...")
	private Integer count;
}
