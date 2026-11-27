package club.dnd5.portal.controller.api.charactersheet;

import club.dnd5.portal.dto.api.charactersheet.CharacterSheetAvatarResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetListResponse;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetRequest;
import club.dnd5.portal.dto.api.charactersheet.CharacterSheetResponse;
import club.dnd5.portal.service.charactersheet.CharacterSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.UUID;

/**
 * Листы персонажей текущего пользователя. Доступ — только авторизованным (правило в WebSecurityConfig);
 * чужой и несуществующий лист — 404.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/character-sheets")
@Tag(name = "Листы персонажей", description = "Листы персонажей пользователя: документ листа хранится целиком")
public class CharacterSheetController {

	private final CharacterSheetService sheetService;

	@Operation(summary = "Листы пользователя с документами (последние изменённые первее), сколько занято и лимит")
	@GetMapping
	public CharacterSheetListResponse findMine() {
		return sheetService.findMine();
	}

	@Operation(summary = "Создание листа; сверх лимита — 403")
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public CharacterSheetResponse create(@RequestBody @Valid final CharacterSheetRequest request) {
		return sheetService.create(request.getName(), request.getData());
	}

	@Operation(summary = "Лист персонажа")
	@GetMapping("/{id}")
	public CharacterSheetResponse findById(@PathVariable final String id) {
		return sheetService.findById(parseId(id));
	}

	@Operation(summary = "Сохранение листа; устаревшая версия — 409")
	@PutMapping("/{id}")
	public CharacterSheetResponse update(@PathVariable final String id,
										 @RequestBody @Valid final CharacterSheetRequest request) {
		return sheetService.update(parseId(id), request.getName(), request.getData(), request.getVersion());
	}

	@Operation(summary = "Удаление листа")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")
	public void delete(@PathVariable final String id) {
		sheetService.delete(parseId(id));
	}

	@Operation(summary = "Восстановление листа из истории удалённых; сверх лимита активных — 403")
	@PostMapping("/{id}/restore")
	public CharacterSheetResponse restore(@PathVariable final String id) {
		return sheetService.restore(parseId(id));
	}

	@Operation(summary = "Загрузка портрета персонажа (WebP до 512 px, файл листа перезаписывается)")
	@PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public CharacterSheetAvatarResponse uploadAvatar(@PathVariable final String id,
													 @RequestPart("file") final MultipartFile file) {
		return sheetService.uploadAvatar(parseId(id), file);
	}

	@Operation(summary = "Удаление портрета персонажа")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}/avatar")
	public void deleteAvatar(@PathVariable final String id) {
		sheetService.deleteAvatar(parseId(id));
	}

	/** Неразборчивый id — такого листа точно нет: 404, как для чужого. */
	private static UUID parseId(String id) {
		try {
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw CharacterSheetService.notFound();
		}
	}
}
