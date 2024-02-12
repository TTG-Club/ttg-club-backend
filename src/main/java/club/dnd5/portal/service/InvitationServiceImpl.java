package club.dnd5.portal.service;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.Invitation;
import club.dnd5.portal.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
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
		String uniqueIdentifier;
		String invitationLink;
		do {
			uniqueIdentifier = UUID.randomUUID().toString();
			invitationLink = ttgUrl + "/invitation/" + uniqueIdentifier + "?groupId=" + groupId;
		} while (invitationRepository.existsByLink(invitationLink)); // Check if the generated link already exists in the database
		return invitationLink;
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
	public Invitation getInvitationById(Long invitationId) {
		return invitationRepository.findById(invitationId).orElseThrow(PageNotFoundException::new);
	}

	@Override
	public void cancelInvitation(String invitationId) {
		Optional<Invitation> invitationOptional = invitationRepository.findById(Long.parseLong(invitationId));

		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			invitationRepository.delete(invitation);
		} else {
			throw new PageNotFoundException();
		}
	}

	@Override
	public void resendInvitation(String invitationId) {

	}

	@Override
	public void setInvitationExpiration(String invitationId, Long expirationTime) {

	}

	@Override
	public String getInviteByLink(Long groupId) {
		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			return invitation.getLink();
		} else {
			throw new PageNotFoundException();
		}
	}

	@Override
	public String getInviteByCode(Long groupId) {
		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			return invitation.getCode();
		} else {
			throw new PageNotFoundException();
		}
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
