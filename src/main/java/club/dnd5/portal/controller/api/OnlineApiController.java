package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.online.OnlineHeartbeatRequest;
import club.dnd5.portal.service.OnlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OnlineApiController {
	private final OnlineService onlineService;

	@PostMapping("/api/online/heartbeat")
	public Map heartbeat(@Valid @RequestBody OnlineHeartbeatRequest request) {
		return onlineService.heartbeat(request);
	}
}
