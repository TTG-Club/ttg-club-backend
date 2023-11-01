package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.NewsBannerApi;
import club.dnd5.portal.model.NewsBanner;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.NewsBannerRepository;
import club.dnd5.portal.repository.YoutubeVideosRepository;
import club.dnd5.portal.repository.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@Tag(name = "News Banners", description = "API для News Banners")
@RestController
@RequestMapping(value = "/api/v1/banners")
public class NewsBannerApiController {
	private static final Set<String> ROLES = new HashSet<>(Arrays.asList("MODERATOR", "ADMIN"));

	private final YoutubeVideosRepository youtubeVideosRepository;
	private final UserRepository userRepository;

	private final NewsBannerRepository newsBannerRepository;

	@Operation(summary = "Get all News Banners")
	@GetMapping
	public ResponseEntity <List<NewsBannerApi>> getNewsBanners(
	) {
		List<NewsBanner> newsBannerList = this.newsBannerRepository.findAll();
		List<NewsBannerApi> newsBannerApiList = new ArrayList<>();
		for (NewsBanner newsBanner : newsBannerList) {
			newsBannerApiList.add(new NewsBannerApi(newsBanner));
		}
		return ResponseEntity.ok(newsBannerApiList);
	}

	@Operation(summary = "Adding a new News Banner")
	@SecurityRequirement(name = "Bearer Authentication")
	@Transactional
	@PostMapping
	public ResponseEntity<?> addNewsBanner(@RequestBody NewsBannerApi newsBannerApi) {
		checkUserPermissions();

		if (newsBannerApi.getName().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("NewsBanner name is incorrect");
		}

		NewsBanner newsBanner = NewsBanner.builder()
			.url(newsBannerApi.getUrl())
			.image(newsBannerApi.getImage())
			.description(newsBannerApi.getDescription())
			.name(newsBannerApi.getName())
			.active(newsBannerApi.isActive())
			.build();
		newsBanner = newsBannerRepository.save(newsBanner);
		return ResponseEntity.status(HttpStatus.OK).body(new NewsBannerApi(newsBanner));
	}

	@Operation(summary = "Update News Banner")
	@SecurityRequirement(name = "Bearer Authentication")
	@Transactional
	@PatchMapping
	public ResponseEntity<?> updateNewsBanner(@RequestBody NewsBannerApi newsBannerApi) {
		checkUserPermissions();

		Optional<NewsBanner> oldNewsBanner = newsBannerRepository.findByName(newsBannerApi.getName());
		if (!oldNewsBanner.isPresent()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("NewsBanner hasn't found by name");
		}

		NewsBanner newNewsBanner = NewsBanner.builder()
			.id(oldNewsBanner.get().getId())
			.url(newsBannerApi.getUrl())
			.image(newsBannerApi.getImage())
			.description(newsBannerApi.getDescription())
			.name(newsBannerApi.getName())
			.active(newsBannerApi.isActive())
			.build();
		return ResponseEntity.status(HttpStatus.OK).body(newsBannerRepository.save(newNewsBanner));
	}

	@Operation(summary = "Remove NewsBanner")
	@SecurityRequirement(name = "Bearer Authentication")
	@DeleteMapping
	public ResponseEntity<?> removeVideo(@RequestParam String name) {
		checkUserPermissions();

		Optional<NewsBanner> newsBanner = newsBannerRepository.findByName(name);
		if (!newsBanner.isPresent()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("News banner name is incorrect!");
		}
		newsBannerRepository.delete(newsBanner.get());
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@Operation(summary = "Change NewsBanner active status")
	@SecurityRequirement(name = "Bearer Authentication")
	@Transactional
	@PatchMapping("/active")
	public ResponseEntity<?> changeVideoActiveStatus(@RequestParam String name, @RequestParam Boolean activeStatus) {
		checkUserPermissions();

		Optional<NewsBanner> newsBanner = newsBannerRepository.findByName(name);
		if (!newsBanner.isPresent()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("News banner name is incorrect!");
		}
		NewsBanner newNewsBanner = newsBanner.get();
		if (newNewsBanner.isActive() == activeStatus) {
			return ResponseEntity.status(HttpStatus.OK).build();
		}
		newNewsBanner.setActive(activeStatus);
		return ResponseEntity.status(HttpStatus.OK).body(newsBannerRepository.save(newNewsBanner));
	}

	private User getCurrentUser() {
		SecurityContext context = SecurityContextHolder.getContext();
		String userName = context.getAuthentication().getName();
		return userRepository.findByEmailOrUsername(userName, userName)
			.orElseThrow(() -> new UsernameNotFoundException(userName));
	}

	private void checkUserPermissions() {
		User user = getCurrentUser();

		if (user.getRoles().stream().map(Role::getName).noneMatch(ROLES::contains)) {
			throw new RuntimeException("You don't have permission!");
		}
	}
}
