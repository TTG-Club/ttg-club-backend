package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.RequestApi;
import club.dnd5.portal.dto.api.ResponseApi;
import club.dnd5.portal.dto.api.spells.SearchRequest;
import club.dnd5.portal.dto.api.youtube.YoutubeRequestApi;
import club.dnd5.portal.dto.api.youtube.YoutubeVideoApi;
import club.dnd5.portal.model.YoutubeVideo;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.YoutubeVideosRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.util.SortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Order;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Youtube", description = "The Youtube API")
@RestController
@RequestMapping(value = "/api/v1/youtube")
public class YoutubeVideoApiController {
	private static final Set<String> ROLES = new HashSet<>(Arrays.asList("MODERATOR", "ADMIN"));

	@Autowired
	YoutubeVideosRepository youtubeVideosRepository;
	@Autowired
	UserRepository userRepository;

	@Operation(summary = "Get added video")
	@PostMapping
	public ResponseEntity<ResponseApi<YoutubeVideoApi>> getLastVideo(@RequestBody YoutubeRequestApi request) {
		Sort sort = Sort.unsorted();

		if (!CollectionUtils.isEmpty(request.getOrders())) {
			sort = SortUtil.getSort(request);
		}

		Pageable pageable = null;

		if (request.getPage() != null && request.getLimit() != null) {
			pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);
		}

		Specification<YoutubeVideo> specification = null;
		Optional<YoutubeRequestApi> optionalRequest = Optional.ofNullable(request);

		if (!optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getValue).orElse("").isEmpty()) {
			if (optionalRequest.map(RequestApi::getSearch).map(SearchRequest::getExact).orElse(false)) {
				specification = (root, query, cb) -> cb.equal(root.get("name"), request.getSearch().getValue().trim().toUpperCase());
			} else {
				String likeSearch = "%" + request.getSearch().getValue() + "%";
				specification = (root, query, cb) -> cb.like(root.get("name"), likeSearch);
			}
		}

		if (request.getOrders() != null && !request.getOrders().isEmpty()) {
			specification = SpecificationUtil.getAndSpecification(specification, (root, query, cb) -> {
				List<Order> orders = request.getOrders().stream()
					.map(
						order -> "asc".equals(order.getDirection()) ? cb.asc(root.get(order.getField())) : cb.desc(root.get(order.getField()))
					)
					.collect(Collectors.toList());

				query.orderBy(orders);

				return cb.and();
			});
		}

		Boolean active = null;

		if (request.getFilter() != null && request.getFilter().getActive() != null) {
			active = request.getFilter().getActive();

			specification = SpecificationUtil.getAndSpecification(
				specification,
				(root, query, cb) -> cb.equal(root.get("active"), request.getFilter().getActive())
			);
		}

		long count;

		if (active == null) {
			count = youtubeVideosRepository.count();
		} else {
			count = youtubeVideosRepository.countByActive(active);
		}

		Collection<YoutubeVideo> videos;

		if (pageable == null) {
			videos = youtubeVideosRepository.findAll(specification, sort);
		} else {
			videos = youtubeVideosRepository.findAll(specification, pageable).toList();
		}

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(
				new ResponseApi<>(
					count,
					videos
						.stream()
						.map(YoutubeVideoApi::new)
						.collect(Collectors.toList())
				)
			);
	}

	@Operation(summary = "Adding new video")
	@SecurityRequirement(name = "Bearer Authentication")
	@PostMapping("/add")
	public ResponseEntity<?> addVideo(@RequestBody YoutubeVideoApi videoApi) {
		User user = getCurrentUser();

		if (!user.getRoles().stream().map(Role::getName).anyMatch(ROLES::contains)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission");
		}

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

		YoutubeVideo newVideo = new YoutubeVideo();

		newVideo.setId(videoApi.getId());
		newVideo.setName(videoApi.getName());
		newVideo.setUser(user);
		newVideo.setActive(Boolean.TRUE);
		newVideo.setCreated(LocalDateTime.now());

		YoutubeVideo saved = youtubeVideosRepository.save(newVideo);

		return ResponseEntity.status(HttpStatus.OK).body(new YoutubeVideoApi(saved));
	}

	@Operation(summary = "Update video")
	@SecurityRequirement(name = "Bearer Authentication")
	@PutMapping(path = "/{id}")
	public ResponseEntity<?> addVideo(@PathVariable String id) {
		if (id.length() != 11) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Youtube video ID is incorrect!");
		}

		User user = getCurrentUser();

		if (!user.getRoles().stream().map(Role::getName).anyMatch(ROLES::contains)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission!");
		}

		Optional<YoutubeVideo> oldVideo = youtubeVideosRepository.findById(id);

		if (!oldVideo.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video with current ID is not exist");
		}

		YoutubeVideo video = new YoutubeVideo();

		video.setId(id);
		video.setActive(true);
		video.setOrder(0);
		video.setUser(oldVideo.get().getUser());
		video.setCreated(oldVideo.get().getCreated());

		YoutubeVideo saved = youtubeVideosRepository.save(video);

		return ResponseEntity.status(HttpStatus.OK).body(new YoutubeVideoApi(saved));
	}

	private User getCurrentUser() {
		SecurityContext context = SecurityContextHolder.getContext();
		String userName = context.getAuthentication().getName();
		return userRepository.findByEmailOrUsername(userName, userName)
			.orElseThrow(() -> new UsernameNotFoundException(userName));
	}
}
