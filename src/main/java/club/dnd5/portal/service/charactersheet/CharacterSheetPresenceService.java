package club.dnd5.portal.service.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetPresenceResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetPresenceUserResponse;
import club.dnd5.portal.model.charactersheet.CharacterSheet;
import club.dnd5.portal.model.charactersheet.CharacterSheetPresence;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.charactersheet.CharacterSheetPresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Совместная правка листа (как в core-api): кто держит его открытым и какая версия сейчас на сервере.
 * Клиент шлёт отметку, пока лист открыт, и по версии в ответе подтягивает чужие правки. Запретов нет:
 * устаревшее сохранение по-прежнему получает 409.
 * <p>
 * Отмечаться может тот же круг, что открывает лист на правку: владелец и редакторы с выданным
 * правом; остальным — 403. Отметка пишется в отдельную таблицу и версию листа не трогает.
 */
@RequiredArgsConstructor
@Service
public class CharacterSheetPresenceService {

	/**
	 * Сколько отметка считается живой. Клиент отмечается чаще (раз в 20 секунд), поэтому один
	 * пропущенный запрос не гасит предупреждение у соседа.
	 */
	public static final Duration PRESENCE_TTL = Duration.ofSeconds(45);

	private final CharacterSheetService sheetService;
	private final CharacterSheetEditorService editorService;
	private final CharacterSheetPresenceRepository presenceRepository;

	/**
	 * Отмечает, что лист открыт у текущего пользователя, и возвращает остальных, у кого он открыт,
	 * вместе с текущей версией листа. Заодно чистит давно не освежавшиеся отметки этого листа:
	 * ушедшие без прощания (закрытая вкладка, пропавшая сеть) иначе копились бы в таблице.
	 */
	@Transactional
	public CharacterSheetPresenceResponse heartbeat(UUID sheetId) {
		User user = sheetService.requireUser();
		CharacterSheet sheet = sheetService.getEditable(sheetId, user, CharacterSheetService::forbidden);
		Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
		Instant freshSince = now.minus(PRESENCE_TTL);
		presenceRepository.deleteStale(sheet.getId(), freshSince);
		presenceRepository.upsert(UUID.randomUUID().toString(), sheet.getId().toString(), user.getId(), now);
		List<CharacterSheetPresence> others = presenceRepository
				.findAllBySheetIdAndUserIdNotAndSeenAtAfter(sheet.getId(), user.getId(), freshSince);
		return new CharacterSheetPresenceResponse(toUsers(others),
				sheet.getVersion() == null ? 0 : sheet.getVersion());
	}

	/**
	 * Снимает отметку текущего пользователя — лист закрыт. Без проверки доступа: убрать можно только
	 * свою отметку, а редактор, у которого право уже отозвали, тоже должен уметь уйти.
	 */
	@Transactional
	public void leave(UUID sheetId) {
		User user = sheetService.requireUser();
		presenceRepository.deleteBySheetIdAndUserId(sheetId, user.getId());
	}

	private List<CharacterSheetPresenceUserResponse> toUsers(List<CharacterSheetPresence> presences) {
		if (presences.isEmpty()) {
			return List.of();
		}
		Map<Long, String> names = editorService.displayNames(
				presences.stream().map(CharacterSheetPresence::getUserId).toList());
		return presences.stream()
				.map(presence -> new CharacterSheetPresenceUserResponse(
						names.getOrDefault(presence.getUserId(), CharacterSheetEditorService.UNNAMED_USER)))
				.toList();
	}
}
