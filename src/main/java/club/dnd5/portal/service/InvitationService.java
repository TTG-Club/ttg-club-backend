package club.dnd5.portal.service;

import club.dnd5.portal.model.Invitation;

public interface InvitationService {
	// Методы для создания приглашений
	String generateLinkInvitation(String groupId);
	String generateCodeInvitation(String groupId);

	// Методы для отправки приглашений
	void sendInvitationEmail(String invitationId);

	// Методы для получения информации о приглашениях
	Invitation getInvitationById(Long invitationId);

	// Методы для управления приглашениями
	void cancelInvitation(String invitationId);
	void resendInvitation(String invitationId);
	void setInvitationExpiration(String invitationId, Long expirationTime);

	// Методы для получения приглашений
	String getInviteByLink(Long groupId);
	String getInviteByCode(Long groupId);
}
