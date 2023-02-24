package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.YoutubeVideoApi;
import club.dnd5.portal.model.YoutubeVideo;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.YoutubeVideosRepository;
import club.dnd5.portal.repository.user.RoleRepository;
import club.dnd5.portal.repository.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Youtube", description = "The Youtube API")
@RestController
@RequestMapping(value = "/api/v1/youtube")
public class YoutubeVideoApiController {
	@Autowired
	YoutubeVideosRepository youtubeVideosRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	RoleRepository roleRepository;

	@Operation(summary = "Get last added video")
	@GetMapping(value = "/last")
	public ResponseEntity<YoutubeVideoApi> getLastVideo() {
		YoutubeVideo video = youtubeVideosRepository.findLastAdded();

		if (video != null) {
			return ResponseEntity.ok(new YoutubeVideoApi(video));
		}

		return ResponseEntity.internalServerError().build();
	}

	@Operation(summary = "Adding new video")
	@SecurityRequirement(name = "Bearer Authentication")
	@PutMapping(path = "/{id}")
	public ResponseEntity<String> addVideo(@PathVariable String id) {
		if (id.length() != 11) {
			return ResponseEntity.badRequest().body("Youtube video ID is incorrect!");
		}

		User user = getCurrentUser();
		boolean roleSuccess = user.getRoles().stream().anyMatch(role -> (role.getName().equals("MODERATOR") || role.getName().equals("ADMIN")));

		if (!roleSuccess) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission!");
		}

		Optional<YoutubeVideo> oldVideo = youtubeVideosRepository.findById(id);
		YoutubeVideo video = oldVideo.orElseGet(YoutubeVideo::new);

		video.setId(id);
		video.setUser(user);
		video.setActive(true);
		video.setOrder(0);

		youtubeVideosRepository.saveAndFlush(video);

		return ResponseEntity.ok().build();
	}

	private User getCurrentUser() {
		SecurityContext context = SecurityContextHolder.getContext();
		String userName = context.getAuthentication().getName();

		return userRepository.findByEmailOrUsername(userName, userName)
				.orElseThrow(() -> new UsernameNotFoundException(userName));
	}
}
