package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.PartyMember;
import club.dnd5.portal.dto.api.UserPartyApi;
import club.dnd5.portal.dto.api.UserPartyCreateApi;
import club.dnd5.portal.dto.api.UserPartyRequestApi;

import java.util.List;

public interface UserPartyService {
	UserPartyApi createUserParty(UserPartyCreateApi userPartyDTO);
	List<UserPartyApi> getAllUserParties(UserPartyRequestApi request);
	UserPartyApi getUserPartyById(Long id);
	List<PartyMember> getUserPartyMembers(Long partyId);
	String deleteUserPartyById(Long id);
	void updateUserParty(Long partyId, UserPartyApi userPartyDTO);
	String leavingFromGroup(Long groupId);
	String kickFromGroup(Long groupId, Long userId);
	String confirmUser(Long groupId, Long userId);
	String sendInvitationEmails(Long userPartyId, List<Long> userIds);
}
