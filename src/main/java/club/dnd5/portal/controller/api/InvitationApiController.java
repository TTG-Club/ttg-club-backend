package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.InvitationApi;
import club.dnd5.portal.service.InvitationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitation")
@RequiredArgsConstructor
public class InvitationApiController {
	private final InvitationServiceImpl invitationService;

	@Operation(summary = "Получение информации о приглашение")
	@GetMapping("/party/{partyId}")
	public InvitationApi getInvitationByGroupId(@PathVariable Long partyId) {
		return invitationService.getInvitationByGroupId(partyId);
	}

	@Operation(summary = "Отмена приглашения")
	@PostMapping("/{partyId}/cancel")
	@ResponseStatus(HttpStatus.OK)
	public void cancelInvitation(@PathVariable Long partyId) {
		invitationService.cancelInvitation(partyId);
	}

	@Operation(summary = "Получение ссылки")
	@GetMapping("/link/{partyId}")
	public String getInvitationLinkByGroupId(@PathVariable Long partyId) {
		return invitationService.getInvitationLinkByGroupId(partyId);
	}

	@Operation(summary = "Получение кода")
	@GetMapping("/code/{partyId}")
	public String getInvitationCodeByGroupId(@PathVariable Long partyId) {
		return invitationService.getInvitationCodeByGroupId(partyId);
	}

	@Operation(summary = "Установка значение сколько будет валидна ссылка / code")
	@PutMapping("/{partyId}/expiration/{days}")
	@ResponseStatus(HttpStatus.OK)
	public void setInvitationExpiration(@PathVariable Long partyId, @PathVariable int days) {
		invitationService.setInvitationExpiration(partyId, days);
	}

	@Operation(summary = "Добавление участника по ссылке")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/{uniqueIdentifier}")
	@ResponseStatus(HttpStatus.OK)
	public String handleInvitationLink(
		@PathVariable String uniqueIdentifier,
		@RequestParam Long partyId
	) {
		return invitationService.addingUserToPartyBasedOnInvitationLink(uniqueIdentifier, partyId);
	}

	@Operation(summary = "Добавление участнике по коду")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/code/{code}")
	@ResponseStatus(HttpStatus.OK)
	public String handleInvitationCode(
		@PathVariable String code,
		@RequestParam Long partyId
	) {
		return invitationService.addingUserToPartyBasedOnInvitationCode(code, partyId);
	}
}
