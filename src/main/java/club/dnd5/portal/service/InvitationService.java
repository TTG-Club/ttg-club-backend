package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.InvitationApi;
import club.dnd5.portal.model.Invitation;

public interface InvitationService {
	String generateLinkInvitation(Long partyId, int expirationDay);
	String generateCodeInvitation(Invitation invitation);
	InvitationApi getInvitationByPartyId(Long partyId);
	void cancelInvitation(Long partyId);
	void setInvitationExpiration(Long partyId, int days);
	String getInvitationLinkByPartyId(Long partyId);
	String getInvitationCodeByPartyId(Long partyId);
	boolean isValidUniqueIdentifier(String uniqueIdentifier);
	boolean isValidInvitationCode(String code);
	String addingUserToPartyBasedOnInvitationLink(String uniqueIdentifier);
	String addingUserToPartyBasedOnInvitationCode(String code);
}
