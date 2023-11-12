package club.dnd5.portal.controller;

import club.dnd5.portal.dto.user.UserDto;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.user.UserRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@RequiredArgsConstructor
@Hidden
@Controller
public class ProfileController {
	private final UserRepository usersRepository;

	@GetMapping({"/profile", "/profile/{username}"})
	public String getProfileForm(Model model) {
		model.addAttribute("user_count", usersRepository.count());
		model.addAttribute("user_writer", usersRepository.countByRoles("WRITER"));
		model.addAttribute("user_moderator", usersRepository.countByRoles("MODERATOR"));

		return "spa";
	}

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
