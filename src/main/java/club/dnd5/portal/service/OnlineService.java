package club.dnd5.portal.service;

import club.dnd5.portal.dto.online.OnlineHeartbeatRequest;
import club.dnd5.portal.dto.online.OnlineUsageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OnlineService {
	private static final String ONLINE_SERVICE_TOKEN_HEADER = "X-Online-Token";
	private static final String HEARTBEAT_PATH = "/api/v1/online/heartbeat";
	private static final String USAGE_PATH = "/api/v1/online/usage";

	private final RestTemplate restTemplate = new RestTemplate();
	private final String apiUrl;
	private final String apiToken;
	private final String siteId;

	public OnlineService(
		@Value("${online.api.url:}") String apiUrl,
		@Value("${online.api.token:}") String apiToken,
		@Value("${online.site-id:}") String siteId) {
		this.apiUrl = trimTrailingSlash(apiUrl);
		this.apiToken = apiToken;
		this.siteId = siteId;
	}

	public Map heartbeat(OnlineHeartbeatRequest request) {
		validateConfig();

		Map<String, Object> body = new HashMap<>();
		body.put("siteId", siteId);
		body.put("key", request.getKey());
		body.put("type", request.getType());

		if (StringUtils.hasText(request.getPreviousGuestKey())) {
			body.put("previousGuestKey", request.getPreviousGuestKey());
		}

		ResponseEntity<Map> response = restTemplate.exchange(
			apiUrl + HEARTBEAT_PATH,
			HttpMethod.POST,
			jsonEntity(body),
			Map.class);

		return response.getBody();
	}

	public void usage(OnlineUsageRequest request) {
		validateConfig();

		Map<String, Object> body = new HashMap<>();
		body.put("siteId", siteId);
		body.put("feature", request.getFeature());
		body.put("key", request.getKey());
		body.put("type", request.getType());
		body.put("activeSeconds", request.getActiveSeconds());

		restTemplate.exchange(
			apiUrl + USAGE_PATH,
			HttpMethod.POST,
			jsonEntity(body),
			Void.class);
	}

	private HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> body) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(ONLINE_SERVICE_TOKEN_HEADER, apiToken);

		return new HttpEntity<>(body, headers);
	}

	private void validateConfig() {
		if (!StringUtils.hasText(apiUrl) || !StringUtils.hasText(apiToken) || !StringUtils.hasText(siteId)) {
			throw new IllegalStateException("[ONLINE] Variables are not set");
		}
	}

	private static String trimTrailingSlash(String value) {
		if (value != null && value.endsWith("/")) {
			return value.substring(0, value.length() - 1);
		}

		return value;
	}
}
