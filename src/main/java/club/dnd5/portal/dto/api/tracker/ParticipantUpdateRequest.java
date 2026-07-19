package club.dnd5.portal.dto.api.tracker;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * Правка участника: применяются только заполненные поля, null — «не менять».
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParticipantUpdateRequest {

	@Size(max = 100)
	@Schema(description = "Новое имя участника")
	private String name;

	@Min(-20)
	@Max(30)
	@Schema(description = "Новый бонус инициативы (итог пересчитается, если бросок уже сделан)")
	private Integer initiativeBonus;

	@Min(1)
	@Max(20)
	@Schema(description = "Ручной результат броска d20 — если игрок кидает живые кости, мастер вносит "
			+ "выпавшее значение; итог считается как бросок + бонус")
	private Integer initiativeRoll;

	@Schema(description = "Пометить участника мёртвым/живым: true — повержен (остаётся в списке, "
			+ "но пропускается в порядке хода), false — вернуть в бой")
	private Boolean dead;
}
