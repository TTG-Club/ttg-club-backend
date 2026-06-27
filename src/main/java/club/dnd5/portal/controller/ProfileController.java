package club.dnd5.portal.controller;

import club.dnd5.portal.dto.user.UserDto;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.user.UserRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@RequiredArgsConstructor
@Hidden
@Controller
public class ProfileController {
	private final UserRepository usersRepository;

	@PostMapping(value = "/api/v1/profile/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getProfile(@PathVariable String username) {
		Optional<User> user = usersRepository.findByName(username);

		if (!user.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		UserDto publicUser = new UserDto(user.get());

		return ResponseEntity.ok(publicUser);
	}
}
