package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.UserPartyApi;

import java.util.List;

public interface UserPartyService {
	UserPartyApi createUserParty(UserPartyApi userPartyDTO);

	List<UserPartyApi> getAllUserParties();

	UserPartyApi getUserPartyById(Long id);

	UserPartyApi getUserPartyByName(String name);

	List<Long> getUserPartyMembers(Long partyId);

	void updateUserPartyName(Long partyId, String newName, String newDescription);

	void deleteUserParty(UserPartyApi userPartyDTO);
}
