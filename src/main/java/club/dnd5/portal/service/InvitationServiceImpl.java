package club.dnd5.portal.service;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.Invitation;
import club.dnd5.portal.model.user.UserParty;
import club.dnd5.portal.repository.InvitationRepository;
import club.dnd5.portal.repository.UserPartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {
	@Value("${ttg.url}")
	private String ttgUrl;
	private final InvitationRepository invitationRepository;
	private final UserPartyRepository userPartyRepository;

	@Override
	public String generateLinkInvitation(Long groupId) {
		UserParty userParty = userPartyRepository.findById(groupId)
			.orElseThrow(PageNotFoundException::new);

		Invitation invitation = new Invitation();
		invitation.setGenerationDate(new Date());
		invitation.setUserParty(userParty);
		invitation.setCode(generateCodeInvitation(groupId));

		// Generate unique invitation link
		String invitationLink;
		do {
			String uniqueIdentifier = UUID.randomUUID().toString();
			invitationLink = ttgUrl + "/invitation/" + uniqueIdentifier + "?groupId=" + groupId;
		} while (invitationRepository.existsByLink(invitationLink)); // Check if the generated link already exists in the database

		invitation.setLink(invitationLink);
		invitation.setExpirationTime(calculateExpirationTimeInMillis(1));
		invitationRepository.save(invitation);

		return invitationLink;
	}

	@Override
	public String generateCodeInvitation(Long groupId) {
		String code;
		do {
			code = generateUniqueCode();
		} while (invitationRepository.existsByCode(code));

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			invitation.setCode(code);
			invitationRepository.save(invitation);
		} else {
			throw new PageNotFoundException();
		}

		return code;
	}

	@Override
	public Invitation getInvitationByGroupId(Long groupId) {
		return userPartyRepository.findById(groupId)
			.map(UserParty::getInvitation)
			.orElseThrow(PageNotFoundException::new);
	}

	@Override
	public void cancelInvitation(Long groupId) {
		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);

		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			invitationRepository.delete(invitation);
		} else {
			throw new PageNotFoundException();
		}
	}

	@Override
	public void setInvitationExpiration(Long groupId, Long expirationTime) {
		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);

		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			invitation.setExpirationTime(expirationTime);

			invitationRepository.save(invitation);
		} else {
			throw new PageNotFoundException();
		}
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

	@Override
	public boolean checkTheInvitationCode(String code) {
		Optional<Invitation> invitationOptional = invitationRepository.findByCode(code);

		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			if (!invitation.isExpired()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean checkTheInvitationLink(String uniqueIdentifier, Long groupId) {
		String invitationLink = ttgUrl + "/invitation/" + uniqueIdentifier + "?groupId=" + groupId;
		Optional<Invitation> invitationOptional = invitationRepository.findByLink(invitationLink);

		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			return !invitation.isExpired();
		} else {
			return false;
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

	private long calculateExpirationTimeInMillis(int days) {
		final long MILLISECONDS_IN_DAY = 24L * 60 * 60 * 1000;
		return days * MILLISECONDS_IN_DAY;
	}
}
