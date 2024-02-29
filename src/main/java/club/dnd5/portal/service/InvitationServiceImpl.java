package club.dnd5.portal.service;

import club.dnd5.portal.config.ValueProvider;
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
import org.springframework.core.env.Environment;
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
	private static final String unauthorizedErrorMessage = "User is not authorized to access the invitation";
	private static final String invitationNotFoundErrorMessage = "Invitation not found for the provided groupId";
	private static final Random random = new Random();
	private final Environment environment;
	private final ValueProvider valueProvider;
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


		String[] profiles = this.environment.getActiveProfiles();
		String invitationLink;
		do {
			String uniqueIdentifier = UUID.randomUUID().toString();
			invitationLink = generateInvitationLink(profiles[0], valueProvider.getDevUrl(), valueProvider.getTtgUrl(), uniqueIdentifier, groupId);
		} while (invitationRepository.existsByLink(invitationLink));

		invitation.setLink(invitationLink);
		invitation.setExpirationTime(calculateExpirationTimeInMillis(1));
		invitation.setCode(generateCodeInvitation(invitation));
		invitationRepository.save(invitation);

		return invitationLink;
	}

	@Override
	public String generateCodeInvitation(Invitation invitation) {
		String code;
		do {
			code = generateUniqueCode();
		} while (invitationRepository.existsByCode(code));
		invitation.setCode(code);
		invitationRepository.save(invitation);

		return code;
	}

	@Override
	public InvitationApi getInvitationByGroupId(Long groupId) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, unauthorizedErrorMessage);
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
			throw new ApiException(HttpStatus.FORBIDDEN, unauthorizedErrorMessage);
		}

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		Invitation invitation = invitationOptional
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, invitationNotFoundErrorMessage));

		invitationRepository.delete(invitation);
	}

	@Override
	public void setInvitationExpiration(Long groupId, int days) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, unauthorizedErrorMessage);
		}

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		Invitation invitation = invitationOptional
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, invitationNotFoundErrorMessage));

		invitation.setExpirationTime(calculateExpirationTimeInMillis(days));
		invitationRepository.save(invitation);
	}

	@Override
	public String getInvitationLinkByGroupId(Long groupId) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, unauthorizedErrorMessage);
		}

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			return invitation.getLink();
		} else {
			throw new ApiException(HttpStatus.NOT_FOUND, invitationNotFoundErrorMessage);
		}
	}

	@Override
	public String getInvitationCodeByGroupId(Long groupId) {
		if (!isUserAuthorizedToAccessInvitation(groupId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, unauthorizedErrorMessage);
		}

		Optional<Invitation> invitationOptional = invitationRepository.findByUserPartyId(groupId);
		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			return invitation.getCode();
		} else {
			throw new ApiException(HttpStatus.NOT_FOUND, invitationNotFoundErrorMessage);
		}
	}

	@Override
	public boolean checkTheInvitationCode(String code) {
		Optional<Invitation> invitationOptional = invitationRepository.findByCode(code);

		if (invitationOptional.isPresent()) {
			Invitation invitation = invitationOptional.get();
			return !invitation.isExpired();
		}
		return false;
	}

	@Override
	public boolean checkTheInvitationLink(String uniqueIdentifier, Long groupId) {
		String invitationLink = valueProvider.getTtgUrl() + "/invitation/" + uniqueIdentifier + "?groupId=" + groupId;
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

	private String generateInvitationLink(String activeProfile, String devUrl, String ttgUrl, String uniqueIdentifier, Long groupId) {
		String baseUrl;
		if ("local".equals(activeProfile)) {
			baseUrl = "http://localhost:8080/api/v1";
		} else {
			baseUrl = "dev".equals(activeProfile) ? devUrl : ttgUrl;
		}
		return baseUrl + "/invitation/" + uniqueIdentifier + "?groupId=" + groupId;
	}
}
