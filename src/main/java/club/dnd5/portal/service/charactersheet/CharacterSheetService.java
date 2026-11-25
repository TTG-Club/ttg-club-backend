package club.dnd5.portal.service.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetAvatarResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetListResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetResponse;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.charactersheet.CharacterSheet;
import club.dnd5.portal.model.user.User;
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
import java.util.UUID;

/**
 * Листы персонажей пользователя. Документ листа сервер не разбирает — только хранит,
 * проверяя размер. Чужой и несуществующий лист неразличимы — оба 404.
 * <p>
 * Портрет персонажа — один файл на лист ({@link #avatarPath}): адрес хранит сам документ листа,
 * а файл стирается вместе с листом.
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

	/**
	 * Путь портрета листа от корня картинок сайта.
	 * @param id идентификатор листа
	 */
	public static String avatarPath(UUID id) {
		return "character-sheets/" + id + ".webp";
	}

	@Transactional(readOnly = true)
	public CharacterSheetListResponse findMine() {
		User user = requireUser();
		List<CharacterSheetResponse> items = sheetRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getId())
				.stream()
				.map(CharacterSheetService::toResponse)
				.toList();
		return new CharacterSheetListResponse(items, items.size(), CharacterSheetLimits.sheets(user),
				CharacterSheetLimits.SUBSCRIBER_SHEETS);
	}

	/** Создаёт лист. Сверх лимита — 403: существующие листы остаются доступны. */
	@Transactional
	public CharacterSheetResponse create(String name, Map<String, Object> data) {
		User user = requireUser();
		if (data == null) {
			throw badRequest("Не переданы данные листа персонажа (data)");
		}
		validateData(data);
		int limit = CharacterSheetLimits.sheets(user);
		if (sheetRepository.countByUserId(user.getId()) >= limit) {
			throw new ApiException(HttpStatus.FORBIDDEN,
					"Достигнут лимит листов персонажей: " + limit + ". Удалите один из существующих");
		}

		Instant now = now();
		CharacterSheet sheet = new CharacterSheet();
		sheet.setUser(user);
		sheet.setName(normalizeName(name, DEFAULT_NAME));
		sheet.setData(new LinkedHashMap<>(data));
		sheet.setCreatedAt(now);
		sheet.setUpdatedAt(now);
		return toResponse(sheetRepository.saveAndFlush(sheet));
	}

	@Transactional(readOnly = true)
	public CharacterSheetResponse findById(UUID id) {
		return toResponse(getOwned(id));
	}

	/**
	 * Сохраняет лист (автосохранение): применяются только переданные поля. Если клиент прислал
	 * версию и она устарела — лист уже изменили в другом окне, ответ 409.
	 */
	@Transactional
	public CharacterSheetResponse update(UUID id, String name, Map<String, Object> data, Long version) {
		CharacterSheet sheet = getOwned(id);
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
		return toResponse(sheetRepository.saveAndFlush(sheet));
	}

	@Transactional
	public void delete(UUID id) {
		sheetRepository.delete(getOwned(id));
		removeAvatarQuietly(id);
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
		CharacterSheet sheet = getOwned(id);
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
		imageStorageService.deletePath(avatarPath(getOwned(id).getId()));
	}

	/** Файл портрета удалённого листа: сбой хранилища не мешает удалению листа. */
	private void removeAvatarQuietly(UUID id) {
		try {
			imageStorageService.deletePath(avatarPath(id));
		} catch (ApiException e) {
			log.warn("Не удалось удалить портрет листа {}: {}", id, e.getMessage());
		}
	}

	/** Лист текущего пользователя; чужой и несуществующий — 404. */
	public CharacterSheet getOwned(UUID id) {
		User user = requireUser();
		return sheetRepository.findByIdAndUserId(id, user.getId())
				.orElseThrow(CharacterSheetService::notFound);
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
				sheet.getShareToken() == null ? null : sheet.getShareToken().toString());
	}

	/** Миллисекундная точность: значение в ответе совпадает с тем, что сохранится в БД. */
	private static Instant now() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

	/** Лист не найден или чужой — ответ одинаковый. */
	public static ApiException notFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "Лист персонажа не найден");
	}

	private static ApiException conflict() {
		return new ApiException(HttpStatus.CONFLICT,
				"Лист персонажа уже изменили в другом месте — загрузите актуальную версию");
	}

	private static ApiException badRequest(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message);
	}
}
