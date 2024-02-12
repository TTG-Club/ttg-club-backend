package club.dnd5.portal.service;

import club.dnd5.portal.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {
	@Value("${ttg.url}")
	private String ttgUrl;

	private final InvitationRepository invitationRepository;

	@Override
	public String generateLinkInvitation(String groupId) {
		String uniqueIdentifier = UUID.randomUUID().toString();
		return ttgUrl + "/invitation/" + uniqueIdentifier + "?groupId=" + groupId;
	}

	@Override
	public String generateEmailInvitation(String groupId, String userEmail) {
		return null;
	}

	@Override
	public String generateCodeInvitation(String groupId) {
		String code;
		do {
			code = generateUniqueCode();
		} while (invitationRepository.existsByCode(code));
		return code;
	}

	@Override
	public void sendInvitationEmail(String invitationId) {

	}

	@Override
	public String getInvitationById(String invitationId) {
		return null;
	}

	@Override
	public String getInvitationStatus(String invitationId) {
		return null;
	}

	@Override
	public void cancelInvitation(String invitationId) {

	}

	@Override
	public void resendInvitation(String invitationId) {

	}

	@Override
	public void setInvitationExpiration(String invitationId, Long expirationTime) {

	}

	@Override
	public String getInviteByLink(Long groupId) {
		return null;
	}

	@Override
	public String getInviteByCode(Long groupId) {
		return null;
	}

	private String generateUniqueCode() {
		Random random = new Random();
		StringBuilder codeBuilder = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			int digit = random.nextInt(10);
			codeBuilder.append(digit);
		}
		return codeBuilder.toString();
	}
}
