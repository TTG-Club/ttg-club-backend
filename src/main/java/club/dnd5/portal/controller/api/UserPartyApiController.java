package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.UserApi;
import club.dnd5.portal.dto.api.UserPartyApi;
import club.dnd5.portal.dto.api.UserPartyCreateApi;
import club.dnd5.portal.dto.api.UserPartyRequestApi;
import club.dnd5.portal.service.UserPartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-parties")
@RequiredArgsConstructor
public class UserPartyApiController {
	private final UserPartyService userPartyService;

	@Operation(summary = "Получение группы по айди")
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public UserPartyApi getUserPartyById(@PathVariable Long id) {
		return userPartyService.getUserPartyById(id);
	}

	@Operation(summary = "Создание группы")
	@PostMapping
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.CREATED)
	public UserPartyApi createUserParty(@RequestBody UserPartyCreateApi userPartyDTO) {
		return userPartyService.createUserParty(userPartyDTO);
	}

	@Operation(summary = "Обновление группы")
	@PutMapping("/{partyId}")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserParty(@PathVariable Long partyId, @RequestBody UserPartyApi userPartyDTO) {
		userPartyService.updateUserParty(partyId, userPartyDTO);
	}

	@Operation(summary = "Удаление группы")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public String deleteUserParty(@PathVariable Long id) {
		return userPartyService.deleteUserPartyById(id);
	}

	@Operation(summary = "Получение айди юзеров, которые находятся в группе")
	@GetMapping("/members/{partyId}")
	@ResponseStatus(HttpStatus.OK)
	public List<UserApi> getUserPartyMembers(@PathVariable Long partyId) {
		return userPartyService.getUserPartyMembers(partyId);
	}

	@Operation(summary = "Получение краткого списка всех групп")
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public List<UserPartyApi> getAllUserParties(@RequestBody UserPartyRequestApi request) {
		return userPartyService.getAllUserParties(request);
	}
}
