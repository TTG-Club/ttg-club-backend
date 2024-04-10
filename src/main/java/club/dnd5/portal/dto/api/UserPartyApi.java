package club.dnd5.portal.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPartyApi {
	@Schema(description = "Уникальный идентификатор пользовательской группы")
	private Long id;

	@Schema(description = "Уникальный идентификатор владельца")
	private Long ownerId;

	@Schema(description = "Название группы")
	private String groupName;

	@Schema(description = "Описание группы")
	private String description;

	@Schema(description = "Список пользователей, связанных с этой группой")
	private List<UserApi> userApiList = new ArrayList<>();

	@Schema(description = "Дата и время создания этой пользовательской группы")
	private LocalDateTime creationDate;

	@Schema(description = "Дата и время последнего обновления этой пользовательской группы")
	private LocalDateTime lastUpdateDate;
}
