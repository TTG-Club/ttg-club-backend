package club.dnd5.portal.service.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditAccessListResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditAccessResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditRequestCountResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditRequestResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditorListResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditorResponse;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.charactersheet.CharacterSheet;
import club.dnd5.portal.model.charactersheet.CharacterSheetEditor;
import club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus;
import club.dnd5.portal.model.charactersheet.SavedCharacterSheet;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.charactersheet.CharacterSheetEditorRepository;
import club.dnd5.portal.repository.charactersheet.CharacterSheetRepository;
import club.dnd5.portal.repository.charactersheet.SavedCharacterSheetRepository;
import club.dnd5.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Права на редактирование чужого листа: запрос, решение владельца и счётчик входящих запросов
 * (как в core-api).
 * <p>
 * Право выдаётся только по запросу. Просить может лишь тот, кто уже сохранил лист по живой ссылке
 * «поделиться», поэтому искать пользователей по логину или почте не нужно и перебирать их некому.
 * Решений у владельца два — разрешить или нет. Отказ и отзыв права запись удаляют: никаких
 * блокировок и пауз, попросить право заново можно сразу.
 */
@RequiredArgsConstructor
@Service
public class CharacterSheetEditorService {

	/** Сколько редакторов может быть у одного листа одновременно. */
	public static final int MAX_EDITORS = 5;

	/** Подпись пользователя без отображаемого имени: логин и почта наружу не отдаются. */
	public static final String UNNAMED_USER = "Пользователь без имени";

	private final CharacterSheetService sheetService;
	private final CharacterSheetEditorRepository editorRepository;
	private final CharacterSheetRepository sheetRepository;
	private final SavedCharacterSheetRepository savedRepository;
	private final UserRepository userRepository;

	/**
	 * Запрос на редактирование листа, сохранённого по ссылке. Чужая запись — 404; отозванная ссылка
	 * или удалённый лист — 404, как и при просмотре. Повтор идемпотентен: ждущий запрос не
	 * дублируется, выданное право остаётся.
	 * @param savedId идентификатор сохранённой записи
	 */
	@Transactional
	public CharacterSheetEditRequestResponse requestEdit(UUID savedId) {
		User user = sheetService.requireUser();
		SavedCharacterSheet saved = savedRepository.findByIdAndUserId(savedId, user.getId())
				.orElseThrow(CharacterSheetEditorService::savedNotFound);
		CharacterSheet sheet = sheetRepository.findById(saved.getSheetId())
				.filter(found -> found.getDeletedAt() == null
						&& Objects.equals(found.getShareToken(), saved.getShareToken()))
				.orElseThrow(CharacterSheetShareService::sharedNotFound);
		CharacterSheetEditor editor = editorRepository.findBySheetIdAndUserId(sheet.getId(), user.getId())
				.orElseGet(() -> editorRepository.saveAndFlush(newRequest(sheet, user)));
		return new CharacterSheetEditRequestResponse(editor.getStatus());
	}

	/** Неотвеченные запросы и выданные права листа — только владельцу. */
	@Transactional(readOnly = true)
	public CharacterSheetEditorListResponse findEditors(UUID sheetId) {
		return toListResponse(sheetService.getOwned(sheetId));
	}

	/**
	 * Разрешает редактирование. Уже выданное право — без изменений. Сверх {@link #MAX_EDITORS} — 409.
	 */
	@Transactional
	public CharacterSheetEditorListResponse approve(UUID sheetId, UUID editorId) {
		CharacterSheet sheet = sheetService.getOwned(sheetId);
		CharacterSheetEditor editor = getEditor(sheet, editorId);
		if (editor.getStatus() == CharacterSheetEditorStatus.PENDING) {
			if (editorRepository.countBySheetIdAndStatus(sheet.getId(), CharacterSheetEditorStatus.APPROVED)
					>= MAX_EDITORS) {
				throw new ApiException(HttpStatus.CONFLICT, String.format(
						"У листа уже %d редакторов — отзовите право у одного из них", MAX_EDITORS));
			}
			editor.setStatus(CharacterSheetEditorStatus.APPROVED);
			editor.setDecidedAt(now());
			editorRepository.saveAndFlush(editor);
		}
		return toListResponse(sheet);
	}

	/**
	 * Отклоняет запрос или отзывает выданное право — для владельца это одно действие «убрать».
	 * Запись удаляется целиком: пользователь может попросить право снова, когда захочет.
	 */
	@Transactional
	public CharacterSheetEditorListResponse remove(UUID sheetId, UUID editorId) {
		CharacterSheet sheet = sheetService.getOwned(sheetId);
		editorRepository.delete(getEditor(sheet, editorId));
		editorRepository.flush();
		return toListResponse(sheet);
	}

	/** Неотвеченные запросы на активные листы текущего пользователя — для точки у меню. */
	@Transactional(readOnly = true)
	public CharacterSheetEditRequestCountResponse countIncoming() {
		User user = sheetService.requireUser();
		return new CharacterSheetEditRequestCountResponse(editorRepository.countPendingForOwner(user.getId()));
	}

	/**
	 * Права на редактирование сохранённых листов текущего пользователя — без документов, чтобы
	 * клиент мог часто спрашивать, не ответил ли владелец. Листы, по которым ничего не запрашивали,
	 * в ответ не попадают.
	 */
	@Transactional(readOnly = true)
	public CharacterSheetEditAccessListResponse findMyEditAccess() {
		User user = sheetService.requireUser();
		List<SavedCharacterSheet> saved = savedRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
		if (saved.isEmpty()) {
			return new CharacterSheetEditAccessListResponse(List.of());
		}
		Map<UUID, CharacterSheetEditorStatus> statuses = editorRepository
				.findAllByUserIdAndSheetIdIn(user.getId(), saved.stream().map(SavedCharacterSheet::getSheetId).toList())
				.stream()
				.collect(Collectors.toMap(CharacterSheetEditor::getSheetId, CharacterSheetEditor::getStatus));
		List<CharacterSheetEditAccessResponse> sheets = saved.stream()
				.filter(item -> statuses.containsKey(item.getSheetId()))
				.map(item -> new CharacterSheetEditAccessResponse(item.getId().toString(),
						item.getSheetId().toString(), item.getName(), statuses.get(item.getSheetId())))
				.toList();
		return new CharacterSheetEditAccessListResponse(sheets);
	}

	/**
	 * Отображаемые имена пользователей одним запросом; у пользователя без имени — общая подпись,
	 * логин и почта наружу не отдаются.
	 */
	public Map<Long, String> displayNames(Collection<Long> userIds) {
		Map<Long, String> names = new HashMap<>();
		if (userIds.isEmpty()) {
			return names;
		}
		userRepository.findAllById(userIds).forEach(user -> names.put(user.getId(), displayName(user)));
		return names;
	}

	private static String displayName(User user) {
		String name = user.getDisplayName();
		return name == null || name.isBlank() ? UNNAMED_USER : name.trim();
	}

	private static CharacterSheetEditor newRequest(CharacterSheet sheet, User user) {
		CharacterSheetEditor editor = new CharacterSheetEditor();
		editor.setSheetId(sheet.getId());
		editor.setUserId(user.getId());
		editor.setStatus(CharacterSheetEditorStatus.PENDING);
		editor.setCreatedAt(now());
		return editor;
	}

	private CharacterSheetEditor getEditor(CharacterSheet sheet, UUID editorId) {
		return editorRepository.findByIdAndSheetId(editorId, sheet.getId())
				.orElseThrow(CharacterSheetEditorService::editorNotFound);
	}

	/** Ждущие запросы и выданные права с именами, старые первее. */
	private CharacterSheetEditorListResponse toListResponse(CharacterSheet sheet) {
		List<CharacterSheetEditor> editors = editorRepository.findAllBySheetIdOrderByCreatedAtAsc(sheet.getId());
		Map<Long, String> names = displayNames(editors.stream().map(CharacterSheetEditor::getUserId).toList());
		List<CharacterSheetEditorResponse> responses = editors.stream()
				.map(editor -> new CharacterSheetEditorResponse(
						editor.getId().toString(),
						names.getOrDefault(editor.getUserId(), UNNAMED_USER),
						editor.getStatus(),
						editor.getCreatedAt() == null ? null : editor.getCreatedAt().toString()))
				.toList();
		return new CharacterSheetEditorListResponse(MAX_EDITORS, responses);
	}

	private static Instant now() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

	public static ApiException editorNotFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "Запрос на редактирование не найден");
	}

	private static ApiException savedNotFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "Сохранённый лист персонажа не найден");
	}
}
