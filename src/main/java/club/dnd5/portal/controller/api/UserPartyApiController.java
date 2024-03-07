package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.PartyMember;
import club.dnd5.portal.dto.api.UserPartyApi;
import club.dnd5.portal.dto.api.UserPartyCreateApi;
import club.dnd5.portal.dto.api.UserPartyRequestApi;
import club.dnd5.portal.service.UserPartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Пати", description = "API для пати")
@RestController
@RequestMapping("/api/v1/user-parties")
@RequiredArgsConstructor
public class UserPartyApiController {
	private final UserPartyService userPartyService;

	@Operation(summary = "Получение группы по айди")
	@GetMapping("/{partyId}")
	@ResponseStatus(HttpStatus.OK)
	public UserPartyApi getUserPartyById(@PathVariable Long partyId) {
		return userPartyService.getUserPartyById(partyId);
	}

	@Operation(summary = "Получение краткого списка всех групп")
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public List<UserPartyApi> getAllUserParties(@RequestBody UserPartyRequestApi request) {
		return userPartyService.getAllUserParties(request);
	}

	@Operation(summary = "Создание группы")
	@PostMapping("/create")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.CREATED)
	public UserPartyApi createUserParty(@RequestBody UserPartyCreateApi userPartyDTO) {
		return userPartyService.createUserParty(userPartyDTO);
	}

	@Operation(summary = "Обновление группы")
	@PutMapping("/{partyId}")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserParty(@PathVariable Long partyId, @RequestBody UserPartyApi userPartyDTO) {
		userPartyService.updateUserParty(partyId, userPartyDTO);
	}

	@Operation(summary = "Удаление группы")
	@DeleteMapping("/{partyId}")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public String deleteUserParty(@PathVariable Long partyId) {
		return userPartyService.deleteUserPartyById(partyId);
	}

	@Operation(summary = "Получение юзеров, которые находятся в группе, в том числе юзеров которые ожидают подтверждение")
	@GetMapping("/members/{partyId}")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public List<PartyMember> getUserPartyMembers(@PathVariable Long partyId) {
		return userPartyService.getUserPartyMembers(partyId);
	}

	@Operation(summary = "Удаление участника группы, создателем группы")
	@DeleteMapping("/members/{partyId}/kick/{userId}")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public String kickFromGroup(@PathVariable("partyId") Long partyId, @PathVariable("userId") Long userId) {
		return userPartyService.kickFromGroup(partyId, userId);
	}

	@Operation(summary = "Покидание группы")
	@DeleteMapping("/leaving/{partyId}")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public String leavingFromGroup(@PathVariable("partyId") Long partyId) {
		return userPartyService.leavingFromGroup(partyId);
	}

	@Operation(summary = "Подтверждение участника")
	@PutMapping("/{partyId}/confirm/{userId}")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public String confirmUser(@PathVariable Long partyId, @PathVariable Long userId) {
		return userPartyService.confirmUser(partyId, userId);
	}

	@Operation(summary = "Отправка приглашений по электронной почте")
	@PostMapping("/{partyId}/send-invitations")
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.OK)
	public String sendInvitationsByEmail(@PathVariable Long partyId, @RequestBody List<Long> userIds) {
		return userPartyService.sendInvitationEmails(partyId, userIds);
	}
}
