package club.dnd5.portal.controller.api.menu;

import club.dnd5.portal.dto.api.menu.MenuApi;
import club.dnd5.portal.repository.MenuRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Меню сайта", description = "The Menu API")
@RestController
@RequestMapping("/api/v1/menu")
public class MenuApiController {
	private final MenuRepository menuRepository;

	/**
	 * Меню меняется редко: ответ кэшируется браузером и переспрашивается по ETag
	 * (см. {@link club.dnd5.portal.config.EtagConfig}), неизменившееся меню приходит как 304.
	 */
	@Operation(summary = "Получение списка элементов меню")
	@GetMapping
	public ResponseEntity<List<MenuApi>> getMenu() {
		List<MenuApi> menu = menuRepository
			.findAll()
			.stream()
			.filter(item -> !item.getChildren().isEmpty())
			.map(MenuApi::new)
			.collect(Collectors.toList());
		return ResponseEntity.ok()
			.cacheControl(CacheControl.noCache())
			.body(menu);
	}
}
