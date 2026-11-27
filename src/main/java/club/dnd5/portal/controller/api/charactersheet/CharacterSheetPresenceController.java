package club.dnd5.portal.controller.api.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetPresenceResponse;
import club.dnd5.portal.service.charactersheet.CharacterSheetPresenceService;
import club.dnd5.portal.service.charactersheet.CharacterSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Мягкая блокировка листа: отметка «лист открыт у меня» и список тех, у кого он открыт ещё.
 * Отмечаться могут владелец и редакторы с выданным правом.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/character-sheets")
@Tag(name = "Листы персонажей: присутствие", description = "Кто сейчас держит лист открытым на правку")
public class CharacterSheetPresenceController {

	private final CharacterSheetPresenceService presenceService;

	@Operation(summary = "Отметить, что лист открыт, и узнать, у кого ещё он открыт и какая версия листа "
			+ "на сервере. Слать, пока лист открыт: отметка живёт 45 секунд. Версию листа не меняет")
	@PostMapping("/{id}/presence")
	public CharacterSheetPresenceResponse heartbeat(@PathVariable final String id) {
		return presenceService.heartbeat(parseId(id));
	}

	@Operation(summary = "Снять свою отметку — лист закрыт")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}/presence")
	public void leave(@PathVariable final String id) {
		presenceService.leave(parseId(id));
	}

	private static UUID parseId(String id) {
		try {
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw CharacterSheetService.notFound();
		}
	}
}
