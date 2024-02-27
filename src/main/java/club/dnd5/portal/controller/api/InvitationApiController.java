package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.InvitationApi;
import club.dnd5.portal.service.InvitationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationApiController {
	private final InvitationServiceImpl invitationService;

	@GetMapping("/{groupId}")
	public InvitationApi getInvitationByGroupId(@PathVariable Long groupId) {
		return invitationService.getInvitationByGroupId(groupId);
	}

	@PostMapping("/{groupId}/cancel")
	@ResponseStatus(HttpStatus.OK)
	public void cancelInvitation(@PathVariable Long groupId) {
		invitationService.cancelInvitation(groupId);
	}

	@GetMapping("/link/{groupId}")
	public String getInvitationLinkByGroupId(@PathVariable Long groupId) {
		return invitationService.getInvitationLinkByGroupId(groupId);
	}

	@GetMapping("/code/{groupId}")
	public String getInvitationCodeByGroupId(@PathVariable Long groupId) {
		return invitationService.getInvitationCodeByGroupId(groupId);
	}

	@PutMapping("/{groupId}/expiration/{days}")
	@ResponseStatus(HttpStatus.OK)
	public void setInvitationExpiration(@PathVariable Long groupId, @PathVariable int days) {
		invitationService.setInvitationExpiration(groupId, days);
	}
}
