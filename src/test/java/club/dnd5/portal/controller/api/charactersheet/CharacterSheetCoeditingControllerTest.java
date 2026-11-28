package club.dnd5.portal.controller.api.charactersheet;

import club.dnd5.portal.config.WebSecurityConfig;
import club.dnd5.portal.controller.api.ApiExceptionHandler;
import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.model.charactersheet.CharacterSheet;
import club.dnd5.portal.model.charactersheet.CharacterSheetEditor;
import club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus;
import club.dnd5.portal.model.charactersheet.CharacterSheetPresence;
import club.dnd5.portal.model.charactersheet.SavedCharacterSheet;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.charactersheet.CharacterSheetEditorRepository;
import club.dnd5.portal.repository.charactersheet.CharacterSheetPresenceRepository;
import club.dnd5.portal.repository.charactersheet.CharacterSheetRepository;
import club.dnd5.portal.repository.charactersheet.SavedCharacterSheetRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import club.dnd5.portal.service.charactersheet.CharacterSheetEditorService;
import club.dnd5.portal.service.charactersheet.CharacterSheetLimits;
import club.dnd5.portal.service.charactersheet.CharacterSheetPresenceService;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
 * Совместная правка листа: запрос на редактирование из «Других листов», решение владельца (до 5
 * редакторов), доступ редактора к листу, действия только владельца и отметки присутствия.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = CharacterSheetCoeditingControllerTest.TestConfig.class)
@TestPropertySource(properties = "allowed-origin-patterns=*")
class CharacterSheetCoeditingControllerTest {
	private static final String URL = "/api/v1/character-sheets";
	private static final String FORBIDDEN_MESSAGE = "Доступ к листу персонажа запрещен";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private CharacterSheetRepository sheetRepository;

	@Autowired
	private SavedCharacterSheetRepository savedRepository;

	@Autowired
	private CharacterSheetEditorRepository editorRepository;

	@Autowired
	private CharacterSheetPresenceRepository presenceRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ImageStorageService imageStorageService;

	private MockMvc mockMvc;
	private User owner;
	private User editorUser;
	private User stranger;
	private CharacterSheet sheet;
	private UUID token;
	private SavedCharacterSheet saved;

	@BeforeEach
	void setUp() {
		reset(sheetRepository, savedRepository, editorRepository, presenceRepository, userRepository,
				imageStorageService);
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

		owner = user(7L, "magistr", "Мастер");
		editorUser = user(8L, "guest", "Гость");
		stranger = user(9L, "stranger", null);
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

		saved = new SavedCharacterSheet();
		saved.setId(UUID.randomUUID());
		saved.setUserId(8L);
		saved.setSheetId(sheet.getId());
		saved.setShareToken(token);
		saved.setName("Торин");
		saved.setCreatedAt(Instant.parse("2026-11-23T10:00:00Z"));

		when(sheetRepository.findByIdAndUserIdAndDeletedAtIsNull(sheet.getId(), 7L)).thenReturn(Optional.of(sheet));
		when(sheetRepository.findByIdAndDeletedAtIsNull(sheet.getId())).thenReturn(Optional.of(sheet));
		when(sheetRepository.findById(sheet.getId())).thenReturn(Optional.of(sheet));
		when(sheetRepository.saveAndFlush(any(CharacterSheet.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(savedRepository.findByIdAndUserId(saved.getId(), 8L)).thenReturn(Optional.of(saved));
		when(editorRepository.saveAndFlush(any(CharacterSheetEditor.class))).thenAnswer(invocation -> {
			CharacterSheetEditor editor = invocation.getArgument(0);
			if (editor.getId() == null) {
				editor.setId(UUID.randomUUID());
			}
			return editor;
		});
		when(userRepository.findAllById(any())).thenAnswer(invocation -> {
			List<User> found = new ArrayList<>();
			for (Object id : (Iterable<?>) invocation.getArgument(0)) {
				for (User candidate : List.of(owner, editorUser, stranger)) {
					if (candidate.getId().equals(id)) {
						found.add(candidate);
					}
				}
			}
			return found;
		});
	}

	private User user(Long id, String login, String displayName) {
		User user = new User();
		user.setId(id);
		user.setUsername(login);
		user.setEmail(login + "@example.com");
		user.setDisplayName(displayName);
		user.setRoles(new ArrayList<>(List.of(new Role("USER"))));
		when(userRepository.findByEmailOrUsername(login, login)).thenReturn(Optional.of(user));
		return user;
	}

	private CharacterSheetEditor editorRow(Long userId, CharacterSheetEditorStatus status, String createdAt) {
		CharacterSheetEditor editor = new CharacterSheetEditor();
		editor.setId(UUID.randomUUID());
		editor.setSheetId(sheet.getId());
		editor.setUserId(userId);
		editor.setStatus(status);
		editor.setCreatedAt(Instant.parse(createdAt));
		return editor;
	}

	/** Пользователь 8 — редактор с выданным правом. */
	private void approveEditor() {
		when(editorRepository.existsBySheetIdAndUserIdAndStatus(sheet.getId(), 8L, CharacterSheetEditorStatus.APPROVED))
				.thenReturn(true);
	}

	// --- Запрос на редактирование ---

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void editRequestCreatesPending() throws Exception {
		mockMvc.perform(post(URL + "/saved/" + saved.getId() + "/edit-request"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PENDING"));

		ArgumentCaptor<CharacterSheetEditor> captor = ArgumentCaptor.forClass(CharacterSheetEditor.class);
		verify(editorRepository).saveAndFlush(captor.capture());
		assertThat(captor.getValue().getSheetId()).isEqualTo(sheet.getId());
		assertThat(captor.getValue().getUserId()).isEqualTo(8L);
		assertThat(captor.getValue().getStatus()).isEqualTo(CharacterSheetEditorStatus.PENDING);
		assertThat(captor.getValue().getCreatedAt()).isNotNull();
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void repeatedEditRequestIsIdempotent() throws Exception {
		when(editorRepository.findBySheetIdAndUserId(sheet.getId(), 8L))
				.thenReturn(Optional.of(editorRow(8L, CharacterSheetEditorStatus.APPROVED, "2026-11-23T11:00:00Z")));

		mockMvc.perform(post(URL + "/saved/" + saved.getId() + "/edit-request"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"));
		verify(editorRepository, never()).saveAndFlush(any(CharacterSheetEditor.class));
	}

	@Test
	@WithMockUser(username = "stranger", roles = "USER")
	void editRequestForForeignSavedIsNotFound() throws Exception {
		mockMvc.perform(post(URL + "/saved/" + saved.getId() + "/edit-request"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Сохранённый лист персонажа не найден"));
		mockMvc.perform(post(URL + "/saved/not-an-id/edit-request")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void editRequestForRevokedOrDeletedSheetIsNotFound() throws Exception {
		sheet.setShareToken(UUID.randomUUID());
		mockMvc.perform(post(URL + "/saved/" + saved.getId() + "/edit-request"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Лист персонажа по этой ссылке не найден"));

		sheet.setShareToken(token);
		sheet.setDeletedAt(Instant.now());
		mockMvc.perform(post(URL + "/saved/" + saved.getId() + "/edit-request"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Лист персонажа по этой ссылке не найден"));
		verify(editorRepository, never()).saveAndFlush(any(CharacterSheetEditor.class));
	}

	@Test
	void guestCannotRequestEdit() throws Exception {
		mockMvc.perform(post(URL + "/saved/" + saved.getId() + "/edit-request")).andExpect(status().isUnauthorized());
		mockMvc.perform(get(URL + "/saved/edit-access")).andExpect(status().isUnauthorized());
		mockMvc.perform(get(URL + "/edit-requests/count")).andExpect(status().isUnauthorized());
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/presence")).andExpect(status().isUnauthorized());
	}

	// --- Сводка и «Другие листы» ---

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void editAccessListsOnlyRequestedSheets() throws Exception {
		SavedCharacterSheet other = new SavedCharacterSheet();
		other.setId(UUID.randomUUID());
		other.setUserId(8L);
		other.setSheetId(UUID.randomUUID());
		other.setShareToken(UUID.randomUUID());
		other.setName("Другой");
		other.setCreatedAt(Instant.now());
		when(savedRepository.findAllByUserIdOrderByCreatedAtDesc(8L)).thenReturn(List.of(other, saved));
		when(editorRepository.findAllByUserIdAndSheetIdIn(eq(8L), any()))
				.thenReturn(List.of(editorRow(8L, CharacterSheetEditorStatus.PENDING, "2026-11-23T11:00:00Z")));

		mockMvc.perform(get(URL + "/saved/edit-access"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sheets", hasSize(1)))
				.andExpect(jsonPath("$.sheets[0].savedId").value(saved.getId().toString()))
				.andExpect(jsonPath("$.sheets[0].sheetId").value(sheet.getId().toString()))
				.andExpect(jsonPath("$.sheets[0].name").value("Торин"))
				.andExpect(jsonPath("$.sheets[0].status").value("PENDING"));
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void savedListHasEditStatus() throws Exception {
		SavedCharacterSheet other = new SavedCharacterSheet();
		other.setId(UUID.randomUUID());
		other.setUserId(8L);
		other.setSheetId(UUID.randomUUID());
		other.setShareToken(UUID.randomUUID());
		other.setName("Другой");
		other.setCreatedAt(Instant.now());
		when(savedRepository.findAllByUserIdOrderByCreatedAtDesc(8L)).thenReturn(List.of(saved, other));
		when(sheetRepository.findAllByIdIn(any())).thenReturn(List.of(sheet));
		when(editorRepository.findAllByUserIdAndSheetIdIn(eq(8L), any()))
				.thenReturn(List.of(editorRow(8L, CharacterSheetEditorStatus.APPROVED, "2026-11-23T11:00:00Z")));

		mockMvc.perform(get(URL + "/saved"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].editStatus").value("APPROVED"))
				.andExpect(jsonPath("$.items[1].editStatus").doesNotExist());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void removingSavedDropsOwnEditorRow() throws Exception {
		mockMvc.perform(delete(URL + "/saved/" + saved.getId())).andExpect(status().isNoContent());

		verify(editorRepository).deleteBySheetIdAndUserId(sheet.getId(), 8L);
		verify(savedRepository).delete(saved);
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void countsIncomingRequests() throws Exception {
		when(editorRepository.countPendingForOwner(7L)).thenReturn(3L);

		mockMvc.perform(get(URL + "/edit-requests/count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.count").value(3));
	}

	// --- Решение владельца ---

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void ownerSeesEditorsOldestFirstWithoutLogins() throws Exception {
		CharacterSheetEditor approved = editorRow(8L, CharacterSheetEditorStatus.APPROVED, "2026-11-23T10:00:00Z");
		CharacterSheetEditor pending = editorRow(9L, CharacterSheetEditorStatus.PENDING, "2026-11-24T10:00:00Z");
		when(editorRepository.findAllBySheetIdOrderByCreatedAtAsc(sheet.getId())).thenReturn(List.of(approved, pending));

		mockMvc.perform(get(URL + "/" + sheet.getId() + "/editors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.limit").value(5))
				.andExpect(jsonPath("$.editors", hasSize(2)))
				.andExpect(jsonPath("$.editors[0].id").value(approved.getId().toString()))
				.andExpect(jsonPath("$.editors[0].displayName").value("Гость"))
				.andExpect(jsonPath("$.editors[0].status").value("APPROVED"))
				.andExpect(jsonPath("$.editors[0].requestedAt").value("2026-11-23T10:00:00Z"))
				.andExpect(jsonPath("$.editors[0].username").doesNotExist())
				.andExpect(jsonPath("$.editors[0].email").doesNotExist())
				.andExpect(jsonPath("$.editors[1].displayName").value("Пользователь без имени"))
				.andExpect(jsonPath("$.editors[1].status").value("PENDING"));
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void editorCannotManageEditors() throws Exception {
		approveEditor();
		UUID editorId = UUID.randomUUID();

		mockMvc.perform(get(URL + "/" + sheet.getId() + "/editors"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(FORBIDDEN_MESSAGE));
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/editors/" + editorId + "/approve"))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/editors/" + editorId))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(username = "stranger", roles = "USER")
	void strangerDoesNotSeeEditors() throws Exception {
		mockMvc.perform(get(URL + "/" + sheet.getId() + "/editors")).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void approveGrantsEditing() throws Exception {
		CharacterSheetEditor pending = editorRow(8L, CharacterSheetEditorStatus.PENDING, "2026-11-23T10:00:00Z");
		when(editorRepository.findByIdAndSheetId(pending.getId(), sheet.getId())).thenReturn(Optional.of(pending));
		when(editorRepository.findAllBySheetIdOrderByCreatedAtAsc(sheet.getId())).thenReturn(List.of(pending));

		mockMvc.perform(post(URL + "/" + sheet.getId() + "/editors/" + pending.getId() + "/approve"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.limit").value(5))
				.andExpect(jsonPath("$.editors[0].status").value("APPROVED"));
		assertThat(pending.getDecidedAt()).isNotNull();
		verify(editorRepository).saveAndFlush(pending);
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void approveOverLimitIsConflict() throws Exception {
		CharacterSheetEditor pending = editorRow(8L, CharacterSheetEditorStatus.PENDING, "2026-11-23T10:00:00Z");
		when(editorRepository.findByIdAndSheetId(pending.getId(), sheet.getId())).thenReturn(Optional.of(pending));
		when(editorRepository.countBySheetIdAndStatus(sheet.getId(), CharacterSheetEditorStatus.APPROVED)).thenReturn(5L);

		mockMvc.perform(post(URL + "/" + sheet.getId() + "/editors/" + pending.getId() + "/approve"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("У листа уже 5 редакторов — отзовите право у одного из них"));
		assertThat(pending.getStatus()).isEqualTo(CharacterSheetEditorStatus.PENDING);
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void approveAlreadyApprovedIsNoop() throws Exception {
		CharacterSheetEditor approved = editorRow(8L, CharacterSheetEditorStatus.APPROVED, "2026-11-23T10:00:00Z");
		when(editorRepository.findByIdAndSheetId(approved.getId(), sheet.getId())).thenReturn(Optional.of(approved));
		when(editorRepository.countBySheetIdAndStatus(sheet.getId(), CharacterSheetEditorStatus.APPROVED)).thenReturn(5L);

		mockMvc.perform(post(URL + "/" + sheet.getId() + "/editors/" + approved.getId() + "/approve"))
				.andExpect(status().isOk());
		verify(editorRepository, never()).saveAndFlush(any(CharacterSheetEditor.class));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void unknownEditorIsNotFound() throws Exception {
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/editors/" + UUID.randomUUID() + "/approve"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Запрос на редактирование не найден"));
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/editors/" + UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Запрос на редактирование не найден"));
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/editors/garbage"))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void removeDeclinesOrRevokesByDeleting() throws Exception {
		CharacterSheetEditor approved = editorRow(8L, CharacterSheetEditorStatus.APPROVED, "2026-11-23T10:00:00Z");
		when(editorRepository.findByIdAndSheetId(approved.getId(), sheet.getId())).thenReturn(Optional.of(approved));
		when(editorRepository.findAllBySheetIdOrderByCreatedAtAsc(sheet.getId())).thenReturn(List.of());

		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/editors/" + approved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.limit").value(5))
				.andExpect(jsonPath("$.editors", hasSize(0)));
		verify(editorRepository).delete(approved);
	}

	// --- Лист у владельца и редактора ---

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void ownerSeesPendingRequestsOnSheet() throws Exception {
		when(editorRepository.countBySheetIdAndStatus(sheet.getId(), CharacterSheetEditorStatus.PENDING)).thenReturn(2L);

		mockMvc.perform(get(URL + "/" + sheet.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.editor").value(false))
				.andExpect(jsonPath("$.shareToken").value(token.toString()))
				.andExpect(jsonPath("$.pendingEditRequests").value(2));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void listHasPendingRequestsFromOneQuery() throws Exception {
		when(sheetRepository.findAllByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(7L)).thenReturn(List.of(sheet));
		List<Object[]> rows = new ArrayList<>();
		rows.add(new Object[]{sheet.getId(), 2L});
		when(editorRepository.countPendingBySheetIds(List.of(sheet.getId()))).thenReturn(rows);

		mockMvc.perform(get(URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].pendingEditRequests").value(2))
				.andExpect(jsonPath("$.items[0].editor").value(false));
		verify(editorRepository, never()).countBySheetIdAndStatus(any(), any());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void approvedEditorOpensSheetWithoutShareToken() throws Exception {
		approveEditor();

		mockMvc.perform(get(URL + "/" + sheet.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Торин"))
				.andExpect(jsonPath("$.data.level").value(3))
				.andExpect(jsonPath("$.editor").value(true))
				.andExpect(jsonPath("$.shareToken").doesNotExist())
				.andExpect(jsonPath("$.pendingEditRequests").value(0));
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void approvedEditorSavesSheet() throws Exception {
		approveEditor();

		mockMvc.perform(put(URL + "/" + sheet.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Торин II\",\"data\":{\"level\":4},\"version\":4}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Торин II"))
				.andExpect(jsonPath("$.data.level").value(4))
				.andExpect(jsonPath("$.editor").value(true))
				.andExpect(jsonPath("$.shareToken").doesNotExist());
		assertThat(sheet.getName()).isEqualTo("Торин II");
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void approvedEditorGetsConflictOnStaleVersion() throws Exception {
		approveEditor();

		mockMvc.perform(put(URL + "/" + sheet.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"data\":{\"level\":4},\"version\":3}"))
				.andExpect(status().isConflict());
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void pendingRequesterIsStillStranger() throws Exception {
		mockMvc.perform(get(URL + "/" + sheet.getId())).andExpect(status().isNotFound());
		mockMvc.perform(put(URL + "/" + sheet.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"data\":{\"level\":4}}"))
				.andExpect(status().isNotFound());
		verify(sheetRepository, never()).saveAndFlush(any(CharacterSheet.class));
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void approvedEditorChangesAvatar() throws Exception {
		approveEditor();
		String path = CharacterSheetService.avatarPath(sheet.getId());
		when(imageStorageService.uploadTo(eq(path), any(), anyString(), anyInt())).thenReturn("https://img/" + path);
		MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3});

		mockMvc.perform(multipart(URL + "/" + sheet.getId() + "/avatar").file(file))
				.andExpect(status().isOk());
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/avatar")).andExpect(status().isNoContent());
		verify(imageStorageService).deletePath(path);
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void editorCannotDoOwnerOnlyActions() throws Exception {
		approveEditor();

		mockMvc.perform(delete(URL + "/" + sheet.getId()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(FORBIDDEN_MESSAGE));
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/restore")).andExpect(status().isForbidden());
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/share")).andExpect(status().isForbidden());
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/share")).andExpect(status().isForbidden());

		assertThat(sheet.getDeletedAt()).isNull();
		verify(sheetRepository, never()).updateShareToken(any(), any());
		verify(editorRepository, never()).deleteAllBySheetIdIn(any());
	}

	// --- Чистка записей ---

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void unshareDropsAllEditors() throws Exception {
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/share")).andExpect(status().isNoContent());

		verify(sheetRepository).updateShareToken(sheet.getId(), null);
		verify(editorRepository).deleteAllBySheetIdIn(List.of(sheet.getId()));
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void softDeleteDropsPresence() throws Exception {
		mockMvc.perform(delete(URL + "/" + sheet.getId())).andExpect(status().isNoContent());

		assertThat(sheet.getDeletedAt()).isNotNull();
		verify(presenceRepository).deleteAllBySheetIdIn(List.of(sheet.getId()));
		verify(editorRepository, never()).deleteAllBySheetIdIn(any());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void trimHistoryDropsEditorsOfEvictedSheets() throws Exception {
		List<CharacterSheet> history = new ArrayList<>();
		for (int index = 0; index <= CharacterSheetLimits.HISTORY; index++) {
			CharacterSheet old = new CharacterSheet();
			old.setId(UUID.randomUUID());
			old.setDeletedAt(Instant.parse("2026-11-01T00:00:00Z").minusSeconds(index));
			history.add(old);
		}
		when(sheetRepository.findAllByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(7L)).thenReturn(history);

		mockMvc.perform(delete(URL + "/" + sheet.getId())).andExpect(status().isNoContent());

		CharacterSheet evicted = history.get(CharacterSheetLimits.HISTORY);
		verify(editorRepository).deleteAllBySheetIdIn(List.of(evicted.getId()));
		verify(sheetRepository).deleteAll(List.of(evicted));
	}

	// --- Присутствие ---

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void ownerHeartbeatReturnsOthersAndVersion() throws Exception {
		CharacterSheetPresence other = new CharacterSheetPresence();
		other.setId(UUID.randomUUID());
		other.setSheetId(sheet.getId());
		other.setUserId(8L);
		other.setSeenAt(Instant.now());
		when(presenceRepository.findAllBySheetIdAndUserIdNotAndSeenAtAfter(eq(sheet.getId()), eq(7L), any()))
				.thenReturn(List.of(other));

		mockMvc.perform(post(URL + "/" + sheet.getId() + "/presence"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.version").value(4))
				.andExpect(jsonPath("$.users", hasSize(1)))
				.andExpect(jsonPath("$.users[0].displayName").value("Гость"))
				.andExpect(jsonPath("$.users[0].username").doesNotExist());

		ArgumentCaptor<Instant> staleBefore = ArgumentCaptor.forClass(Instant.class);
		ArgumentCaptor<Instant> seenAt = ArgumentCaptor.forClass(Instant.class);
		verify(presenceRepository).deleteStale(eq(sheet.getId()), staleBefore.capture());
		verify(presenceRepository).upsert(anyString(), eq(sheet.getId().toString()), eq(7L), seenAt.capture());
		assertThat(seenAt.getValue().getEpochSecond() - staleBefore.getValue().getEpochSecond())
				.isEqualTo(CharacterSheetPresenceService.PRESENCE_TTL.toSeconds());
		verify(sheetRepository, never()).saveAndFlush(any(CharacterSheet.class));
		verify(sheetRepository, never()).save(any(CharacterSheet.class));
		assertThat(sheet.getVersion()).isEqualTo(4L);
	}

	@Test
	@WithMockUser(username = "guest", roles = "USER")
	void approvedEditorHeartbeatSeesOwner() throws Exception {
		approveEditor();
		CharacterSheetPresence ownerMark = new CharacterSheetPresence();
		ownerMark.setId(UUID.randomUUID());
		ownerMark.setSheetId(sheet.getId());
		ownerMark.setUserId(7L);
		ownerMark.setSeenAt(Instant.now());
		when(presenceRepository.findAllBySheetIdAndUserIdNotAndSeenAtAfter(eq(sheet.getId()), eq(8L), any()))
				.thenReturn(List.of(ownerMark));

		mockMvc.perform(post(URL + "/" + sheet.getId() + "/presence"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.users[0].displayName").value("Мастер"));
		verify(presenceRepository).upsert(anyString(), eq(sheet.getId().toString()), eq(8L), any());
	}

	@Test
	@WithMockUser(username = "stranger", roles = "USER")
	void strangerHeartbeatIsForbidden() throws Exception {
		mockMvc.perform(post(URL + "/" + sheet.getId() + "/presence"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value(FORBIDDEN_MESSAGE));
		verify(presenceRepository, never()).upsert(anyString(), anyString(), anyLong(), any());
	}

	@Test
	@WithMockUser(username = "magistr", roles = "USER")
	void heartbeatOnDeletedSheetIsNotFound() throws Exception {
		UUID deleted = UUID.randomUUID();
		mockMvc.perform(post(URL + "/" + deleted + "/presence")).andExpect(status().isNotFound());
		verify(presenceRepository, never()).upsert(anyString(), anyString(), anyLong(), any());
	}

	@Test
	@WithMockUser(username = "stranger", roles = "USER")
	void leaveRemovesOnlyOwnMarkWithoutAccessCheck() throws Exception {
		mockMvc.perform(delete(URL + "/" + sheet.getId() + "/presence")).andExpect(status().isNoContent());

		verify(presenceRepository).deleteBySheetIdAndUserId(sheet.getId(), 9L);
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
		CharacterSheetEditorRepository characterSheetEditorRepository() {
			return mock(CharacterSheetEditorRepository.class);
		}

		@Bean
		CharacterSheetPresenceRepository characterSheetPresenceRepository() {
			return mock(CharacterSheetPresenceRepository.class);
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
													ImageStorageService imageStorageService,
													CharacterSheetEditorRepository editorRepository,
													CharacterSheetPresenceRepository presenceRepository) {
			return new CharacterSheetService(sheetRepository, userRepository, new JsonMapper(), imageStorageService,
					editorRepository, presenceRepository);
		}

		@Bean
		CharacterSheetShareService characterSheetShareService(CharacterSheetService sheetService,
															  CharacterSheetRepository sheetRepository,
															  SavedCharacterSheetRepository savedRepository,
															  CharacterSheetEditorRepository editorRepository) {
			return new CharacterSheetShareService(sheetService, sheetRepository, savedRepository, editorRepository);
		}

		@Bean
		CharacterSheetEditorService characterSheetEditorService(CharacterSheetService sheetService,
																CharacterSheetEditorRepository editorRepository,
																CharacterSheetRepository sheetRepository,
																SavedCharacterSheetRepository savedRepository,
																UserRepository userRepository) {
			return new CharacterSheetEditorService(sheetService, editorRepository, sheetRepository, savedRepository,
					userRepository);
		}

		@Bean
		CharacterSheetPresenceService characterSheetPresenceService(CharacterSheetService sheetService,
																	CharacterSheetEditorService editorService,
																	CharacterSheetPresenceRepository presenceRepository) {
			return new CharacterSheetPresenceService(sheetService, editorService, presenceRepository);
		}

		@Bean
		CharacterSheetController characterSheetController(CharacterSheetService service) {
			return new CharacterSheetController(service);
		}

		@Bean
		CharacterSheetShareController characterSheetShareController(CharacterSheetShareService service) {
			return new CharacterSheetShareController(service);
		}

		@Bean
		CharacterSheetEditorController characterSheetEditorController(CharacterSheetEditorService service) {
			return new CharacterSheetEditorController(service);
		}

		@Bean
		CharacterSheetPresenceController characterSheetPresenceController(CharacterSheetPresenceService service) {
			return new CharacterSheetPresenceController(service);
		}
	}
}
