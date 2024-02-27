package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.InvitationApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.Invitation;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.model.user.UserParty;
import club.dnd5.portal.repository.InvitationRepository;
import club.dnd5.portal.repository.UserPartyRepository;
import club.dnd5.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
	private final UserRepository userRepository;

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
	public InvitationApi getInvitationByGroupId(Long groupId) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "User is not authorized to access the invitation");
		}

		UserParty userParty = userPartyRepository.findById(groupId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User party not found for the provided groupId"));

		Invitation invitation = userParty.getInvitation();

		// Convert Invitation entity to InvitationApi DTO
		return InvitationApi.fromEntity(invitation);
	}

	@Override
	public void cancelInvitation(Long groupId) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "User is not authorized to access the invitation");
		}

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		Invitation invitation = invitationOptional
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invitation not found for the provided groupId"));

		invitationRepository.delete(invitation);
	}

	@Override
	public void setInvitationExpiration(Long groupId, Long expirationTime) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "User is not authorized to access the invitation");
		}

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		Invitation invitation = invitationOptional
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Invitation not found for the provided groupId"));

		invitation.setExpirationTime(expirationTime);
		invitationRepository.save(invitation);
	}

	@Override
	public String getInviteByLink(Long groupId) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "User is not authorized to access the invitation");
		}

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			return invitation.getLink();
		} else {
			throw new ApiException(HttpStatus.NOT_FOUND, "Invitation not found for the provided groupId");
		}
	}

	@Override
	public String getInviteByCode(Long groupId) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "User is not authorized to access the invitation");
		}

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			return invitation.getCode();
		} else {
			throw new ApiException(HttpStatus.NOT_FOUND, "Invitation not found for the provided groupId");
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

	private boolean isUserAuthorizedToAccessInvitation(Long groupId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			String userEmail = authentication.getName();
			Optional<User> optionalUser = userRepository.findByEmail(userEmail);
			if (optionalUser.isPresent()) {
				User user = optionalUser.get();
				UserParty userParty = userPartyRepository.findById(groupId).orElseThrow(PageNotFoundException::new);
				return userParty.getOwnerId().equals(user.getId());
			}
			return false;
		}
		throw new IllegalStateException("User is not authenticated");
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
