package club.dnd5.portal.controller.api.charactersheet;

import club.dnd5.portal.config.WebSecurityConfig;
import club.dnd5.portal.controller.api.ApiExceptionHandler;
import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.model.charactersheet.CharacterSheet;
import club.dnd5.portal.model.charactersheet.SavedCharacterSheet;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.charactersheet.CharacterSheetRepository;
import club.dnd5.portal.repository.charactersheet.SavedCharacterSheetRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import club.dnd5.portal.service.charactersheet.CharacterSheetLimits;
import club.dnd5.portal.service.charactersheet.CharacterSheetService;
import club.dnd5.portal.service.charactersheet.CharacterSheetShareService;
import club.dnd5.portal.service.image.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Доступ к листу по ссылке и «Другие листы»: ссылка открывает лист и гостю, отзыв её гасит,
 * сохранённые записи становятся недоступными, лимит зависит от подписки.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = CharacterSheetShareControllerTest.TestConfig.class)
@TestPropertySource(properties = "allowed-origin-patterns=*")
class CharacterSheetShareControllerTest {
	private static final String URL = "/api/v1/character-sheets";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private CharacterSheetRepository sheetRepository;

	@Autowired
	private SavedCharacterSheetRepository savedRepository;

	@Autowired
	private UserRepository userRepository;

	private MockMvc mockMvc;
	private User owner;
	private User viewer;
	private CharacterSheet sheet;
	private UUID token;

	@BeforeEach
	void setUp() {
		reset(sheetRepository, savedRepository, userRepository);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		owner = user(7L, "magistr");
		viewer = user(8L, "guest");
		token = UUID.randomUUID();

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("level", 3);
		sheet = new CharacterSheet();
		sheet.setId(UUID.randomUUID());
		sheet.setUser(owner);
		sheet.setName("Торин");
		sheet.setData(data);
		sheet.setVersion(4L);
		sheet.setShareToken(token);
		sheet.setCreatedAt(Instant.parse("2026-11-20T10:00:00.000Z"));
		sheet.setUpdatedAt(Instant.parse("2026-11-22T10:15:30.123Z"));

		when(sheetRepository.findByIdAndUserIdAndDeletedAtIsNull(any(), anyLong())).thenReturn(Optional.empty());
		when(sheetRepository.findByIdAndUserIdAndDeletedAtIsNull(sheet.getId(), 7L)).thenReturn(Optional.of(sheet));
		when(sheetRepository.findByShareTokenAndDeletedAtIsNull(any())).thenReturn(Optional.empty());
		when(sheetRepository.findByShareTokenAndDeletedAtIsNull(token)).thenReturn(Optional.of(sheet));
		when(savedRepository.findByUserIdAndSheetId(anyLong(), any())).thenReturn(Optional.empty());
		when(savedRepository.findByIdAndUserId(any(), anyLong())).thenReturn(Optional.empty());
		when(savedRepository.saveAndFlush(any(SavedCharacterSheet.class))).thenAnswer(invocation -> {
			SavedCharacterSheet saved = invocation.getArgument(0);
			if (saved.getId() == null) {
				saved.setId(UUID.randomUUID());
			}
			return saved;
		});
	}

	private User user(Long id, String login) {
		User user = new User();
		user.setId(id);
		user.setUsername(login);
		user.setRoles(new ArrayList<>(List.of(new Role("USER"))));
		when(userRepository.findByEmailOrUsername(login, login)).thenReturn(Optional.of(user));
		return user;
	}

	@Test
	void guestOpensSharedSheet() throws Exception {
		mockMvc.perform(get(URL + "/shared/" + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(sheet.getId().toString()))
				.andExpect(jsonPath("$.name").value("Торин"))
				.andExpect(jsonPath("$.data.level").value(3))
				.andExpect(jsonPath("$.own").value(false))
				.andExpect(jsonPath("$.savedId").doesNotExist());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void deletedSheetMakesSavedUnavailable() throws Exception {
		SavedCharacterSheet saved = new SavedCharacterSheet();
		saved.setId(UUID.randomUUID());
		saved.setUserId(8L);
		saved.setSheetId(sheet.getId());
		saved.setShareToken(token);
		saved.setName("Торин");
		saved.setCreatedAt(Instant.now());
		sheet.setDeletedAt(Instant.now());
		when(savedRepository.findAllByUserIdOrderByCreatedAtDesc(8L)).thenReturn(List.of(saved));
		when(sheetRepository.findAllByIdIn(List.of(sheet.getId()))).thenReturn(List.of(sheet));

		mockMvc.perform(get(URL + "/saved"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].available").value(false));
	}

	@Test
	void unknownOrBrokenTokenIsNotFound() throws Exception {
		mockMvc.perform(get(URL + "/shared/" + UUID.randomUUID())).andExpect(status().isNotFound());
		mockMvc.perform(get(URL + "/shared/not-a-token")).andExpect(status().isNotFound());
	}

	@Test
	void guestCannotShareOrSave() throws Exception {
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/share")).andExpect(status().isUnauthorized());
		mockMvc.perform(get(URL + "/saved")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void shareKeepsExistingToken() throws Exception {
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/share"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.shareToken").value(token.toString()));
		verify(sheetRepository, never()).updateShareToken(any(), any());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void shareIssuesNewTokenWithoutTouchingVersion() throws Exception {
		sheet.setShareToken(null);

		mockMvc.perform(post(URL + "/" + sheet.getId() + "/share")).andExpect(status().isOk());

		ArgumentCaptor<UUID> issued = ArgumentCaptor.forClass(UUID.class);
		verify(sheetRepository).updateShareToken(eq(sheet.getId()), issued.capture());
		assertThat(issued.getValue()).isNotNull();
		verify(sheetRepository, never()).saveAndFlush(any());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void unshareClearsToken() throws Exception {
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/share")).andExpect(status().isNoContent());
		verify(sheetRepository).updateShareToken(eq(sheet.getId()), isNull());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void foreignSheetCannotBeShared() throws Exception {
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/share")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void saveForeignSheetByLink() throws Exception {
		mockMvc.perform(post(URL + "/saved").contentType(MediaType.APPLICATION_JSON)
						.content("{\"shareToken\":\"" + token + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sheetId").value(sheet.getId().toString()))
				.andExpect(jsonPath("$.available").value(true))
				.andExpect(jsonPath("$.data.level").value(3));

		ArgumentCaptor<SavedCharacterSheet> saved = ArgumentCaptor.forClass(SavedCharacterSheet.class);
		verify(savedRepository).saveAndFlush(saved.capture());
		assertThat(saved.getValue().getUserId()).isEqualTo(8L);
		assertThat(saved.getValue().getShareToken()).isEqualTo(token);
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void ownSheetIsNotSaved() throws Exception {
		mockMvc.perform(post(URL + "/saved").contentType(MediaType.APPLICATION_JSON)
						.content("{\"shareToken\":\"" + token + "\"}"))
				.andExpect(status().isBadRequest());
		verify(savedRepository, never()).saveAndFlush(any());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void savedLimitDependsOnSubscription() throws Exception {
		when(savedRepository.countByUserId(8L)).thenReturn((long) CharacterSheetLimits.SAVED);

		mockMvc.perform(post(URL + "/saved").contentType(MediaType.APPLICATION_JSON)
						.content("{\"shareToken\":\"" + token + "\"}"))
				.andExpect(status().isForbidden());

		viewer.getRoles().add(new Role(CharacterSheetLimits.SUBSCRIBER_ROLE));

		mockMvc.perform(post(URL + "/saved").contentType(MediaType.APPLICATION_JSON)
						.content("{\"shareToken\":\"" + token + "\"}"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void revokedLinkMakesSavedUnavailable() throws Exception {
		SavedCharacterSheet saved = new SavedCharacterSheet();
		saved.setId(UUID.randomUUID());
		saved.setUserId(8L);
		saved.setSheetId(sheet.getId());
		saved.setShareToken(UUID.randomUUID());
		saved.setName("Старое имя");
		saved.setCreatedAt(Instant.now());
		when(savedRepository.findAllByUserIdOrderByCreatedAtDesc(8L)).thenReturn(List.of(saved));
		when(sheetRepository.findAllByIdIn(List.of(sheet.getId()))).thenReturn(List.of(sheet));

		mockMvc.perform(get(URL + "/saved"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].available").value(false))
				.andExpect(jsonPath("$.items[0].name").value("Старое имя"))
				.andExpect(jsonPath("$.items[0].data").doesNotExist())
				.andExpect(jsonPath("$.used").value(1))
				.andExpect(jsonPath("$.limit").value(CharacterSheetLimits.SAVED))
				.andExpect(jsonPath("$.subscriberLimit").value(CharacterSheetLimits.SUBSCRIBER_SAVED));
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void viewerSeesWhetherSheetIsSaved() throws Exception {
		SavedCharacterSheet saved = new SavedCharacterSheet();
		saved.setId(UUID.randomUUID());
		when(savedRepository.findByUserIdAndSheetId(8L, sheet.getId())).thenReturn(Optional.of(saved));

		mockMvc.perform(get(URL + "/shared/" + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.savedId").value(saved.getId().toString()));
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void removeForeignSavedIsNotFound() throws Exception {
		mockMvc.perform(delete(URL + "/saved/" + UUID.randomUUID())).andExpect(status().isNotFound());
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
		CharacterSheetRepository characterSheetRepository() {
			return mock(CharacterSheetRepository.class);
		}

		@Bean
		SavedCharacterSheetRepository savedCharacterSheetRepository() {
			return mock(SavedCharacterSheetRepository.class);
		}

		@Bean
		UserRepository userRepository() {
			return mock(UserRepository.class);
		}

		@Bean
		CharacterSheetService characterSheetService(CharacterSheetRepository sheetRepository,
													UserRepository userRepository) {
			return new CharacterSheetService(sheetRepository, userRepository, new JsonMapper(),
					mock(ImageStorageService.class));
		}

		@Bean
		CharacterSheetShareService characterSheetShareService(CharacterSheetService sheetService,
															  CharacterSheetRepository sheetRepository,
															  SavedCharacterSheetRepository savedRepository) {
			return new CharacterSheetShareService(sheetService, sheetRepository, savedRepository);
		}

		@Bean
		CharacterSheetShareController characterSheetShareController(CharacterSheetShareService service) {
			return new CharacterSheetShareController(service);
		}
	}
}
