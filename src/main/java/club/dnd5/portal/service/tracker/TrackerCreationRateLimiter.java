package club.dnd5.portal.service.tracker;

import club.dnd5.portal.config.properties.TrackerProperties;
import club.dnd5.portal.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ограничение создания анонимных трекеров инициативы по IP (fixed-window, в памяти, без bucket4j).
 * <p>
 * Лимит «один трекер анониму» держит клиент (localStorage); сервер этой защитой лишь пресекает
 * массовое создание мусорных трекеров с одного адреса.
 */
@RequiredArgsConstructor
@Component
public class TrackerCreationRateLimiter {

	private final TrackerProperties properties;

	private final Map<String, Window> windows = new ConcurrentHashMap<>();

	public void checkAnonymousCreation(String clientIp) {
		Window window = windows.computeIfAbsent(clientIp, ip -> new Window());
		if (!window.tryConsume(properties.getAnonymousCreateLimit(), properties.getAnonymousCreateWindow())) {
			throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
					"Слишком много анонимных трекеров. Попробуйте позже или войдите в аккаунт");
		}
	}

	/**
	 * IP клиента с учётом X-Forwarded-For — тот же способ, что в {@code RateLimitFilter}.
	 */
	public static String resolveClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (StringUtils.hasText(forwarded)) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	/** Очистка простаивающих записей, чтобы карта не росла бесконечно. */
	@Scheduled(fixedDelayString = "PT30M")
	public void cleanup() {
		Instant cutoff = Instant.now().minus(properties.getBucketIdleTtl());
		windows.entrySet().removeIf(entry -> entry.getValue().lastSeenBefore(cutoff));
	}

	/** Счётчик с фиксированным окном для одного IP. */
	private static final class Window {
		private Instant windowStart = Instant.now();
		private int count;
		private Instant lastSeen = Instant.now();

		synchronized boolean tryConsume(int limit, Duration windowSize) {
			Instant now = Instant.now();
			lastSeen = now;
			if (Duration.between(windowStart, now).compareTo(windowSize) >= 0) {
				windowStart = now;
				count = 0;
			}
			if (count >= limit) {
				return false;
			}
			count++;
			return true;
		}

		synchronized boolean lastSeenBefore(Instant cutoff) {
			return lastSeen.isBefore(cutoff);
		}
	}
}
