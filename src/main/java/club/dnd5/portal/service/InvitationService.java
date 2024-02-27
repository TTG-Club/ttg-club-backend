package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.InvitationApi;

public interface InvitationService {
	String generateLinkInvitation(Long groupId);
	String generateCodeInvitation(Long groupId);
	InvitationApi getInvitationByGroupId(Long groupId);
	void cancelInvitation(Long groupId);
	void setInvitationExpiration(Long groupId, Long expirationTime);
	String getInviteByLink(Long groupId);
	String getInviteByCode(Long groupId);
	boolean checkTheInvitationLink(String uniqueIdentifier, Long groupId);
	boolean checkTheInvitationCode(String code);
}
