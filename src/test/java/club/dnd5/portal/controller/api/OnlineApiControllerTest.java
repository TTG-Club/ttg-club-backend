package club.dnd5.portal.controller.api;

import club.dnd5.portal.config.WebSecurityConfig;
import club.dnd5.portal.dto.online.OnlineUsageRequest;
import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import club.dnd5.portal.service.OnlineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Отметки о времени работы с инструментом: принимаются без входа, отвечают 204 без тела,
 * невалидное тело — 400 и в online-service не уходит.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = OnlineApiControllerTest.TestConfig.class)
@TestPropertySource(properties = "allowed-origin-patterns=*")
class OnlineApiControllerTest {
	private static final String USAGE_URI = "/api/online/usage";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private OnlineService onlineService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		reset(onlineService);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	void anonymousUsageIsForwardedAndAnswersNoContent() throws Exception {
		mockMvc.perform(post(USAGE_URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"feature\":\"dm-screen\",\"key\":\"guest-uuid\",\"type\":\"GUEST\",\"activeSeconds\":60}"))
			.andExpect(status().isNoContent())
			.andExpect(content().string(""));

		ArgumentCaptor<OnlineUsageRequest> captor = ArgumentCaptor.forClass(OnlineUsageRequest.class);
		verify(onlineService).usage(captor.capture());
		assertEquals("dm-screen", captor.getValue().getFeature());
		assertEquals("guest-uuid", captor.getValue().getKey());
		assertEquals("GUEST", captor.getValue().getType());
		assertEquals(60, captor.getValue().getActiveSeconds());
	}

	@Test
	void boundaryActiveSecondsAreAccepted() throws Exception {
		for (int seconds : new int[] {0, 300}) {
			mockMvc.perform(post(USAGE_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(body("dm-screen", "user", "REGISTERED", String.valueOf(seconds))))
				.andExpect(status().isNoContent());
		}
	}

	@Test
	void invalidBodyIsRejected() throws Exception {
		String longFeature = "f".repeat(33);
		String longKey = "k".repeat(129);

		for (String invalid : new String[] {
				"{\"key\":\"user\",\"type\":\"GUEST\",\"activeSeconds\":60}",
				"{\"feature\":\"dm-screen\",\"type\":\"GUEST\",\"activeSeconds\":60}",
				"{\"feature\":\"dm-screen\",\"key\":\"user\",\"activeSeconds\":60}",
				"{\"feature\":\"dm-screen\",\"key\":\"user\",\"type\":\"GUEST\"}",
				body(" ", "user", "GUEST", "60"),
				body(longFeature, "user", "GUEST", "60"),
				body("dm-screen", longKey, "GUEST", "60"),
				body("dm-screen", "user", "ADMIN", "60"),
				body("dm-screen", "user", "GUEST", "-1"),
				body("dm-screen", "user", "GUEST", "301")
		}) {
			mockMvc.perform(post(USAGE_URI)
					.contentType(MediaType.APPLICATION_JSON)
					.content(invalid))
				.andExpect(status().isBadRequest());
		}

		verify(onlineService, never()).usage(any());
	}

	private static String body(String feature, String key, String type, String activeSeconds) {
		return "{\"feature\":\"" + feature + "\",\"key\":\"" + key + "\",\"type\":\"" + type
			+ "\",\"activeSeconds\":" + activeSeconds + "}";
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
		OnlineService onlineService() {
			return mock(OnlineService.class);
		}

		@Bean
		OnlineApiController onlineApiController(OnlineService onlineService) {
			return new OnlineApiController(onlineService);
		}
	}
}
