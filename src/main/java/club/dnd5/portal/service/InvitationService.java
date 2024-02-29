package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.InvitationApi;
import club.dnd5.portal.model.Invitation;

public interface InvitationService {
	String generateLinkInvitation(Long groupId);
	public String generateCodeInvitation(Invitation invitation);
	InvitationApi getInvitationByGroupId(Long groupId);
	void cancelInvitation(Long groupId);
	void setInvitationExpiration(Long groupId, int days);
	String getInvitationLinkByGroupId(Long groupId);
	String getInvitationCodeByGroupId(Long groupId);
	boolean checkTheInvitationLink(String uniqueIdentifier, Long groupId);
	boolean checkTheInvitationCode(String code);
	String addingUserToPartyBasedOnInvitationLink(String uniqueIdentifier, Long groupId);
	String addingUserToPartyBasedOnInvitationCode(String code, Long groupId);
}
