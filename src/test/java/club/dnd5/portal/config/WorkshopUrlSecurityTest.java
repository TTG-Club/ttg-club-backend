package club.dnd5.portal.config;

import club.dnd5.portal.controller.api.ApiExceptionHandler;
import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Права на мастерскую проверяются до разбора тела запроса: аноним не должен получать 400
 * с результатом валидации своих данных — только 401.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WorkshopUrlSecurityTest.TestConfig.class)
@TestPropertySource(properties = "allowed-origin-patterns=*")
class WorkshopUrlSecurityTest {
	private static final String URL = "/api/v1/workshop/test";
	private static final String INVALID_BODY = "{\"name\":\"\"}";
	private static final String VALID_BODY = "{\"name\":\"Оперативник\"}";

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	void anonymousGetsUnauthorizedBeforeValidation() throws Exception {
		mockMvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content(INVALID_BODY))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void regularUserGetsForbiddenBeforeValidation() throws Exception {
		mockMvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content(INVALID_BODY))
			.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "MODERATOR")
	void moderatorGetsValidationError() throws Exception {
		mockMvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content(INVALID_BODY))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "MODERATOR")
	void moderatorCanSave() throws Exception {
		mockMvc.perform(patch(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
			.andExpect(status().isOk());
	}

	@Getter
	@Setter
	static class TestRequest {
		@NotBlank
		private String name;
	}

	@RestController
	static class WorkshopTestController {
		@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
		@PatchMapping(URL)
		public String update(@Valid @RequestBody TestRequest request) {
			return "ok";
		}
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
		WorkshopTestController workshopTestController() {
			return new WorkshopTestController();
		}
	}
}
