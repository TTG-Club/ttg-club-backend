package club.dnd5.portal.controller.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

/**
 * Отказ метода безопасности не должен превращаться в 500 общим {@code @ExceptionHandler(Exception.class)}:
 * иначе Spring Security не увидит {@link AccessDeniedException}, фронт не получит 401 и не обновит токен.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApiExceptionHandlerSecurityTest.TestConfig.class)
class ApiExceptionHandlerSecurityTest {
	@Autowired
	private SecuredTestController controller;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void accessDeniedIsPropagatedToSpringSecurityInsteadOfBecoming500() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
			"key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(new ApiExceptionHandler())
			.build();

		assertThatThrownBy(() -> mockMvc.perform(patch("/api/v1/workshop/test")
			.contentType(MediaType.APPLICATION_JSON)))
			.hasRootCauseInstanceOf(AccessDeniedException.class);
	}

	@Test
	void allowedRequestIsNotAffected() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(
			new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
				"admin", null, AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.setControllerAdvice(new ApiExceptionHandler())
			.build();

		int status = mockMvc.perform(patch("/api/v1/workshop/test")
				.contentType(MediaType.APPLICATION_JSON))
			.andReturn()
			.getResponse()
			.getStatus();

		assertThat(status).isEqualTo(200);
	}

	@RestController
	static class SecuredTestController {
		@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
		@PatchMapping("/api/v1/workshop/test")
		public String update() {
			return "ok";
		}
	}

	@Configuration
	@EnableGlobalMethodSecurity(prePostEnabled = true)
	static class TestConfig {
		@Bean
		SecuredTestController securedTestController() {
			return new SecuredTestController();
		}
	}
}
