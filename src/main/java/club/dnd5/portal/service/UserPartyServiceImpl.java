package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.UserApi;
import club.dnd5.portal.dto.api.UserPartyApi;
import club.dnd5.portal.dto.api.UserPartyCreateApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.model.user.UserParty;
import club.dnd5.portal.repository.UserPartyRepository;
import club.dnd5.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

		List<User> usersToSendEmail = userPartyDTO.getUserListIds().stream()
			.map(userRepository::findById)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());


		emailService.sendInvitationLink(usersToSendEmail,
			invitationService.generateLinkInvitation(userParty.getId()));

		return convertToUserPartyApi(userParty);
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
	public List<UserApi> getUserPartyMembers(Long partyId) {
		Optional<UserParty> optionalUserParty = userPartyRepository.findById(partyId);
		List<UserApi> userApis = new ArrayList<>();
		if (optionalUserParty.isPresent()) {
			for (User user : optionalUserParty.get().getUserList()) {
				UserApi userApi = convertFromUserToUserApi(user);
				userApis.add(userApi);
			}
		}
		return userApis;
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
		userPartyApi.setUserApiList(userParty.getUserList().stream()
			.map(this::convertFromUserToUserApi)
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
			.userList(userPartyDTO.getUserApiList().stream()
				.map(userApi -> userApi.getEmail())
				.map(userRepository::findByEmail)
				.filter(Optional::isPresent)
				.map(Optional::get)
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
		userParty.setCreationDate(new Date());
		userParty.setLastUpdateDate(new Date());
		return userParty;
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

	private UserApi convertFromUserToUserApi (User user) {
		return UserApi.builder()
			.email(user.getEmail())
			.name(user.getName())
			.roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
			.username(user.getUsername())
			.build();
	}
}
