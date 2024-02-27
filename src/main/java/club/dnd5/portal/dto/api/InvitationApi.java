package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.Invitation;
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
	private Long id;
	private String link;
	private String code;
	private Date generationDate;
	private Long expirationTime;
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
