package club.dnd5.portal.service.dmscreen;

import club.dnd5.portal.dto.api.dmscreen.DmScreenListResponse;
import club.dnd5.portal.dto.api.dmscreen.DmScreenResponse;
import club.dnd5.portal.dto.api.dmscreen.DmScreenWindow;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.dmscreen.DmScreen;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.dmscreen.DmScreenRepository;
import club.dnd5.portal.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DmScreenServiceTest {
	private static final long USER_ID = 7L;

	private final DmScreenRepository screenRepository = mock(DmScreenRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final DmScreenService service = new DmScreenService(screenRepository, userRepository, new JsonMapper());

	private User user;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(USER_ID);
		user.setUsername("magistr");
		when(userRepository.findByEmailOrUsername("magistr", "magistr")).thenReturn(Optional.of(user));
		when(screenRepository.save(any(DmScreen.class))).thenAnswer(invocation -> {
			DmScreen screen = invocation.getArgument(0);
			screen.setId(UUID.randomUUID());
			return screen;
		});
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void freeUserHasLimitOfOne() {
		signIn("ROLE_USER");
		when(screenRepository.findAllByUserIdOrderByUpdatedAtDesc(USER_ID)).thenReturn(Collections.emptyList());

		DmScreenListResponse list = service.findMine();

		assertThat(list.getLimit()).isEqualTo(1);
		assertThat(list.getUsed()).isZero();
		assertThat(list.getItems()).isEmpty();
	}

	@Test
	void subscriberAndAdminHaveLimitOfThirty() {
		signIn("ROLE_USER", "ROLE_SUBSCRIBER");
		assertThat(service.currentLimit()).isEqualTo(30);

		signIn("ROLE_ADMIN");
		assertThat(service.currentLimit()).isEqualTo(30);

		signIn("ROLE_MODERATOR");
		assertThat(service.currentLimit()).isEqualTo(1);
	}

	@Test
	void freeUserCanCreateFirstScreen() {
		signIn("ROLE_USER");
		when(screenRepository.countByUserId(USER_ID)).thenReturn(0L);

		DmScreenResponse response = service.create("  Подземелье  ", List.of(window("w1")));

		ArgumentCaptor<DmScreen> saved = ArgumentCaptor.forClass(DmScreen.class);
		verify(screenRepository).save(saved.capture());
		assertThat(saved.getValue().getUser()).isSameAs(user);
		assertThat(response.getName()).isEqualTo("Подземелье");
		assertThat(response.getId()).isEqualTo(saved.getValue().getId().toString());
		assertThat(response.getWindows()).hasSize(1);
		assertThat(response.getWindows().get(0).getIsMinimized()).isFalse();
		assertThat(Instant.parse(response.getUpdatedAt())).isNotNull();
	}

	@Test
	void createWithoutWindowsStoresEmptyLayout() {
		signIn("ROLE_USER");

		DmScreenResponse response = service.create("Ширма", null);

		assertThat(response.getWindows()).isEmpty();
	}

	@Test
	void freeUserGetsForbiddenOverLimit() {
		signIn("ROLE_USER");
		when(screenRepository.countByUserId(USER_ID)).thenReturn(1L);

		assertThatThrownBy(() -> service.create("Вторая", null))
				.isInstanceOfSatisfying(ApiException.class, e -> {
					assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
					assertThat(e.getMessage()).contains("подписк");
				});
		verify(screenRepository, never()).save(any());
	}

	@Test
	void subscriberGetsForbiddenOverThirty() {
		signIn("ROLE_SUBSCRIBER");
		when(screenRepository.countByUserId(USER_ID)).thenReturn(29L);
		service.create("Тридцатая", null);

		when(screenRepository.countByUserId(USER_ID)).thenReturn(30L);
		assertThatThrownBy(() -> service.create("Тридцать первая", null))
				.isInstanceOfSatisfying(ApiException.class,
						e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN));
	}

	@Test
	void screensOverLimitStayAvailableAfterSubscriptionEnds() {
		signIn("ROLE_USER");
		DmScreen first = screen("Первая", List.of(window("a"), window("b")));
		DmScreen second = screen("Вторая", Collections.emptyList());
		when(screenRepository.findAllByUserIdOrderByUpdatedAtDesc(USER_ID)).thenReturn(List.of(first, second));
		when(screenRepository.findByIdAndUserId(second.getId(), USER_ID)).thenReturn(Optional.of(second));

		DmScreenListResponse list = service.findMine();

		assertThat(list.getUsed()).isEqualTo(2);
		assertThat(list.getLimit()).isEqualTo(1);
		assertThat(list.getItems()).extracting("name").containsExactly("Первая", "Вторая");
		assertThat(list.getItems()).extracting("windowCount").containsExactly(2, 0);
		assertThat(list.getItems().get(0).getId()).isEqualTo(first.getId().toString());
		assertThat(service.findById(second.getId()).getName()).isEqualTo("Вторая");
	}

	@Test
	void foreignOrMissingScreenIsNotFound() {
		signIn("ROLE_USER");
		UUID foreign = UUID.randomUUID();
		when(screenRepository.findByIdAndUserId(foreign, USER_ID)).thenReturn(Optional.empty());

		assertNotFound(() -> service.findById(foreign));
		assertNotFound(() -> service.rename(foreign, "Имя"));
		assertNotFound(() -> service.replaceWindows(foreign, Collections.emptyList()));
		assertNotFound(() -> service.delete(foreign));
		verify(screenRepository, never()).delete(any());
	}

	@Test
	void renameTrimsNameAndTouchesUpdatedAt() {
		signIn("ROLE_USER");
		DmScreen screen = screen("Старое", Collections.emptyList());
		Instant before = screen.getUpdatedAt();
		when(screenRepository.findByIdAndUserId(screen.getId(), USER_ID)).thenReturn(Optional.of(screen));

		DmScreenResponse response = service.rename(screen.getId(), "  Новое  ");

		assertThat(response.getName()).isEqualTo("Новое");
		assertThat(screen.getUpdatedAt()).isAfter(before);
	}

	@Test
	void rejectsInvalidNames() {
		signIn("ROLE_USER");

		assertBadRequest(() -> service.create("   ", null));
		assertBadRequest(() -> service.create(null, null));
		assertBadRequest(() -> service.create("я".repeat(65), null));
		assertThat(service.create("  " + "я".repeat(64) + "  ", null).getName()).hasSize(64);
	}

	@Test
	void replaceWindowsStoresLayoutAndTouchesUpdatedAt() {
		signIn("ROLE_USER");
		DmScreen screen = screen("Ширма", Collections.emptyList());
		Instant before = screen.getUpdatedAt();
		when(screenRepository.findByIdAndUserId(screen.getId(), USER_ID)).thenReturn(Optional.of(screen));

		service.replaceWindows(screen.getId(), List.of(window("a"), window("b"), window("c")));

		assertThat(screen.getUpdatedAt()).isAfter(before);
		assertThat(screen.getWindows()).contains("\"isMinimized\":false").doesNotContain("\"minimized\"");
		assertThat(service.findById(screen.getId()).getWindows()).extracting("id").containsExactly("a", "b", "c");
	}

	@Test
	void rejectsInvalidWindows() {
		signIn("ROLE_USER");
		DmScreen screen = screen("Ширма", Collections.emptyList());
		UUID id = screen.getId();
		when(screenRepository.findByIdAndUserId(id, USER_ID)).thenReturn(Optional.of(screen));

		List<DmScreenWindow> tooMany = new ArrayList<>();
		for (int i = 0; i < 101; i++) {
			tooMany.add(window("w" + i));
		}
		assertBadRequest(() -> service.replaceWindows(id, tooMany));
		assertBadRequest(() -> service.create("Ширма", tooMany));

		assertBadRequest(() -> service.replaceWindows(id, null));
		assertBadRequest(() -> service.replaceWindows(id, Collections.singletonList(null)));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setPath("bestiary/goblin")))));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setPath("/" + "a".repeat(256))))));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setTitle("т".repeat(257))))));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setTitle(null)))));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setId(" ")))));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setX(null)))));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setWidth(Double.NaN)))));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setLayer(null)))));
		assertBadRequest(() -> service.replaceWindows(id, List.of(with(window("a"), w -> w.setIsMinimized(null)))));

		List<DmScreenWindow> hundred = new ArrayList<>(tooMany.subList(0, 100));
		hundred.set(0, with(window("max"), w -> {
			w.setPath("/" + "a".repeat(255));
			w.setTitle("т".repeat(256));
		}));
		service.replaceWindows(id, hundred);
		assertThat(service.findById(id).getWindows()).hasSize(100);
	}

	@Test
	void deleteRemovesOwnScreen() {
		signIn("ROLE_USER");
		DmScreen screen = screen("Ширма", Collections.emptyList());
		when(screenRepository.findByIdAndUserId(screen.getId(), USER_ID)).thenReturn(Optional.of(screen));

		service.delete(screen.getId());

		verify(screenRepository).delete(screen);
	}

	@Test
	void guestIsUnauthorized() {
		assertThatThrownBy(service::findMine)
				.isInstanceOfSatisfying(ApiException.class,
						e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
	}

	private void signIn(String... authorities) {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				"magistr", null, AuthorityUtils.createAuthorityList(authorities)));
	}

	private DmScreen screen(String name, List<DmScreenWindow> windows) {
		DmScreen screen = new DmScreen();
		screen.setId(UUID.randomUUID());
		screen.setUser(user);
		screen.setName(name);
		screen.setWindows(new JsonMapper().writeValueAsString(windows));
		screen.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
		return screen;
	}

	static DmScreenWindow window(String id) {
		return new DmScreenWindow(id, "/bestiary/goblin", "Гоблин", 10.5, 20.0, 400.0, 300.0, 1, false);
	}

	private static DmScreenWindow with(DmScreenWindow window, java.util.function.Consumer<DmScreenWindow> change) {
		change.accept(window);
		return window;
	}

	private static void assertNotFound(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
		assertThatThrownBy(call).isInstanceOfSatisfying(ApiException.class,
				e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
	}

	private static void assertBadRequest(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
		assertThatThrownBy(call).isInstanceOfSatisfying(ApiException.class,
				e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
	}
}
