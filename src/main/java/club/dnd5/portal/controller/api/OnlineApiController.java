package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.online.OnlineHeartbeatRequest;
import club.dnd5.portal.dto.online.OnlineUsageRequest;
import club.dnd5.portal.service.OnlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "Онлайн", description = "API учёта посетителей и времени работы с инструментами")
@RestController
@RequiredArgsConstructor
public class OnlineApiController {
	private final OnlineService onlineService;

	@PostMapping("/api/online/heartbeat")
	public Map heartbeat(@Valid @RequestBody OnlineHeartbeatRequest request) {
		return onlineService.heartbeat(request);
	}

	@Operation(summary = "Отметка о времени работы с инструментом сайта")
	@PostMapping("/api/online/usage")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void usage(@Valid @RequestBody OnlineUsageRequest request) {
		onlineService.usage(request);
	}
}
