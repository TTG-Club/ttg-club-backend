package club.dnd5.portal.controller.api.menu;

import club.dnd5.portal.config.EtagConfig;
import club.dnd5.portal.config.WebSecurityConfig;
import club.dnd5.portal.controller.api.ApiExceptionHandler;
import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.model.Menu;
import club.dnd5.portal.repository.MenuRepository;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Меню отдаётся с ETag и Cache-Control: no-cache, а неизменившееся меню приходит как 304.
 * Фильтры стоят в том же порядке, что и в приложении: Spring Security снаружи, ETag внутри.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = MenuApiControllerEtagTest.TestConfig.class)
@TestPropertySource(properties = "allowed-origin-patterns=*")
class MenuApiControllerEtagTest {
	private static final String URL = "/api/v1/menu";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	@Qualifier("springSecurityFilterChain")
	private Filter securityFilter;

	@Autowired
	private MenuRepository menuRepository;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		Menu child = new Menu();
		child.setName("Классы");
		child.setUrl("/classes");
		child.setChildren(List.of());

		Menu group = new Menu();
		group.setName("Персонаж");
		group.setChildren(List.of(child));

		when(menuRepository.findAll()).thenReturn(List.of(group));

		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.addFilters(securityFilter, new EtagConfig().etagFilter().getFilter())
			.build();
	}

	@Test
	void returnsMenuWithEtagAndRevalidatingCache() throws Exception {
		mockMvc.perform(get(URL))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Персонаж")))
			.andExpect(header().exists(HttpHeaders.ETAG))
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache"))
			.andExpect(header().string(HttpHeaders.CACHE_CONTROL, not(containsString("no-store"))));
	}

	@Test
	void unchangedMenuIsNotModified() throws Exception {
		String etag = mockMvc.perform(get(URL))
			.andReturn()
			.getResponse()
			.getHeader(HttpHeaders.ETAG);

		mockMvc.perform(get(URL).header(HttpHeaders.IF_NONE_MATCH, etag))
			.andExpect(status().isNotModified())
			.andExpect(content().string(""));
	}

	@Test
	void changedMenuIsSentAgain() throws Exception {
		mockMvc.perform(get(URL).header(HttpHeaders.IF_NONE_MATCH, "\"stale\""))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Персонаж")));
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
		MenuRepository menuRepository() {
			return mock(MenuRepository.class);
		}

		@Bean
		MenuApiController menuApiController(MenuRepository menuRepository) {
			return new MenuApiController(menuRepository);
		}
	}
}
