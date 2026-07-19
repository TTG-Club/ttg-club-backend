package club.dnd5.portal.dto.api.tracker;

import club.dnd5.portal.model.tracker.TrackerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TrackerShortResponse {

	@Schema(description = "Идентификатор трекера")
	private UUID id;

	@Schema(description = "Название трекера")
	private String name;

	@Schema(description = "Статус (ключ): PREPARING или ACTIVE")
	private TrackerStatus status;

	@Schema(description = "Человеко-читаемый статус: «Подготовка» или «Бой»")
	private String statusName;

	@Schema(description = "Номер раунда боя: 0 — бой не начат")
	private int round;

	@Schema(description = "Новая инициатива каждый раунд (опция)")
	private boolean rerollEachRound;

	@Schema(description = "Трекер удалён (виден только в истории при includeDeleted=true)")
	private boolean deleted;

	@Schema(description = "Дата создания (история создания трекеров)")
	private Instant createdAt;

	@Schema(description = "Дата последнего изменения")
	private Instant updatedAt;
}
