package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.UserApi;
import club.dnd5.portal.dto.api.UserPartyApi;
import club.dnd5.portal.dto.api.UserPartyCreateApi;

import java.util.List;

public interface UserPartyService {
	UserPartyApi createUserParty(UserPartyCreateApi userPartyDTO);
	List<UserPartyApi> getAllUserParties();
	UserPartyApi getUserPartyById(Long id);
	UserPartyApi getUserPartyByName(String name);
	List<UserApi> getUserPartyMembers(Long partyId);
	String deleteUserPartyById(Long id);
	void updateUserParty(Long partyId, UserPartyApi userPartyDTO);
}
