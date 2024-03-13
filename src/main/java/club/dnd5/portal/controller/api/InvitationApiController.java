package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.InvitationApi;
import club.dnd5.portal.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitation")
@RequiredArgsConstructor
@Tag(name = "API для работы с приглашениям группы")
public class InvitationApiController {
	private final InvitationService invitationService;

	@Operation(summary = "Получение информации о приглашении")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/party/{partyId}")
	public InvitationApi getInvitationByPartyId(@PathVariable Long partyId) {
		return invitationService.getInvitationByPartyId(partyId);
	}

	@Operation(summary = "Отмена приглашения")
	@PostMapping("/{partyId}/cancel")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public void cancelInvitation(@PathVariable Long partyId) {
		invitationService.cancelInvitation(partyId);
	}

	@Operation(summary = "Получение ссылки")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/link/{partyId}")
	public String getInvitationLinkByPartyId(@PathVariable Long partyId) {
		return invitationService.getInvitationLinkByPartyId(partyId);
	}

	@Operation(summary = "Получение кода")
	@GetMapping("/code/{partyId}")
	public String getInvitationCodeByPartyId(@PathVariable Long partyId) {
		return invitationService.getInvitationCodeByPartyId(partyId);
	}

	@Operation(summary = "Установка значения срока действия ссылки / кода")
	@PutMapping("/{partyId}/expiration/{days}")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public void setInvitationExpiration(@PathVariable Long partyId, @PathVariable int days) {
		invitationService.setInvitationExpiration(partyId, days);
	}

	@Operation(summary = "Добавление участника по ссылке")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/{uniqueIdentifier}")
	@ResponseStatus(HttpStatus.OK)
	public String handleInvitationLink(@PathVariable String uniqueIdentifier) {
		return invitationService.addingUserToPartyBasedOnInvitationLink(uniqueIdentifier);
	}

	@Operation(summary = "Добавление участника по коду")
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/code/{code}")
	@ResponseStatus(HttpStatus.OK)
	public String handleInvitationCode(@PathVariable String code) {
		return invitationService.addingUserToPartyBasedOnInvitationCode(code);
	}
}
