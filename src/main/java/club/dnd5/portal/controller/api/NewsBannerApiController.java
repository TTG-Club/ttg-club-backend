package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.NewsBannerApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.NewsBanner;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.NewsBannerRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

@RequiredArgsConstructor
@Tag(name = "News Banners", description = "API для News Banners")
@RestController
@RequestMapping(value = "/api/v1/banners")
public class NewsBannerApiController {
	private static final Set<String> ROLES = new HashSet<>(Arrays.asList("MODERATOR", "ADMIN"));

	private final UserRepository userRepository;

	private final NewsBannerRepository newsBannerRepository;

	@Operation(summary = "Получение списка баннеров")
	@GetMapping
	public ResponseEntity<Page<NewsBannerApi>> getNewsBanners(
		@RequestParam(required = false) String search,
		@RequestParam(required = false, defaultValue = "0") final Integer page,
		@RequestParam(required = false, defaultValue = "-1") final Integer size,
		@RequestParam(required = false) List<String> order,
		@RequestParam(required = false) Boolean activeStatus
	) {
		Specification<NewsBanner> specification = null;

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

		Page<NewsBanner> news = newsBannerRepository.findAll(specification, pageable);

		return ResponseEntity
			.status(HttpStatus.OK)
			.body(news.map(NewsBannerApi::new));
	}

	@Operation(summary = "Получение новостного Баннера по статусу")
	@GetMapping("/active")
	public ResponseEntity <NewsBannerApi> getNewsBanner(@RequestParam boolean active) {
		NewsBanner newsBanner = newsBannerRepository.findByActive(active).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(new NewsBannerApi(newsBanner));
	}

	@Operation(summary = "Добавление новостного баннера")
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

	@Operation(summary = "Обновление новостного баннера")
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
			.createdAt(LocalDateTime.now())
			.build();
		return ResponseEntity.status(HttpStatus.OK).body(newsBannerRepository.save(newNewsBanner));
	}

	@Operation(summary = "Изменение статуса для новостного баннера")
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
		if (newNewsBanner.isActive() == Boolean.TRUE.equals(activeStatus)) {
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
