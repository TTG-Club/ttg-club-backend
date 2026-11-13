package club.dnd5.portal.controller.api.dmscreen;

import club.dnd5.portal.dto.api.dmscreen.DmScreenCreateRequest;
import club.dnd5.portal.dto.api.dmscreen.DmScreenListResponse;
import club.dnd5.portal.dto.api.dmscreen.DmScreenRenameRequest;
import club.dnd5.portal.dto.api.dmscreen.DmScreenResponse;
import club.dnd5.portal.dto.api.dmscreen.DmScreenWindow;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.service.dmscreen.DmScreenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Ширмы Мастера текущего пользователя. Доступ — только авторизованным (правило в WebSecurityConfig);
 * чужая и несуществующая ширма — 404.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/dm-screens")
@Tag(name = "Ширма Мастера", description = "Несколько ширм Мастера на пользователя: имя и раскладка окон")
public class DmScreenController {

	private final DmScreenService screenService;

	@Operation(summary = "Ширмы пользователя (последние изменённые первее), сколько занято и лимит")
	@GetMapping
	public DmScreenListResponse findMine() {
		return screenService.findMine();
	}

	@Operation(summary = "Создание ширмы: у пользователя одна ширма; вторая — 403")
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public DmScreenResponse create(@RequestBody @Valid final DmScreenCreateRequest request) {
		return screenService.create(request.getName(), request.getWindows());
	}

	@Operation(summary = "Ширма с окнами")
	@GetMapping("/{id}")
	public DmScreenResponse findById(@PathVariable final String id) {
		return screenService.findById(parseId(id));
	}

	@Operation(summary = "Переименование ширмы")
	@PatchMapping("/{id}")
	public DmScreenResponse rename(@PathVariable final String id,
								   @RequestBody @Valid final DmScreenRenameRequest request) {
		return screenService.rename(parseId(id), request.getName());
	}

	@Operation(summary = "Замена раскладки окон ширмы (до 100 окон)")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PutMapping("/{id}/windows")
	public void replaceWindows(@PathVariable final String id,
							   @RequestBody final List<DmScreenWindow> windows) {
		screenService.replaceWindows(parseId(id), windows);
	}

	@Operation(summary = "Удаление ширмы")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")
	public void delete(@PathVariable final String id) {
		screenService.delete(parseId(id));
	}

	/** Неразборчивый id — такой ширмы точно нет: 404, как для чужой. */
	private static UUID parseId(String id) {
		try {
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw new ApiException(HttpStatus.NOT_FOUND, "Ширма не найдена");
		}
	}
}
