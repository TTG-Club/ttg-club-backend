package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.InvitationApi;
import club.dnd5.portal.service.InvitationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationApiController {
	private final InvitationServiceImpl invitationService;

	@Operation(summary = "Получение информации о приглашение")
	@GetMapping("/{groupId}")
	public InvitationApi getInvitationByGroupId(@PathVariable Long groupId) {
		return invitationService.getInvitationByGroupId(groupId);
	}

	@Operation(summary = "Отмена приглашения")
	@PostMapping("/{groupId}/cancel")
	@ResponseStatus(HttpStatus.OK)
	public void cancelInvitation(@PathVariable Long groupId) {
		invitationService.cancelInvitation(groupId);
	}

	@Operation(summary = "Получение ссылки")
	@GetMapping("/link/{groupId}")
	public String getInvitationLinkByGroupId(@PathVariable Long groupId) {
		return invitationService.getInvitationLinkByGroupId(groupId);
	}

	@Operation(summary = "Получение кода")
	@GetMapping("/code/{groupId}")
	public String getInvitationCodeByGroupId(@PathVariable Long groupId) {
		return invitationService.getInvitationCodeByGroupId(groupId);
	}

	@Operation(summary = "Установка значение сколько будет валидна ссылка / code")
	@PutMapping("/{groupId}/expiration/{days}")
	@ResponseStatus(HttpStatus.OK)
	public void setInvitationExpiration(@PathVariable Long groupId, @PathVariable int days) {
		invitationService.setInvitationExpiration(groupId, days);
	}
}
