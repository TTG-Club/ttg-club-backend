package club.dnd5.portal.controller.api.menu;

import club.dnd5.portal.dto.api.menu.MenuApi;
import club.dnd5.portal.repository.MenuRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Меню сайта", description = "The Menu API")
@RestController
@RequestMapping("/api/v1/menu")
public class MenuApiController {
	private final MenuRepository menuRepository;

	@Operation(summary = "Получение списка элементов меню")
	@GetMapping
	@ResponseStatus(code = HttpStatus.OK)
	public List<MenuApi> getMenu() {
		return menuRepository
			.findAll()
			.stream()
			.filter(item -> !item.getChildren().isEmpty())
			.map(MenuApi::new)
			.collect(Collectors.toList());
	}
}
