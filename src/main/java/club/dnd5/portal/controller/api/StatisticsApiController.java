package club.dnd5.portal.controller.api;

import club.dnd5.portal.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Статистика", description = "API по статистике сайта")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsApiController {
	private final StatisticsService statisticsService;

	@Operation(summary = "Общее число материалов сайта")
	@GetMapping("/count-all")
	public Long countAllMaterials() {
		return statisticsService.countAllMaterials();
	}
}
