package club.dnd5.portal.controller.api.charactersheet;

import club.dnd5.portal.config.WebSecurityConfig;
import club.dnd5.portal.controller.api.ApiExceptionHandler;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.model.charactersheet.CharacterSheet;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.charactersheet.CharacterSheetRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import club.dnd5.portal.service.charactersheet.CharacterSheetLimits;
import club.dnd5.portal.service.charactersheet.CharacterSheetService;
import club.dnd5.portal.service.image.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Контракт HTTP API листов персонажей: только авторизованным, документ хранится как есть,
 * чужой лист — 404, устаревшая версия — 409, лимит — 403.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = CharacterSheetControllerTest.TestConfig.class)
@TestPropertySource(properties = "allowed-origin-patterns=*")
class CharacterSheetControllerTest {
	private static final String URL = "/api/v1/character-sheets";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private CharacterSheetRepository sheetRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ImageStorageService imageStorageService;

	private MockMvc mockMvc;
	private CharacterSheet sheet;

	@BeforeEach
	void setUp() {
		reset(sheetRepository, userRepository, imageStorageService);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		User user = new User();
		user.setId(7L);
		user.setUsername("magistr");
		when(userRepository.findByEmailOrUsername("magistr", "magistr")).thenReturn(Optional.of(user));

		Map<String, Object> data = new LinkedHashMap<>();
		data.put("schemaVersion", 1);
		data.put("level", 3);

		sheet = new CharacterSheet();
		sheet.setId(UUID.randomUUID());
		sheet.setUser(user);
		sheet.setName("Торин");
		sheet.setData(data);
		sheet.setVersion(4L);
		sheet.setCreatedAt(Instant.parse("2026-11-20T10:00:00.000Z"));
		sheet.setUpdatedAt(Instant.parse("2026-11-22T10:15:30.123Z"));
		when(sheetRepository.findByIdAndUserIdAndDeletedAtIsNull(any(), anyLong())).thenReturn(Optional.empty());
		when(sheetRepository.findByIdAndUserIdAndDeletedAtIsNull(sheet.getId(), 7L)).thenReturn(Optional.of(sheet));
		when(sheetRepository.findByIdAndUserIdAndDeletedAtIsNotNull(any(), anyLong())).thenReturn(Optional.empty());
		when(sheetRepository.findAllByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(7L)).thenReturn(List.of(sheet));
		when(sheetRepository.findAllByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(7L)).thenReturn(List.of());
		when(sheetRepository.saveAndFlush(any(CharacterSheet.class))).thenAnswer(invocation -> {
			CharacterSheet saved = invocation.getArgument(0);
			if (saved.getId() == null) {
				saved.setId(UUID.randomUUID());
				saved.setVersion(0L);
			}
			return saved;
		});
	}

	@Test
	void anonymousGetsUnauthorized() throws Exception {
		mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
		mockMvc.perform(get(URL + "/" + sheet.getId())).andExpect(status().isUnauthorized());
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"data\":{}}"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(put(URL + "/" + sheet.getId()).contentType(MediaType.APPLICATION_JSON)
				.content("{\"data\":{}}")).andExpect(status().isUnauthorized());
		mockMvc.perform(delete(URL + "/" + sheet.getId())).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void listHasDocumentsAndLimit() throws Exception {
		mockMvc.perform(get(URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.used").value(1))
				.andExpect(jsonPath("$.limit").value(CharacterSheetLimits.SHEETS))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].id").value(sheet.getId().toString()))
				.andExpect(jsonPath("$.items[0].name").value("Торин"))
				.andExpect(jsonPath("$.items[0].data.level").value(3))
				.andExpect(jsonPath("$.items[0].version").value(4))
				.andExpect(jsonPath("$.items[0].updatedAt").value("2026-11-22T10:15:30.123Z"));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void createUsesDefaultNameAndKeepsDocument() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"  \",\"data\":{\"level\":1,\"abilities\":{\"str\":15}}}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(CharacterSheetService.DEFAULT_NAME))
				.andExpect(jsonPath("$.data.abilities.str").value(15))
				.andExpect(jsonPath("$.version").value(0));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void createWithoutDataIsBadRequest() throws Exception {
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Торин\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void createOverLimitIsForbidden() throws Exception {
		when(sheetRepository.countByUserIdAndDeletedAtIsNull(7L)).thenReturn((long) CharacterSheetLimits.SHEETS);
		mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{\"data\":{}}"))
				.andExpect(status().isForbidden());
		verify(sheetRepository, never()).saveAndFlush(any());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void foreignOrMalformedIdIsNotFound() throws Exception {
		mockMvc.perform(get(URL + "/" + UUID.randomUUID())).andExpect(status().isNotFound());
		mockMvc.perform(get(URL + "/not-a-uuid")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void updateWithCurrentVersionSaves() throws Exception {
		mockMvc.perform(put(URL + "/" + sheet.getId()).contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Торин Дубощит\",\"data\":{\"level\":4},\"version\":4}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Торин Дубощит"))
				.andExpect(jsonPath("$.data.level").value(4));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void updateWithStaleVersionIsConflict() throws Exception {
		mockMvc.perform(put(URL + "/" + sheet.getId()).contentType(MediaType.APPLICATION_JSON)
						.content("{\"data\":{\"level\":4},\"version\":3}"))
				.andExpect(status().isConflict());
		verify(sheetRepository, never()).saveAndFlush(any());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void tooLargeDocumentIsBadRequest() throws Exception {
		String big = "x".repeat(CharacterSheetService.MAX_DATA_BYTES);
		mockMvc.perform(put(URL + "/" + sheet.getId()).contentType(MediaType.APPLICATION_JSON)
						.content("{\"data\":{\"notes\":\"" + big + "\"}}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void deleteMovesSheetToHistory() throws Exception {
		mockMvc.perform(delete(URL + "/" + sheet.getId())).andExpect(status().isNoContent());

		assertThat(sheet.getDeletedAt()).isNotNull();
		verify(sheetRepository, never()).delete(any(CharacterSheet.class));
		verify(imageStorageService, never()).deletePath(anyString());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void deleteTrimsOldestHistoryWithAvatars() throws Exception {
		List<CharacterSheet> history = new java.util.ArrayList<>();
		for (int index = 0; index <= CharacterSheetLimits.HISTORY; index++) {
			CharacterSheet old = new CharacterSheet();
			old.setId(UUID.randomUUID());
			old.setDeletedAt(Instant.parse("2026-11-01T00:00:00Z").minusSeconds(index));
			history.add(old);
		}
		when(sheetRepository.findAllByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(7L)).thenReturn(history);
		doThrow(new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "нет хранилища"))
				.when(imageStorageService).deletePath(anyString());

		mockMvc.perform(delete(URL + "/" + sheet.getId())).andExpect(status().isNoContent());

		CharacterSheet evicted = history.get(CharacterSheetLimits.HISTORY);
		verify(sheetRepository).deleteAll(List.of(evicted));
		verify(imageStorageService).deletePath(CharacterSheetService.avatarPath(evicted.getId()));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void listHasHistoryWithoutDocuments() throws Exception {
		CharacterSheet deleted = new CharacterSheet();
		deleted.setId(UUID.randomUUID());
		deleted.setUser(sheet.getUser());
		deleted.setName("Старый");
		deleted.setData(new LinkedHashMap<>(Map.of("level", 1)));
		deleted.setVersion(1L);
		deleted.setCreatedAt(sheet.getCreatedAt());
		deleted.setUpdatedAt(sheet.getUpdatedAt());
		deleted.setDeletedAt(Instant.parse("2026-11-23T10:00:00Z"));
		when(sheetRepository.findAllByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(7L)).thenReturn(List.of(deleted));

		mockMvc.perform(get(URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.used").value(1))
				.andExpect(jsonPath("$.history", hasSize(1)))
				.andExpect(jsonPath("$.history[0].name").value("Старый"))
				.andExpect(jsonPath("$.history[0].data").doesNotExist())
				.andExpect(jsonPath("$.history[0].deletedAt").value("2026-11-23T10:00:00Z"))
				.andExpect(jsonPath("$.historyLimit").value(CharacterSheetLimits.HISTORY))
				.andExpect(jsonPath("$.subscriberHistoryLimit").value(CharacterSheetLimits.SUBSCRIBER_HISTORY));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void restoreFromHistory() throws Exception {
		sheet.setDeletedAt(Instant.parse("2026-11-23T10:00:00Z"));
		when(sheetRepository.findByIdAndUserIdAndDeletedAtIsNotNull(sheet.getId(), 7L)).thenReturn(Optional.of(sheet));

		mockMvc.perform(post(URL + "/" + sheet.getId() + "/restore"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.deletedAt").doesNotExist())
				.andExpect(jsonPath("$.data.level").value(3));
		assertThat(sheet.getDeletedAt()).isNull();
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void restoreOverLimitIsForbidden() throws Exception {
		sheet.setDeletedAt(Instant.parse("2026-11-23T10:00:00Z"));
		when(sheetRepository.findByIdAndUserIdAndDeletedAtIsNotNull(sheet.getId(), 7L)).thenReturn(Optional.of(sheet));
		when(sheetRepository.countByUserIdAndDeletedAtIsNull(7L)).thenReturn((long) CharacterSheetLimits.SHEETS);

		mockMvc.perform(post(URL + "/" + sheet.getId() + "/restore")).andExpect(status().isForbidden());
		assertThat(sheet.getDeletedAt()).isNotNull();
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void restoreActiveOrForeignSheetIsNotFound() throws Exception {
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/restore")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void uploadAvatarToSheetPath() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "portrait.png", "image/png", new byte[] {1, 2, 3});
		String path = CharacterSheetService.avatarPath(sheet.getId());
		when(imageStorageService.uploadTo(eq(path), any(), anyString(), eq(CharacterSheetService.AVATAR_MAX_SIDE)))
				.thenReturn("/s3/5e14/" + path);

		mockMvc.perform(multipart(URL + "/" + sheet.getId() + "/avatar").file(file))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.startsWith("/s3/5e14/" + path + "?v=")));
		verify(sheetRepository, never()).saveAndFlush(any(CharacterSheet.class));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void avatarOfForeignSheetIsNotFound() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "portrait.png", "image/png", new byte[] {1});
		String foreign = URL + "/" + UUID.randomUUID() + "/avatar";

		mockMvc.perform(multipart(foreign).file(file)).andExpect(status().isNotFound());
		mockMvc.perform(delete(foreign)).andExpect(status().isNotFound());
		verify(imageStorageService, never()).uploadTo(anyString(), any(), anyString(), anyInt());
		verify(imageStorageService, never()).deletePath(anyString());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void deleteAvatarOfOwnSheet() throws Exception {
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/avatar")).andExpect(status().isNoContent());
		verify(imageStorageService).deletePath(CharacterSheetService.avatarPath(sheet.getId()));
	}

	@Test
	void anonymousCannotUploadAvatar() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "portrait.png", "image/png", new byte[] {1});
		mockMvc.perform(multipart(URL + "/" + sheet.getId() + "/avatar").file(file))
				.andExpect(status().isUnauthorized());
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
		UserRepository userRepository() {
			return mock(UserRepository.class);
		}

		@Bean
		ImageStorageService imageStorageService() {
			return mock(ImageStorageService.class);
		}

		@Bean
		CharacterSheetService characterSheetService(CharacterSheetRepository sheetRepository,
													UserRepository userRepository,
													ImageStorageService imageStorageService) {
			return new CharacterSheetService(sheetRepository, userRepository, new JsonMapper(), imageStorageService);
		}

		@Bean
		CharacterSheetController characterSheetController(CharacterSheetService service) {
			return new CharacterSheetController(service);
		}
	}
}
