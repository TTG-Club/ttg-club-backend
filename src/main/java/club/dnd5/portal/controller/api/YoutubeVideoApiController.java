package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.PaginatedResponseApi;
import club.dnd5.portal.dto.api.YoutubeVideoApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.YoutubeVideo;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.YoutubeVideosRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Youtube", description = "API для ссылок на ютуб")
@RestController
@RequestMapping(value = "/api/v1/youtube")
public class YoutubeVideoApiController {
	private static final Set<String> ROLES = new HashSet<>(Arrays.asList("MODERATOR", "ADMIN"));

	private final YoutubeVideosRepository youtubeVideosRepository;
	private final UserRepository userRepository;

	@Operation(summary = "Получение списка видео")
	@GetMapping
	public ResponseEntity<PaginatedResponseApi<YoutubeVideoApi>> getVideos(
		@RequestParam(required = false) String search,
		@RequestParam(required = false, defaultValue = "0") final Integer page,
		@RequestParam(required = false, defaultValue = "-1") final Integer size,
		@RequestParam(required = false) List<String> order,
		@RequestParam(required = false) Boolean activeStatus
	) {
		Specification<YoutubeVideo> specification = null;

		if (Objects.nonNull(search)) {
			specification = SpecificationUtil.getSearchByName(search);
		}

		if (activeStatus != null) {
			specification = SpecificationUtil.getAndSpecification(
				specification,
				(root, query, cb) -> cb.equal(root.get("active"), activeStatus)
			);
		}

		Pageable pageable = PageAndSortUtil.getPageable(page, size, order);

		long count = getCount(activeStatus);

		Collection<YoutubeVideo> videos = youtubeVideosRepository.findAll(specification, pageable).toList();

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(
				new PaginatedResponseApi<>(
					videos
						.stream()
						.map(YoutubeVideoApi::new)
						.collect(Collectors.toList()),
					count,
					page,
					size
				)
			);
	}

	@Operation(summary = "Adding new video")
	@SecurityRequirement(name = "Bearer Authentication")
	@Transactional
	@PostMapping
	public ResponseEntity<?> addVideo(@RequestBody YoutubeVideoApi videoApi) {
		checkUserPermissions();

		if (videoApi.getId().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Video ID is incorrect");
		}

		if (videoApi.getName().isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Video name is incorrect");
		}

		Optional<YoutubeVideo> oldVideo = youtubeVideosRepository.findById(videoApi.getId());

		if (oldVideo.isPresent()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Video with current ID is already exist");
		}

		User user = getCurrentUser();

		YoutubeVideo newVideo = new YoutubeVideo();

		newVideo.setId(videoApi.getId());
		newVideo.setName(videoApi.getName());
		newVideo.setUser(user);
		newVideo.setActive(Boolean.FALSE);
		newVideo.setCreated(LocalDateTime.now());

		YoutubeVideo saved = youtubeVideosRepository.save(newVideo);

		return ResponseEntity.status(HttpStatus.OK).body(new YoutubeVideoApi(saved));
	}

	@Operation(summary = "Update video")
	@SecurityRequirement(name = "Bearer Authentication")
	@Transactional
	@PatchMapping
	public ResponseEntity<?> updateVideos(@RequestBody YoutubeVideoApi videoApi) {
		checkUserPermissions();

		if (videoApi.getId() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Video hasn't ID");
		}

		YoutubeVideo video = youtubeVideosRepository.findById(videoApi.getId())
			.orElseThrow(PageNotFoundException::new);

		video.setName(videoApi.getName());

		return ResponseEntity.status(HttpStatus.OK).body(new YoutubeVideoApi(youtubeVideosRepository.save(video)));
	}

	@Operation(summary = "Remove video")
	@SecurityRequirement(name = "Bearer Authentication")
	@DeleteMapping
	public ResponseEntity<?> removeVideo(@RequestParam String id) {
		checkUserPermissions();

		if (id.length() != 11) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Youtube video ID is incorrect!");
		}

		youtubeVideosRepository.deleteById(id);

		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@Operation()
	@SecurityRequirement(name = "Bearer Authentication")
	@GetMapping("/count")
	public long getVideoCount(@RequestParam(required = false) Boolean active) {
		return getCount(active);
	}

	@Operation(summary = "Change video active status")
	@SecurityRequirement(name = "Bearer Authentication")
	@Transactional
	@PatchMapping("/active")
	public ResponseEntity<?> changeVideoActiveStatus(@RequestParam String id, @RequestParam Boolean activeStatus) {
		checkUserPermissions();

		YoutubeVideo video = youtubeVideosRepository.findById(id)
			.orElseThrow(PageNotFoundException::new);

		if (video.isActive() == Boolean.TRUE.equals(activeStatus)) {
			return ResponseEntity.status(HttpStatus.OK).build();
		}

		video.setActive(activeStatus);

		return ResponseEntity.status(HttpStatus.OK).body(new YoutubeVideoApi(youtubeVideosRepository.save(video)));
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

	private long getCount(Boolean active) {
		if (active != null) {
			return youtubeVideosRepository.countByActive(active);
		} else {
			return youtubeVideosRepository.count();
		}
	}
}
