package club.dnd5.portal.controller.api;

import club.dnd5.portal.service.LssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/lss")
@Tag(name = "API для интеграции с LSS", description = "LSS Integration API")
@RequiredArgsConstructor
public class LssApiController {
	private final LssService lssService;

	@Operation(summary = "Получение заклинание в LSS формате по айди")
	@GetMapping(value = "/spell", produces = "application/json")
	public String findByIdAndFoundryVersion(@RequestParam("id") Integer id) {
		return lssService.findById(id);
	}
}
