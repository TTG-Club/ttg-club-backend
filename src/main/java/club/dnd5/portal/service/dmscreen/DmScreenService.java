package club.dnd5.portal.service.dmscreen;

import club.dnd5.portal.dto.api.dmscreen.DmScreenListResponse;
import club.dnd5.portal.dto.api.dmscreen.DmScreenResponse;
import club.dnd5.portal.dto.api.dmscreen.DmScreenShortResponse;
import club.dnd5.portal.dto.api.dmscreen.DmScreenWindow;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.dmscreen.DmScreen;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.dmscreen.DmScreenRepository;
import club.dnd5.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Ширмы Мастера: именованные раскладки окон пользователя. На время испытаний у пользователя {@value SCREEN_LIMIT} ширма, от подписки и роли лимит не
 * зависит. Несколько ширм вернутся, когда инструмент будет доведён.
 * Чужая и несуществующая ширма неразличимы — обе 404.
 */
@RequiredArgsConstructor
@Service
public class DmScreenService {

	public static final int SCREEN_LIMIT = 1;
	public static final int MAX_NAME_LENGTH = 64;
	public static final int MAX_WINDOWS = 100;
	public static final int MAX_PATH_LENGTH = 256;
	public static final int MAX_TITLE_LENGTH = 256;
	public static final int MAX_WINDOW_ID_LENGTH = 64;
	/** Начало пути окна-заметки: у заметки нет страницы на сайте. */
	public static final String NOTE_PATH_PREFIX = "note:";
	public static final int MAX_NOTE_LENGTH = 20000;

	private static final TypeReference<List<DmScreenWindow>> WINDOWS_TYPE = new TypeReference<>() {
	};

	private final DmScreenRepository screenRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Transactional(readOnly = true)
	public DmScreenListResponse findMine() {
		User user = requireUser();
		List<DmScreenShortResponse> items = screenRepository.findAllByUserIdOrderByUpdatedAtDesc(user.getId())
				.stream()
				.map(screen -> new DmScreenShortResponse(
						screen.getId().toString(),
						screen.getName(),
						readWindows(screen).size(),
						screen.getUpdatedAt().toString()))
				.toList();
		return new DmScreenListResponse(items, items.size(), currentLimit());
	}

	/**
	 * Создаёт ширму. Если ширм уже не меньше лимита (в т.ч. больше — созданных при прежнем лимите),
	 * существующие остаются доступны, но новую создать нельзя.
	 */
	@Transactional
	public DmScreenResponse create(String name, List<DmScreenWindow> windows) {
		User user = requireUser();
		String normalizedName = normalizeName(name);
		List<DmScreenWindow> normalizedWindows = windows == null ? Collections.emptyList() : windows;
		validateWindows(normalizedWindows);

		int limit = currentLimit();
		if (screenRepository.countByUserId(user.getId()) >= limit) {
			throw new ApiException(HttpStatus.FORBIDDEN,
					"Ширма уже есть: пока можно вести только одну");
		}

		DmScreen screen = new DmScreen();
		screen.setUser(user);
		screen.setName(normalizedName);
		screen.setWindows(writeWindows(normalizedWindows));
		screen.setUpdatedAt(now());
		return toResponse(screenRepository.save(screen));
	}

	@Transactional(readOnly = true)
	public DmScreenResponse findById(UUID id) {
		return toResponse(getOwned(id));
	}

	@Transactional
	public DmScreenResponse rename(UUID id, String name) {
		DmScreen screen = getOwned(id);
		screen.setName(normalizeName(name));
		screen.setUpdatedAt(now());
		return toResponse(screen);
	}

	@Transactional
	public void replaceWindows(UUID id, List<DmScreenWindow> windows) {
		DmScreen screen = getOwned(id);
		if (windows == null) {
			throw badRequest("Ожидается массив окон");
		}
		validateWindows(windows);
		screen.setWindows(writeWindows(windows));
		screen.setUpdatedAt(now());
	}

	@Transactional
	public void delete(UUID id) {
		screenRepository.delete(getOwned(id));
	}

	/** Лимит ширм: один для всех пользователей. */
	public int currentLimit() {
		return SCREEN_LIMIT;
	}

	private DmScreen getOwned(UUID id) {
		User user = requireUser();
		return screenRepository.findByIdAndUserId(id, user.getId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ширма не найдена"));
	}

	private User requireUser() {
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

	private static String normalizeName(String name) {
		String trimmed = name == null ? "" : name.trim();
		if (trimmed.isEmpty()) {
			throw badRequest("Укажите название ширмы");
		}
		if (trimmed.length() > MAX_NAME_LENGTH) {
			throw badRequest("Название ширмы — не длиннее " + MAX_NAME_LENGTH + " символов");
		}
		return trimmed;
	}

	private static void validateWindows(List<DmScreenWindow> windows) {
		if (windows.size() > MAX_WINDOWS) {
			throw badRequest("На ширме не больше " + MAX_WINDOWS + " окон");
		}
		for (int i = 0; i < windows.size(); i++) {
			validateWindow(windows.get(i), i);
		}
	}

	private static void validateWindow(DmScreenWindow window, int index) {
		String prefix = "windows[" + index + "]";
		if (window == null) {
			throw badRequest(prefix + ": окно не может быть пустым");
		}
		if (window.getId() == null || window.getId().isBlank() || window.getId().length() > MAX_WINDOW_ID_LENGTH) {
			throw badRequest(prefix + ".id: обязательная строка до " + MAX_WINDOW_ID_LENGTH + " символов");
		}
		String path = window.getPath();
		boolean isNote = path != null && path.startsWith(NOTE_PATH_PREFIX);
		if (path == null || !(path.startsWith("/") || isNote) || path.length() > MAX_PATH_LENGTH) {
			throw badRequest(prefix + ".path: путь начинается с «/» или «" + NOTE_PATH_PREFIX + "», не длиннее "
				+ MAX_PATH_LENGTH + " символов");
		}
		String content = window.getContent();
		if (content != null && (!isNote || content.length() > MAX_NOTE_LENGTH)) {
			throw badRequest(prefix + ".content: текст есть только у заметки, не длиннее " + MAX_NOTE_LENGTH
				+ " символов");
		}
		if (window.getTitle() == null || window.getTitle().length() > MAX_TITLE_LENGTH) {
			throw badRequest(prefix + ".title: обязательная строка до " + MAX_TITLE_LENGTH + " символов");
		}
		requireFinite(window.getX(), prefix + ".x");
		requireFinite(window.getY(), prefix + ".y");
		requireFinite(window.getWidth(), prefix + ".width");
		requireFinite(window.getHeight(), prefix + ".height");
		if (window.getLayer() == null) {
			throw badRequest(prefix + ".layer: обязательное целое число");
		}
		if (window.getIsMinimized() == null) {
			throw badRequest(prefix + ".isMinimized: обязательный признак");
		}
	}

	private static void requireFinite(Double value, String field) {
		if (value == null || !Double.isFinite(value)) {
			throw badRequest(field + ": обязательное число");
		}
	}

	private DmScreenResponse toResponse(DmScreen screen) {
		return new DmScreenResponse(
				screen.getId().toString(),
				screen.getName(),
				readWindows(screen),
				screen.getUpdatedAt().toString());
	}

	private List<DmScreenWindow> readWindows(DmScreen screen) {
		String json = screen.getWindows();
		if (json == null || json.isBlank()) {
			return Collections.emptyList();
		}
		try {
			return objectMapper.readValue(json, WINDOWS_TYPE);
		} catch (JacksonException e) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось прочитать окна ширмы");
		}
	}

	private String writeWindows(List<DmScreenWindow> windows) {
		return objectMapper.writeValueAsString(windows);
	}

	/** Миллисекундная точность: значение в ответе совпадает с тем, что сохранится в БД. */
	private static Instant now() {
		return Instant.now().truncatedTo(ChronoUnit.MILLIS);
	}

	private static ApiException badRequest(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message);
	}
}
