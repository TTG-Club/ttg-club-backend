package club.dnd5.portal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Клиент межсервисной ручки comments-service, массово приводящей имя автора в его
 * комментариях к отображаемому имени. Дёргается после смены имени в профиле и после
 * публикации комментария (сервис стамперит логин из токена — этот вызов заменяет его).
 * <p>
 * Переименование скоупится платформой этого сайта ({@link #SOURCE_PLATFORM}): {@code authorId}
 * (клейм {@code sub}) общий у пользователя на всех сайтах через единый auth-service, а имя у
 * каждого сайта своё — без скоупа вызов переписал бы и комментарии автора на новом сайте.
 * <p>
 * Всё best-effort: без межсервисного токена ({@code comments-service.internal-token}) или при
 * ошибке запроса переименование тихо пропускается — синхронизация имени не должна ронять
 * пользовательский поток.
 */
@Component
public class CommentsRenameClient {
    /** Платформа-источник этого сайта в сервисе комментариев (редакция 2014). */
    private static final String SOURCE_PLATFORM = "SITE_5E14";

    /** Заголовок межсервисной авторизации internal API comments-service. */
    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    /** Internal-эндпоинт массового переименования автора. */
    private static final String RENAME_PATH = "/api/v1/internal/comments/rename-by-author";

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private final RestTemplate restTemplate = buildRestTemplate();
    private final String baseUrl;
    private final String serviceToken;

    public CommentsRenameClient(
            @Value("${comments-service.base-url}") String baseUrl,
            @Value("${comments-service.internal-token:}") String serviceToken) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.serviceToken = serviceToken;
    }

    /**
     * Приводит имя автора в его комментариях этого сайта к переданному имени. Возвращает
     * {@code true}, если запрос ушёл успешно; {@code false} — если межсервисный токен не задан
     * или сервис ответил ошибкой (тогда имя обновится при следующей синхронизации).
     *
     * @param authorId    UUID автора (клейм {@code sub} токена auth-service)
     * @param displayName отображаемое имя
     */
    public boolean rename(String authorId, String displayName) {
        if (!StringUtils.hasText(serviceToken) || !StringUtils.hasText(authorId)
                || !StringUtils.hasText(displayName)) {
            return false;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("authorId", authorId);
        body.put("sourcePlatform", SOURCE_PLATFORM);
        body.put("displayName", displayName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(SERVICE_TOKEN_HEADER, serviceToken);

        try {
            restTemplate.exchange(
                    baseUrl + RENAME_PATH,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Void.class);

            return true;
        } catch (RuntimeException error) {
            // Best-effort: имя подхватится при следующем вызове (после публикации/смены имени).
            return false;
        }
    }

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);

        return new RestTemplate(factory);
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
