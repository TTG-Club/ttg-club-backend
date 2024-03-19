package club.dnd5.portal.controller.api;


import club.dnd5.portal.service.LssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("api/v1/lss")
@Tag(name = "API для интеграции с LSS", description = "LSS Integration API")
@RequiredArgsConstructor
public class LssApiController {
	private final LssService lssService;

	@Operation(summary = "Получение заклинание в LSS формате, по английскому имени")
	@GetMapping(value = "spell/{englishName}", produces="application/json")
	public String findByName(@PathVariable String englishName) {
		return lssService.findByName(englishName);
	}

	@Operation(summary = "Получение всех заклинаний в LSS формате")
	@GetMapping(value = "spell/all", produces="application/json")
	public List<String> getAllSpellForLSS() {
		return lssService.getAllSpellForLSS();
	}
}
