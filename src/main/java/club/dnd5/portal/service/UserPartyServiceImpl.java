package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.UserPartyApi;
import club.dnd5.portal.dto.api.UserPartyCreateApi;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.model.user.UserParty;
import club.dnd5.portal.repository.UserPartyRepository;
import club.dnd5.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPartyServiceImpl implements UserPartyService {
	private final UserPartyRepository userPartyRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final InvitationServiceImpl invitationService;

	@Override
	public UserPartyApi createUserParty(UserPartyCreateApi userPartyDTO) {
		String userEmail = getAuthenticatedUserEmail();
		User user = userRepository.findByEmail(userEmail).orElseThrow(PageNotFoundException::new);

		UserParty userParty = convertFromUserPartyCreateToEntity(userPartyDTO);
		userParty.setOwnerId(user.getId());

		userParty.getUserList().add(user);
		user.getUserParties().add(userParty);
		userRepository.save(user);

		userParty = userPartyRepository.save(userParty);

		List<User> usersToSendEmail = userParty.getUserList();

		emailService.sendInvitationLink(usersToSendEmail,
			invitationService.generateLinkInvitation(userParty.getId()));

		return convertToUserPartyApi(userParty);
	}

	@Override
	@Transactional
	public void addingUserToPartyBasedOnInvitationLink(String uniqueIdentifier, Long groupId) {
		addUserToPartyBasedOnInvitation(uniqueIdentifier, groupId, true);
	}

	@Override
	@Transactional
	public void addingUserToPartyBasedOnInvitationCode(String code, Long groupId) {
		addUserToPartyBasedOnInvitation(code, groupId, false);
	}

	private void addUserToPartyBasedOnInvitation(String identifier, Long groupId, boolean isLink) {
		String userEmail = getAuthenticatedUserEmail();
		User user = userRepository.findByEmail(userEmail).orElseThrow(PageNotFoundException::new);
		Optional<UserParty> optionalUserParty = userPartyRepository.findById(groupId);

		if (!optionalUserParty.isPresent()) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Party not found");
		}

		UserParty userParty = optionalUserParty.get();
		if (isLink && invitationService.checkTheInvitationLink(identifier, groupId)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Invalid URL");
		} else if (!isLink && invitationService.checkTheInvitationCode(identifier)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Invalid invitation code");
		}

		addUserToParty(user, userParty);
	}

	@Override
	public List<UserPartyApi> getAllUserParties() {
		List<UserParty> userParties = userPartyRepository.findAll();
		return convertToUserPartyApiList(userParties);
	}

	@Override
	public UserPartyApi getUserPartyById(Long id) {
		Optional<UserParty> optionalUserParty = userPartyRepository.findById(id);
		return optionalUserParty.map(this::convertToUserPartyApi)
			.orElseThrow(PageNotFoundException::new);
	}

	@Override
	public UserPartyApi getUserPartyByName(String name) {
		Optional<UserParty> optionalUserParty = userPartyRepository.findByGroupName(name);
		return optionalUserParty.map(this::convertToUserPartyApi)
			.orElseThrow(PageNotFoundException::new);
	}

	@Override
	public List<Long> getUserPartyMembers(Long partyId) {
		Optional<UserParty> optionalUserParty = userPartyRepository.findById(partyId);
		return optionalUserParty.map(userParty -> userParty.getUserList().stream()
				.map(User::getId)
				.collect(Collectors.toList()))
			.orElseThrow(PageNotFoundException::new);
	}

	@Override
	public void updateUserParty(Long partyId, UserPartyApi userPartyDTO) {
		Optional<UserParty> optionalUserParty = userPartyRepository.findById(partyId);
		optionalUserParty.ifPresent(userParty -> {
			userParty.setGroupName(userPartyDTO.getGroupName());
			userParty.setDescription(userPartyDTO.getDescription());
			userParty.setLastUpdateDate(new Date());
			userPartyRepository.save(userParty);
		});
	}

	@Override
	public String deleteUserPartyById(Long id) {
		Optional<UserParty> optionalUserParty = userPartyRepository.findById(id);
		if (optionalUserParty.isPresent()) {
			userPartyRepository.deleteById(id);
			return "Delete was successful";
		} else {
			return "User party with id " + id + " not found";
		}
	}

	// Utility methods for conversion between API DTO and JPA Entity
	private UserPartyApi convertToUserPartyApi(UserParty userParty) {
		UserPartyApi userPartyApi = new UserPartyApi();
		userPartyApi.setId(userParty.getId());
		userPartyApi.setOwnerId(userParty.getOwnerId());
		userPartyApi.setGroupName(userParty.getGroupName());
		userPartyApi.setDescription(userParty.getDescription());
		userPartyApi.setUserListIds(userParty.getUserList().stream()
			.map(User::getId)
			.collect(Collectors.toList()));
		userPartyApi.setCreationDate(userParty.getCreationDate());
		userPartyApi.setLastUpdateDate(userParty.getLastUpdateDate());
		return userPartyApi;
	}

	private UserParty convertToUserPartyEntity(UserPartyApi userPartyDTO) {
		return UserParty.builder()
			.ownerId(userPartyDTO.getOwnerId())
			.groupName(userPartyDTO.getGroupName())
			.description(userPartyDTO.getDescription())
			.userList(userPartyDTO.getUserListIds().stream()
				.filter(id -> userRepository.findById(id).isPresent())
				.map(id -> userRepository.findById(id).get())
				.collect(Collectors.toList())
			)
			.creationDate((new Date()))
			.lastUpdateDate(userPartyDTO.getLastUpdateDate())
			.build();
	}

	private UserParty convertFromUserPartyCreateToEntity(UserPartyCreateApi userPartyCreateApi) {
		UserParty userParty = new UserParty();
		userParty.setGroupName(userPartyCreateApi.getGroupName());
		userParty.setDescription(userPartyCreateApi.getDescription());
		userParty.setUserList(userPartyCreateApi.getUserListIds().stream()
			.filter(id -> userRepository.findById(id).isPresent())
			.map(id -> userRepository.findById(id).get())
			.collect(Collectors.toList())
		);
		userParty.setCreationDate(new Date());
		userParty.setLastUpdateDate(new Date());
		return userParty;
	}

	private List<User> retrieveUsersFromUserPartyApi(UserPartyApi userPartyDTO) {
		return userPartyDTO.getUserListIds().stream()
			.map(userId -> userRepository.findById(userId).orElse(null))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	private List<UserPartyApi> convertToUserPartyApiList(List<UserParty> userParties) {
		List<UserPartyApi> userPartyApis = new ArrayList<>();
		for (UserParty userParty : userParties) {
			userPartyApis.add(convertToUserPartyApi(userParty));
		}
		return userPartyApis;
	}

	private String getAuthenticatedUserEmail() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			return authentication.getName();
		}
		throw new IllegalStateException("User is not authenticated");
	}

	private void addUserToParty(User user, UserParty userParty) {
		if (!user.getUserParties().contains(userParty)) {
			user.getUserParties().add(userParty);
			userRepository.save(user);
		}
	}
}
