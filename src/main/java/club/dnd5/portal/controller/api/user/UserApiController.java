package club.dnd5.portal.controller.api.user;

import club.dnd5.portal.dto.api.UserApi;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.user.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Tag(name = "Пользователь", description = "API пользователя")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class UserApiController {
	private final UserRepository userRepository;

	@GetMapping("/status")
	public ResponseEntity<Boolean> getStatus() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			Optional<User> user = userRepository.findByEmailOrUsername(authentication.getName(), authentication.getName());
			if (user.isPresent()) {
				return ResponseEntity.ok(true);
			}
		}
		return ResponseEntity.ok(false);
	}

	@GetMapping("/info")
	public ResponseEntity<UserApi> getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
		    Optional<User> user = userRepository.findByEmailOrUsername(authentication.getName(), authentication.getName());
			return user.map(value -> ResponseEntity.ok(new UserApi(value))).orElseGet(() -> ResponseEntity.notFound().build());
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}
}
