package club.dnd5.portal.service;

public interface InvitationService {
	// Методы для создания приглашений
	String generateLinkInvitation(String groupId);
	String generateEmailInvitation(String groupId, String userEmail);
	String generateCodeInvitation(String groupId);

	// Методы для отправки приглашений
	void sendInvitationEmail(String invitationId);

	// Методы для получения информации о приглашениях
	String getInvitationById(String invitationId);
	String getInvitationStatus(String invitationId);

	// Методы для управления приглашениями
	void cancelInvitation(String invitationId);
	void resendInvitation(String invitationId);
	void setInvitationExpiration(String invitationId, Long expirationTime);

	// Методы для получения приглашений
	String getInviteByLink(Long groupId);
	String getInviteByCode(Long groupId);
}