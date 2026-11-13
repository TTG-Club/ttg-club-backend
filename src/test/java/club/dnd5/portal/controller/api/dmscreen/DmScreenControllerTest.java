package club.dnd5.portal.controller.api.dmscreen;

import club.dnd5.portal.config.WebSecurityConfig;
import club.dnd5.portal.controller.api.ApiExceptionHandler;
import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.model.dmscreen.DmScreen;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.dmscreen.DmScreenRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import club.dnd5.portal.service.dmscreen.DmScreenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Контракт HTTP API ширм: только авторизованным, форма JSON (id строкой, isMinimized, ISO-даты),
 * коды ответов.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = DmScreenControllerTest.TestConfig.class)
@TestPropertySource(properties = "allowed-origin-patterns=*")
class DmScreenControllerTest {
	private static final String URL = "/api/v1/dm-screens";
	private static final String WINDOW_JSON = "{\"id\":\"w1\",\"path\":\"/bestiary/goblin\",\"title\":\"Гоблин\","
			+ "\"x\":10.5,\"y\":20,\"width\":400,\"height\":300,\"layer\":2,\"isMinimized\":true}";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private DmScreenRepository screenRepository;

	@Autowired
	private UserRepository userRepository;

	private MockMvc mockMvc;
	private DmScreen screen;

	@BeforeEach
	void setUp() {
		reset(screenRepository, userRepository);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		User user = new User();
		user.setId(7L);
		user.setUsername("magistr");
		when(userRepository.findByEmailOrUsername("magistr", "magistr")).thenReturn(Optional.of(user));

		screen = new DmScreen();
		screen.setId(UUID.randomUUID());
		screen.setUser(user);
		screen.setName("Подземелье");
		screen.setWindows("[" + WINDOW_JSON + "]");
		screen.setUpdatedAt(Instant.parse("2026-11-12T10:15:30.123Z"));
		when(screenRepository.findByIdAndUserId(any(), anyLong())).thenReturn(Optional.empty());
		when(screenRepository.findByIdAndUserId(screen.getId(), 7L)).thenReturn(Optional.of(screen));
		when(screenRepository.findAllByUserIdOrderByUpdatedAtDesc(7L)).thenReturn(List.of(screen));
		when(screenRepository.save(any(DmScreen.class))).thenAnswer(invocation -> {
			DmScreen saved = invocation.getArgument(0);
			saved.setId(UUID.randomUUID());
			return saved;
		});
	}

	@Test
	void anonymousGetsUnauthorized() throws Exception {
		mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
		mockMvc.perform(get(URL + "/" + screen.getId())).andExpect(status().isUnauthorized());
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(patch(URL + "/" + screen.getId()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"x\"}")).andExpect(status().isUnauthorized());
		mockMvc.perform(put(URL + "/" + screen.getId() + "/windows").contentType(MediaType.APPLICATION_JSON)
				.content("[]")).andExpect(status().isUnauthorized());
		mockMvc.perform(delete(URL + "/" + screen.getId())).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void listHasCountsAndLimit() throws Exception {
		mockMvc.perform(get(URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.used").value(1))
				.andExpect(jsonPath("$.limit").value(1))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].id").value(screen.getId().toString()))
				.andExpect(jsonPath("$.items[0].name").value("Подземелье"))
				.andExpect(jsonPath("$.items[0].windowCount").value(1))
				.andExpect(jsonPath("$.items[0].updatedAt").value("2026-11-12T10:15:30.123Z"));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "SUBSCRIBER")
	void subscriberLimitIsTheSame() throws Exception {
		mockMvc.perform(get(URL)).andExpect(jsonPath("$.limit").value(1));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void screenJsonMatchesContract() throws Exception {
		mockMvc.perform(get(URL + "/" + screen.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(screen.getId().toString()))
				.andExpect(jsonPath("$.updatedAt").value("2026-11-12T10:15:30.123Z"))
				.andExpect(jsonPath("$.windows[0].id").value("w1"))
				.andExpect(jsonPath("$.windows[0].path").value("/bestiary/goblin"))
				.andExpect(jsonPath("$.windows[0].title").value("Гоблин"))
				.andExpect(jsonPath("$.windows[0].x").value(10.5))
				.andExpect(jsonPath("$.windows[0].layer").value(2))
				.andExpect(jsonPath("$.windows[0].isMinimized").value(true))
				.andExpect(jsonPath("$.windows[0]", not(hasKey("minimized"))));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void foreignAndMalformedIdsAreNotFound() throws Exception {
		mockMvc.perform(get(URL + "/" + UUID.randomUUID())).andExpect(status().isNotFound());
		mockMvc.perform(get(URL + "/not-a-uuid")).andExpect(status().isNotFound());
		mockMvc.perform(delete(URL + "/" + UUID.randomUUID())).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void createReturnsScreenAndForbidsOverLimit() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\" Новая \",\"windows\":[" + WINDOW_JSON + "]}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Новая"))
				.andExpect(jsonPath("$.windows[0].isMinimized").value(true));

		when(screenRepository.countByUserId(7L)).thenReturn(1L);
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Вторая\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").isNotEmpty());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void invalidBodiesAreBadRequest() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(patch(URL + "/" + screen.getId()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"" + "я".repeat(65) + "\"}")).andExpect(status().isBadRequest());
		mockMvc.perform(put(URL + "/" + screen.getId() + "/windows").contentType(MediaType.APPLICATION_JSON)
				.content("[" + WINDOW_JSON.replace("/bestiary", "bestiary") + "]")).andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void renameReplaceWindowsAndDelete() throws Exception {
		mockMvc.perform(patch(URL + "/" + screen.getId()).contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Логово\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Логово"));
		mockMvc.perform(put(URL + "/" + screen.getId() + "/windows").contentType(MediaType.APPLICATION_JSON)
						.content("[" + WINDOW_JSON + "," + WINDOW_JSON.replace("w1", "w2") + "]"))
				.andExpect(status().isNoContent());
		mockMvc.perform(get(URL)).andExpect(jsonPath("$.items[0].windowCount").value(2));
		mockMvc.perform(delete(URL + "/" + screen.getId())).andExpect(status().isNoContent());
	}

	@Configuration
	@EnableWebMvc
	@Import(WebSecurityConfig.class)
	static class TestConfig {
		@Bean
		JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
			return new JwtAuthenticationEntryPoint();
		}

		@Bean
		ExternalAuthClient externalAuthClient() {
			return mock(ExternalAuthClient.class);
		}

		@Bean
		ExternalAuthUserSynchronizer externalAuthUserSynchronizer() {
			return mock(ExternalAuthUserSynchronizer.class);
		}

		@Bean
		RedirectToLowerCaseInterceptor redirectToLowerCaseInterceptor() {
			return new RedirectToLowerCaseInterceptor();
		}

		@Bean
		ApiExceptionHandler apiExceptionHandler() {
			return new ApiExceptionHandler();
		}

		@Bean
		DmScreenRepository dmScreenRepository() {
			return mock(DmScreenRepository.class);
		}

		@Bean
		UserRepository userRepository() {
			return mock(UserRepository.class);
		}

		@Bean
		DmScreenService dmScreenService(DmScreenRepository screenRepository, UserRepository userRepository) {
			return new DmScreenService(screenRepository, userRepository, new JsonMapper());
		}

		@Bean
		DmScreenController dmScreenController(DmScreenService service) {
			return new DmScreenController(service);
		}
	}
}
