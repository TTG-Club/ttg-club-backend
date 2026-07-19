package club.dnd5.portal.dto.api.tracker;

import club.dnd5.portal.model.tracker.TrackerStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrackerDetailedResponse {

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

	@Schema(description = "id участника, чей сейчас ход. NULL — бой не начат")
	private UUID currentParticipantId;

	@Schema(description = "Секретный ключ доступа к анонимному трекеру: сохраните его на клиенте "
			+ "и передавайте в заголовке X-Tracker-Key. Для трекера с владельцем не используется")
	private UUID accessKey;

	@Schema(description = "Дата создания")
	private Instant createdAt;

	@Schema(description = "Дата последнего изменения")
	private Instant updatedAt;

	@Schema(description = "Участники в порядке хода; не бросавшие инициативу — в конце, в порядке добавления")
	private List<ParticipantResponse> participants;
}
