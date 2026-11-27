package club.dnd5.portal.controller.api.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetShareResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetSharedResponse;
import club.dnd5.portal.dto.api.charactersheet.SavedCharacterSheetHealthRequest;
import club.dnd5.portal.dto.api.charactersheet.SavedCharacterSheetListResponse;
import club.dnd5.portal.dto.api.charactersheet.SavedCharacterSheetRequest;
import club.dnd5.portal.dto.api.charactersheet.SavedCharacterSheetResponse;
import club.dnd5.portal.service.charactersheet.CharacterSheetService;
import club.dnd5.portal.service.charactersheet.CharacterSheetShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Доступ к листу по ссылке и «Другие листы». Лист по ссылке открыт и гостю (правило в
 * WebSecurityConfig), остальное — только авторизованным.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/character-sheets")
@Tag(name = "Листы персонажей: доступ по ссылке", description = "Ссылка на лист и чужие листы, сохранённые по ссылке")
public class CharacterSheetShareController {

	private final CharacterSheetShareService shareService;

	@Operation(summary = "Включение доступа по ссылке; у открытого листа — прежняя ссылка")
	@PostMapping("/{id}/share")
	public CharacterSheetShareResponse share(@PathVariable final String id) {
		return shareService.share(parseId(id));
	}

	@Operation(summary = "Отзыв ссылки: прежняя ссылка больше не открывает лист")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}/share")
	public void unshare(@PathVariable final String id) {
		shareService.unshare(parseId(id));
	}

	@Operation(summary = "Лист по ссылке (только чтение, доступен гостю); отозванная ссылка — 404")
	@GetMapping("/shared/{token}")
	public CharacterSheetSharedResponse findShared(@PathVariable final String token) {
		return shareService.findShared(token);
	}

	@Operation(summary = "Чужие листы, сохранённые по ссылке, сколько занято и лимит")
	@GetMapping("/saved")
	public SavedCharacterSheetListResponse findSaved() {
		return shareService.findSaved();
	}

	@Operation(summary = "Сохранение чужого листа по ссылке; сверх лимита — 403")
	@PostMapping("/saved")
	public SavedCharacterSheetResponse save(@RequestBody @Valid final SavedCharacterSheetRequest request) {
		return shareService.save(request.shareToken());
	}

	@Operation(summary = "Удаление листа из сохранённых: сам лист остаётся у владельца")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/saved/{id}")
	public void removeSaved(@PathVariable final String id) {
		shareService.removeSaved(parseId(id));
	}

	@Operation(summary = "Текущие и временные хиты сохранённого чужого листа: их отмечает мастер в трекере "
			+ "инициативы. Меняются только health.current и health.temporary (если прислано), текущие "
			+ "зажимаются health.max. Отозванная ссылка, удалённый лист и чужая запись — 404")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PatchMapping("/saved/{id}/health")
	public void updateSavedHealth(@PathVariable final String id,
								  @RequestBody @Valid final SavedCharacterSheetHealthRequest request) {
		shareService.updateSavedHealth(parseId(id), request.current(), request.temporary());
	}

	private static UUID parseId(String id) {
		try {
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw CharacterSheetService.notFound();
		}
	}
}
