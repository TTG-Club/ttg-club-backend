package club.dnd5.portal.service;

import club.dnd5.portal.model.Invitation;

public interface InvitationService {
	String generateLinkInvitation(String groupId);
	String generateCodeInvitation(String groupId);

	Invitation getInvitationByGroupId(Long groupId);

	void cancelInvitation(String invitationId);
	void setInvitationExpiration(String invitationId, Long expirationTime);

	String getInviteByLink(Long groupId);
	String getInviteByCode(Long groupId);
}
