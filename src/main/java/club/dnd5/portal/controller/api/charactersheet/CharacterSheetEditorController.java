package club.dnd5.portal.controller.api.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditAccessListResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditRequestCountResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditRequestResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetEditorListResponse;
import club.dnd5.portal.service.charactersheet.CharacterSheetEditorService;
import club.dnd5.portal.service.charactersheet.CharacterSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Права на редактирование листа: запрос от сохранившего лист по ссылке, решение владельца и счётчик
 * входящих запросов. Только авторизованным (правило в WebSecurityConfig).
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/character-sheets")
@Tag(name = "Листы персонажей: редакторы", description = "Права на редактирование листа: запрос, разрешение, отказ и отзыв")
public class CharacterSheetEditorController {

	private final CharacterSheetEditorService editorService;

	@Operation(summary = "Запрос владельцу на редактирование сохранённого листа. Повтор не дублирует запрос; "
			+ "чужая запись, отозванная ссылка и удалённый лист — 404")
	@PostMapping("/saved/{savedId}/edit-request")
	public CharacterSheetEditRequestResponse requestEdit(@PathVariable final String savedId) {
		return editorService.requestEdit(parseId(savedId));
	}

	@Operation(summary = "Права на редактирование сохранённых листов — лёгкая сводка без документов")
	@GetMapping("/saved/edit-access")
	public CharacterSheetEditAccessListResponse findMyEditAccess() {
		return editorService.findMyEditAccess();
	}

	@Operation(summary = "Неотвеченные запросы на активные листы текущего пользователя")
	@GetMapping("/edit-requests/count")
	public CharacterSheetEditRequestCountResponse countIncoming() {
		return editorService.countIncoming();
	}

	@Operation(summary = "Запросы и редакторы листа, старые первее: только владельцу, редактору — 403")
	@GetMapping("/{id}/editors")
	public CharacterSheetEditorListResponse findEditors(@PathVariable final String id) {
		return editorService.findEditors(parseId(id));
	}

	@Operation(summary = "Разрешить редактирование: до 5 редакторов на лист, сверх — 409")
	@PostMapping("/{id}/editors/{editorId}/approve")
	public CharacterSheetEditorListResponse approve(@PathVariable final String id,
													@PathVariable final String editorId) {
		return editorService.approve(parseId(id), parseEditorId(editorId));
	}

	@Operation(summary = "Отклонить запрос или отозвать право: запись удаляется, попросить заново можно сразу")
	@DeleteMapping("/{id}/editors/{editorId}")
	public CharacterSheetEditorListResponse remove(@PathVariable final String id,
												   @PathVariable final String editorId) {
		return editorService.remove(parseId(id), parseEditorId(editorId));
	}

	private static UUID parseId(String id) {
		try {
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw CharacterSheetService.notFound();
		}
	}

	private static UUID parseEditorId(String id) {
		try {
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw CharacterSheetEditorService.editorNotFound();
		}
	}
}
