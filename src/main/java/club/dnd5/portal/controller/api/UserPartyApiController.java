package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.UserPartyApi;
import club.dnd5.portal.service.UserPartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-parties")
@RequiredArgsConstructor
public class UserPartyApiController {

	private final UserPartyService userPartyService;

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public UserPartyApi getUserPartyById(@PathVariable Long id) {
		return userPartyService.getUserPartyById(id);
	}

	@GetMapping("/by-name/{name}")
	@ResponseStatus(HttpStatus.OK)
	public UserPartyApi getUserPartyByName(@PathVariable String name) {
		return userPartyService.getUserPartyByName(name);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserPartyApi createUserParty(@RequestBody UserPartyApi userPartyDTO) {
		return userPartyService.createUserParty(userPartyDTO);
	}

	@PutMapping("/{partyId}")
	@ResponseStatus(HttpStatus.OK)
	public void updateUserParty(@PathVariable Long partyId, @RequestBody UserPartyApi userPartyDTO) {
		userPartyService.updateUserParty(partyId, userPartyDTO);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public String deleteUserParty(@PathVariable Long id) {
		return userPartyService.deleteUserPartyById(id);
	}

	@GetMapping("/members/{partyId}")
	@ResponseStatus(HttpStatus.OK)
	public List<Long> getUserPartyMembers(@PathVariable Long partyId) {
		return userPartyService.getUserPartyMembers(partyId);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<UserPartyApi> getAllUserParties() {
		return userPartyService.getAllUserParties();
	}
}
