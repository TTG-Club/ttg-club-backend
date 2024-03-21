package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.Invitation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationApi {
	@Schema(description = "Уникальный идентификатор")
	private Long id;

	@Schema(description = "Ссылка", example = "https://ttg.club/invitation/4a9d0fc5-9338-4b17-8c6d-0c47fb28e454?groupId=8")
	private String link;

	@Schema(description = "Код", example = "053794")
	private String code;

	@Schema(description = "Дата генерации")
	private Date generationDate;

	@Schema(description = "Время истечения")
	private Long expirationTime;

	@Schema(description = "Флаг истекшего срока")
	private boolean expired;

	public static InvitationApi fromEntity(Invitation invitation) {
		return InvitationApi.builder()
			.id(invitation.getId())
			.link(invitation.getLink())
			.code(invitation.getCode())
			.generationDate(invitation.getGenerationDate())
			.expirationTime(invitation.getExpirationTime())
			.expired(invitation.isExpired())
			.build();
	}
}
