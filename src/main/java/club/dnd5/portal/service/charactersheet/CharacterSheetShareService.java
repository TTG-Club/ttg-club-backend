package club.dnd5.portal.service.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetShareResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetSharedResponse;
import club.dnd5.portal.dto.api.charactersheet.SavedCharacterSheetListResponse;
import club.dnd5.portal.dto.api.charactersheet.SavedCharacterSheetResponse;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.charactersheet.CharacterSheet;
import club.dnd5.portal.model.charactersheet.SavedCharacterSheet;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.charactersheet.CharacterSheetRepository;
import club.dnd5.portal.repository.charactersheet.SavedCharacterSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Доступ к листу по ссылке и «Другие листы» (как в core-api). У листа одна секретная ссылка:
 * её включает и отзывает владелец, по ней лист открывается только на чтение — запись по ссылке
 * сервер не принимает. Отзыв гасит ссылку навсегда: повторное включение выдаёт новую.
 */
@RequiredArgsConstructor
@Service
public class CharacterSheetShareService {

	private static final String HEALTH_FIELD = "health";
	private static final String CURRENT_FIELD = "current";
	private static final String TEMPORARY_FIELD = "temporary";

	private final CharacterSheetService sheetService;
	private final CharacterSheetRepository sheetRepository;
	private final SavedCharacterSheetRepository savedRepository;

	/** Включает доступ по ссылке; у уже открытого листа возвращает прежнюю ссылку. */
	@Transactional
	public CharacterSheetShareResponse share(UUID id) {
		CharacterSheet sheet = sheetService.getOwned(id);
		UUID token = sheet.getShareToken();
		if (token == null) {
			token = UUID.randomUUID();
			sheetRepository.updateShareToken(sheet.getId(), token);
		}
		return new CharacterSheetShareResponse(token.toString());
	}

	/** Отзывает ссылку: прежняя ссылка и сохранённые по ней записи больше не открывают лист. */
	@Transactional
	public void unshare(UUID id) {
		CharacterSheet sheet = sheetService.getOwned(id);
		if (sheet.getShareToken() != null) {
			sheetRepository.updateShareToken(sheet.getId(), null);
		}
	}

	/** Лист по ссылке: доступен и гостю. Неизвестная или отозванная ссылка — 404. */
	@Transactional(readOnly = true)
	public CharacterSheetSharedResponse findShared(String token) {
		CharacterSheet sheet = sheetRepository.findByShareTokenAndDeletedAtIsNull(parseToken(token))
				.orElseThrow(CharacterSheetShareService::sharedNotFound);
		Optional<User> viewer = currentUser();
		boolean own = viewer.map(user -> Objects.equals(user.getId(), sheet.getUser().getId())).orElse(false);
		String savedId = viewer
				.flatMap(user -> savedRepository.findByUserIdAndSheetId(user.getId(), sheet.getId()))
				.map(saved -> saved.getId().toString())
				.orElse(null);
		return new CharacterSheetSharedResponse(sheet.getId().toString(), sheet.getName(), sheet.getData(), own, savedId);
	}

	@Transactional(readOnly = true)
	public SavedCharacterSheetListResponse findSaved() {
		User user = sheetService.requireUser();
		List<SavedCharacterSheet> saved = savedRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
		Map<UUID, CharacterSheet> sheets = sheetRepository
				.findAllByIdIn(saved.stream().map(SavedCharacterSheet::getSheetId).toList())
				.stream()
				.collect(Collectors.toMap(CharacterSheet::getId, Function.identity()));
		List<SavedCharacterSheetResponse> items = saved.stream()
				.map(item -> toResponse(item, sheets.get(item.getSheetId())))
				.toList();
		return new SavedCharacterSheetListResponse(items, items.size(), CharacterSheetLimits.saved(user),
				CharacterSheetLimits.SUBSCRIBER_SAVED);
	}

	/**
	 * Сохраняет чужой лист по ссылке. Повторное сохранение того же листа обновляет ссылку и имя,
	 * новой записи не создаёт; лимит проверяется только для новой записи.
	 */
	@Transactional
	public SavedCharacterSheetResponse save(String token) {
		User user = sheetService.requireUser();
		CharacterSheet sheet = sheetRepository.findByShareTokenAndDeletedAtIsNull(parseToken(token))
				.orElseThrow(CharacterSheetShareService::sharedNotFound);
		if (Objects.equals(sheet.getUser().getId(), user.getId())) {
			throw new ApiException(HttpStatus.BAD_REQUEST,
					"Это ваш лист персонажа — он уже есть в списке ваших персонажей");
		}
		SavedCharacterSheet saved = savedRepository.findByUserIdAndSheetId(user.getId(), sheet.getId())
				.orElseGet(() -> newSaved(user, sheet));
		saved.setShareToken(sheet.getShareToken());
		saved.setName(sheet.getName());
		return toResponse(savedRepository.saveAndFlush(saved), sheet);
	}

	/** Убирает лист из «Других листов»: сам лист остаётся у владельца. */
	@Transactional
	public void removeSaved(UUID id) {
		User user = sheetService.requireUser();
		SavedCharacterSheet saved = savedRepository.findByIdAndUserId(id, user.getId())
				.orElseThrow(CharacterSheetShareService::savedNotFound);
		savedRepository.delete(saved);
	}

	/**
	 * Пишет в сохранённый чужой лист хиты из трекера инициативы: мастер отмечает урон, игрок видит
	 * его в своём листе. Править можно только свою сохранённую запись и только пока лист доступен
	 * (не удалён и ссылка не отозвана) — иначе 404. Меняются только текущие и (если прислано)
	 * временные хиты, остальной документ остаётся владельца. Версия листа растёт: открытый у
	 * владельца лист получит 409 при автосохранении и перечитается уже с этими хитами.
	 * @param savedId идентификатор сохранённой записи
	 * @param current текущие хиты; зажимаются максимумом листа
	 * @param temporary временные хиты; null — не менять
	 */
	@Transactional
	public void updateSavedHealth(UUID savedId, int current, Integer temporary) {
		User user = sheetService.requireUser();
		SavedCharacterSheet saved = savedRepository.findByIdAndUserId(savedId, user.getId())
				.orElseThrow(CharacterSheetShareService::savedNotFound);
		CharacterSheet sheet = sheetRepository.findById(saved.getSheetId())
				.filter(found -> found.getDeletedAt() == null
						&& Objects.equals(found.getShareToken(), saved.getShareToken()))
				.orElseThrow(CharacterSheetShareService::sharedNotFound);
		sheet.setData(withHealth(sheet.getData(), current, temporary));
		sheet.setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
		sheetRepository.saveAndFlush(sheet);
	}

	/**
	 * Копия документа с новыми хитами. Именно копия (и документа, и здоровья): иначе Hibernate
	 * сравнил бы изменённую на месте карту саму с собой и не заметил правки. Сверху хиты не режем:
	 * настоящий максимум листа больше {@code health.max} на бонусы черт и эффектов, его считает
	 * клиент по документу — обрезка по {@code health.max} недодавала бы хиты при лечении.
	 */
	private static Map<String, Object> withHealth(Map<String, Object> data, int current, Integer temporary) {
		if (!(data.get(HEALTH_FIELD) instanceof Map<?, ?> health)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "В листе персонажа нет хитов");
		}
		Map<Object, Object> nextHealth = new LinkedHashMap<>(health);
		nextHealth.put(CURRENT_FIELD, Math.max(0, current));
		if (temporary != null) {
			nextHealth.put(TEMPORARY_FIELD, Math.max(0, temporary));
		}
		Map<String, Object> document = new LinkedHashMap<>(data);
		document.put(HEALTH_FIELD, nextHealth);
		return document;
	}

	private SavedCharacterSheet newSaved(User user, CharacterSheet sheet) {
		int limit = CharacterSheetLimits.saved(user);
		if (savedRepository.countByUserId(user.getId()) >= limit) {
			throw new ApiException(HttpStatus.FORBIDDEN,
					"Достигнут лимит сохранённых листов: " + limit + ". Уберите один из сохранённых");
		}
		SavedCharacterSheet saved = new SavedCharacterSheet();
		saved.setUserId(user.getId());
		saved.setSheetId(sheet.getId());
		saved.setCreatedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
		return saved;
	}

	/** Запись доступна, пока лист есть и его ссылка совпадает с сохранённой. */
	private static SavedCharacterSheetResponse toResponse(SavedCharacterSheet saved, CharacterSheet sheet) {
		boolean available = sheet != null
				&& sheet.getDeletedAt() == null
				&& Objects.equals(sheet.getShareToken(), saved.getShareToken());
		return new SavedCharacterSheetResponse(
				saved.getId().toString(),
				saved.getSheetId().toString(),
				saved.getShareToken().toString(),
				available ? sheet.getName() : saved.getName(),
				available ? sheet.getData() : null,
				available);
	}

	/** Пользователь, если вошёл; гость — пусто (ссылка открывается и без входа). */
	private Optional<User> currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
				|| !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}
		return Optional.of(sheetService.requireUser());
	}

	/** Неразборчивая ссылка — такого листа точно нет. */
	private static UUID parseToken(String token) {
		try {
			return UUID.fromString(token);
		} catch (IllegalArgumentException | NullPointerException e) {
			throw sharedNotFound();
		}
	}

	private static ApiException savedNotFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "Сохранённый лист не найден");
	}

	private static ApiException sharedNotFound() {
		return new ApiException(HttpStatus.NOT_FOUND, "Лист персонажа по этой ссылке не найден");
	}
}
