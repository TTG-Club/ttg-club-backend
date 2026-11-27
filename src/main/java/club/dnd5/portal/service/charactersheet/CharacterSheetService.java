package club.dnd5.portal.service.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetAvatarResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetListResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetResponse;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.charactersheet.CharacterSheet;
import club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.charactersheet.CharacterSheetEditorRepository;
import club.dnd5.portal.repository.charactersheet.CharacterSheetPresenceRepository;
import club.dnd5.portal.repository.charactersheet.CharacterSheetRepository;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.service.image.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Листы персонажей пользователя. Документ листа сервер не разбирает — только хранит,
 * проверяя размер. Чужой и несуществующий лист неразличимы — оба 404.
 * <p>
 * Портрет персонажа — один файл на лист ({@link #avatarPath}): адрес хранит сам документ листа,
 * а файл стирается вместе с листом.
 * <p>
 * Открыть и сохранить лист (и сменить портрет) может владелец или редактор, которому владелец дал
 * право ({@link CharacterSheetEditorService}); удаление, восстановление, ссылка и редакторы — только
 * владельцу: редактору там 403, остальным — 404.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CharacterSheetService {

	public static final int MAX_NAME_LENGTH = 100;
	/** Предел документа в байтах UTF-8: лист с заметками и инвентарём укладывается с запасом. */
	public static final int MAX_DATA_BYTES = 512 * 1024;
	public static final String DEFAULT_NAME = "Новый персонаж";
	/** Наибольшая сторона портрета: на листе он рисуется кругом в 96 px. */
	public static final int AVATAR_MAX_SIDE = 512;
	/** Портрет перезаписывают на месте, поэтому кэшируем его ненадолго. */
	private static final String AVATAR_CACHE_CONTROL = "public, max-age=600";

	private final CharacterSheetRepository sheetRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;
	private final ImageStorageService imageStorageService;
	private final CharacterSheetEditorRepository editorRepository;
	private final CharacterSheetPresenceRepository presenceRepository;

	/**
	 * Путь портрета листа от корня картинок сайта.
	 * @param id идентификатор листа
	 */
	public static String avatarPath(UUID id) {
		return "character-sheets/" + id + ".webp";
	}

	/**
	 * Активные листы с документами и история удалённых — без документов: её показывают только
	 * названием, а восстановленный лист перечитывается целиком.
	 */
	@Transactional(readOnly = true)
	public CharacterSheetListResponse findMine() {
		User user = requireUser();
		List<CharacterSheet> active = sheetRepository
				.findAllByUserIdAndDeletedAtIsNullOrderByUpdatedAtDesc(user.getId());
		Map<UUID, Long> pending = countPendingEditRequests(active);
		List<CharacterSheetResponse> items = active.stream()
				.map(sheet -> {
					CharacterSheetResponse response = toResponse(sheet);
					response.setPendingEditRequests(pending.getOrDefault(sheet.getId(), 0L).intValue());
					return response;
				})
				.toList();
		List<CharacterSheetResponse> history = sheetRepository
				.findAllByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(user.getId())
				.stream()
				.limit(CharacterSheetLimits.history(user))
				.map(CharacterSheetService::toHistoryResponse)
				.toList();
		return new CharacterSheetListResponse(items, items.size(), CharacterSheetLimits.sheets(user),
				CharacterSheetLimits.SUBSCRIBER_SHEETS, history, CharacterSheetLimits.history(user),
				CharacterSheetLimits.SUBSCRIBER_HISTORY);
	}

	/** Создаёт лист. Сверх лимита — 403: существующие листы остаются доступны. */
	@Transactional
	public CharacterSheetResponse create(String name, Map<String, Object> data) {
		User user = requireUser();
		if (data == null) {
			throw badRequest("Не переданы данные листа персонажа (data)");
		}
		validateData(data);
		requireFreeSlot(user);

		Instant now = now();
		CharacterSheet sheet = new CharacterSheet();
		sheet.setUser(user);
		sheet.setName(normalizeName(name, DEFAULT_NAME));
		sheet.setData(new LinkedHashMap<>(data));
		sheet.setCreatedAt(now);
		sheet.setUpdatedAt(now);
		return toResponse(sheetRepository.saveAndFlush(sheet));
	}

	/** Лист целиком — владельцу или редактору с выданным правом; остальным 404. */
	@Transactional(readOnly = true)
	public CharacterSheetResponse findById(UUID id) {
		User user = requireUser();
		return toAccessibleResponse(getEditable(id, user, CharacterSheetService::notFound), user);
	}

	/**
	 * Сохраняет лист (автосохранение): применяются только переданные поля. Если клиент прислал
	 * версию и она устарела — лист уже изменили в другом окне, ответ 409.
	 */
	@Transactional
	public CharacterSheetResponse update(UUID id, String name, Map<String, Object> data, Long version) {
		User user = requireUser();
		CharacterSheet sheet = getEditable(id, user, CharacterSheetService::notFound);
		if (version != null && !Objects.equals(version, sheet.getVersion())) {
			throw conflict();
		}
		if (name != null && !name.isBlank()) {
			sheet.setName(normalizeName(name, sheet.getName()));
		}
		if (data != null) {
			validateData(data);
			sheet.setData(new LinkedHashMap<>(data));
		}
		sheet.setUpdatedAt(now());
		return toAccessibleResponse(sheetRepository.saveAndFlush(sheet), user);
	}

	/**
	 * Мягкое удаление: лист уходит в историю вместе с документом и портретом, ссылка на него
	 * перестаёт открываться. История подрезается до последних листов — иначе цикл «создать →
	 * удалить» рос бы в базе без конца: лимит активных удалённые не считает.
	 */
	@Transactional
	public void delete(UUID id) {
		User user = requireUser();
		CharacterSheet sheet = getOwned(id);
		sheet.setDeletedAt(now());
		sheetRepository.saveAndFlush(sheet);
		// Удалённый лист никто не правит, а сами отметки не уйдут: их чистят только отметки того же листа.
		presenceRepository.deleteAllBySheetIdIn(List.of(sheet.getId()));
		trimHistory(user);
	}

	/**
	 * Восстановление из истории: лист снова активный, если в лимите активных есть место.
	 * @param id идентификатор листа
	 */
	@Transactional
	public CharacterSheetResponse restore(UUID id) {
		User user = requireUser();
		CharacterSheet sheet = sheetRepository.findByIdAndUserIdAndDeletedAtIsNotNull(id, user.getId())
				.orElseThrow(() -> ownerOnlyDenied(id, user));
		requireFreeSlot(user);
		sheet.setDeletedAt(null);
		sheet.setUpdatedAt(now());
		return toAccessibleResponse(sheetRepository.saveAndFlush(sheet), user);
	}

	/** Сверх лимита активных — 403: существующие листы остаются доступны. */
	private void requireFreeSlot(User user) {
		int limit = CharacterSheetLimits.sheets(user);
		if (sheetRepository.countByUserIdAndDeletedAtIsNull(user.getId()) >= limit) {
			throw new ApiException(HttpStatus.FORBIDDEN,
					"Достигнут лимит листов персонажей: " + limit + ". Удалите один из существующих");
		}
	}

	/** Самые старые удалённые листы сверх истории удаляются насовсем, с портретами и редакторами. */
	private void trimHistory(User user) {
		List<CharacterSheet> deleted = sheetRepository
				.findAllByUserIdAndDeletedAtIsNotNullOrderByDeletedAtDesc(user.getId());
		int keep = CharacterSheetLimits.history(user);
		if (deleted.size() <= keep) {
			return;
		}
		List<CharacterSheet> evicted = deleted.subList(keep, deleted.size());
		List<UUID> evictedIds = evicted.stream().map(CharacterSheet::getId).toList();
		editorRepository.deleteAllBySheetIdIn(evictedIds);
		presenceRepository.deleteAllBySheetIdIn(evictedIds);
		sheetRepository.deleteAll(evicted);
		evicted.forEach(sheet -> removeAvatarQuietly(sheet.getId()));
	}

	/**
	 * Загружает портрет листа вместо прежнего. Документ листа не меняется: адрес в него
	 * записывает клиент, иначе его автосохранение получило бы 409 из-за новой версии.
	 * @param id идентификатор листа
	 * @param file загруженный файл
	 * @return адрес с меткой версии, чтобы браузер не показал старую картинку
	 */
	@Transactional(readOnly = true)
	public CharacterSheetAvatarResponse uploadAvatar(UUID id, MultipartFile file) {
		CharacterSheet sheet = getEditable(id, requireUser(), CharacterSheetService::notFound);
		String url = imageStorageService.uploadTo(avatarPath(sheet.getId()), file, AVATAR_CACHE_CONTROL, AVATAR_MAX_SIDE)
				+ "?v=" + Instant.now().getEpochSecond();
		return new CharacterSheetAvatarResponse(url);
	}

	/**
	 * Удаляет файл портрета листа.
	 * @param id идентификатор листа
	 */
	@Transactional(readOnly = true)
	public void deleteAvatar(UUID id) {
		CharacterSheet sheet = getEditable(id, requireUser(), CharacterSheetService::notFound);
		imageStorageService.deletePath(avatarPath(sheet.getId()));
	}

	/** Файл портрета удалённого листа: сбой хранилища не мешает удалению листа. */
	private void removeAvatarQuietly(UUID id) {
		try {
			imageStorageService.deletePath(avatarPath(id));
		} catch (ApiException e) {
			log.warn("Не удалось удалить портрет листа {}: {}", id, e.getMessage());
		}
	}

	/**
	 * Активный лист текущего пользователя — для действий только владельца. Редактору листа — 403,
	 * чужой и несуществующий — 404.
	 */
	public CharacterSheet getOwned(UUID id) {
		User user = requireUser();
		return sheetRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId())
				.orElseThrow(() -> ownerOnlyDenied(id, user));
	}

	/**
	 * Активный лист, который пользователь может открыть и сохранить: свой или тот, на который владелец
	 * дал ему право. Несуществующий и удалённый — 404.
	 * @param strangerError ответ остальным: листу и сохранению — 404, отметке присутствия — 403
	 */
	public CharacterSheet getEditable(UUID id, User user, Supplier<ApiException> strangerError) {
		Optional<CharacterSheet> owned = sheetRepository.findByIdAndUserIdAndDeletedAtIsNull(id, user.getId());
		if (owned.isPresent()) {
			return owned.get();
		}
		CharacterSheet sheet = sheetRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(CharacterSheetService::notFound);
		if (!isApprovedEditor(id, user)) {
			throw strangerError.get();
		}
		return sheet;
	}

	public static boolean isOwner(CharacterSheet sheet, User user) {
		return Objects.equals(sheet.getUser().getId(), user.getId());
	}

	private boolean isApprovedEditor(UUID sheetId, User user) {
		return editorRepository.existsBySheetIdAndUserIdAndStatus(sheetId, user.getId(),
				CharacterSheetEditorStatus.APPROVED);
	}

	/** Действие только владельца не удалось: редактору — 403, чужой и несуществующий лист — 404. */
	private ApiException ownerOnlyDenied(UUID id, User user) {
		return isApprovedEditor(id, user) ? forbidden() : notFound();
	}

	/**
	 * Ответ с учётом того, кто открыл лист. Редактору токен ссылки не отдаётся: управлять доступом
	 * может только владелец, а по токену редактор раздал бы лист дальше. Владельцу активного листа —
	 * число неотвеченных запросов на редактирование.
	 */
	private CharacterSheetResponse toAccessibleResponse(CharacterSheet sheet, User user) {
		CharacterSheetResponse response = toResponse(sheet);
		if (!isOwner(sheet, user)) {
			response.setShareToken(null);
			response.setEditor(true);
		} else if (sheet.getDeletedAt() == null) {
			response.setPendingEditRequests((int) editorRepository.countBySheetIdAndStatus(sheet.getId(),
					CharacterSheetEditorStatus.PENDING));
		}
		return response;
	}

	/** Неотвеченные запросы по листам списка одним запросом. */
	private Map<UUID, Long> countPendingEditRequests(List<CharacterSheet> sheets) {
		if (sheets.isEmpty()) {
			return Map.of();
		}
		return editorRepository.countPendingBySheetIds(sheets.stream().map(CharacterSheet::getId).toList())
				.stream()
				.collect(Collectors.toMap(row -> (UUID) row[0], row -> ((Number) row[1]).longValue()));
	}

	/** Текущий пользователь; аноним — 401. */
	public User requireUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
		}
		String login = authentication.getName();
		return userRepository.findByEmailOrUsername(login, login)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
	}

	private void validateData(Map<String, Object> data) {
		int size = objectMapper.writeValueAsString(data).getBytes(StandardCharsets.UTF_8).length;
		if (size > MAX_DATA_BYTES) {
			throw badRequest("Лист персонажа слишком большой: не больше " + MAX_DATA_BYTES / 1024 + " КБ");
		}
	}

	private static String normalizeName(String name, String fallback) {
		String trimmed = name == null ? "" : name.trim();
		if (trimmed.isEmpty()) {
			return fallback;
		}
		if (trimmed.length() > MAX_NAME_LENGTH) {
			throw badRequest("Имя персонажа — не длиннее " + MAX_NAME_LENGTH + " символов");
		}
		return trimmed;
	}

	private static CharacterSheetResponse toResponse(CharacterSheet sheet) {
		return new CharacterSheetResponse(
				sheet.getId().toString(),
				sheet.getName(),
				sheet.getData(),
				sheet.getVersion() == null ? 0 : sheet.getVersion(),
				sheet.getCreatedAt().toString(),
				sheet.getUpdatedAt().toString(),
				sheet.getShareToken() == null ? null : sheet.getShareToken().toString(),
				sheet.getDeletedAt() == null ? null : sheet.getDeletedAt().toString(),
				false,
				0);
	}

	/** Лист истории: без документа, ссылка ему не нужна. */
	private static CharacterSheetResponse toHistoryResponse(CharacterSheet sheet) {
		CharacterSheetResponse response = toResponse(sheet);
		response.setData(null);
		response.setShareToken(null);
		return response;
	}

	/** Миллисекундная точность: значение в ответе совпадает с тем, что сохранится в БД. */
	private static Instant now() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

	/** Лист не найден или чужой — ответ одинаковый. */
	public static ApiException notFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "Лист персонажа не найден");
	}

	/** Редактор пытается сделать то, что можно только владельцу, или посторонний отмечается в листе. */
	public static ApiException forbidden() {
		return new ApiException(HttpStatus.FORBIDDEN, "Доступ к листу персонажа запрещен");
	}

	private static ApiException conflict() {
		return new ApiException(HttpStatus.CONFLICT,
				"Лист персонажа уже изменили в другом месте — загрузите актуальную версию");
	}

	private static ApiException badRequest(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message);
	}
}
