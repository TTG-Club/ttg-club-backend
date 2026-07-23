package club.dnd5.portal.controller.proxy;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/**
 * Прокси публичного API сервиса комментариев (comments.ttg.club).
 * <p>
 * Старый сайт — статический SPA, который отдаёт этот же Spring-монолит, поэтому
 * запрос фронта на относительный {@code /api/v1/comments/**} приходит сюда, а не
 * в отдельный сервер приложения (в отличие от нового сайта, где ту же роль
 * играет Nitro-сервер Nuxt). Без этого форварда путь упирается в отсутствующий
 * контроллер и отдаёт 404.
 * <p>
 * Сервис комментариев проверяет тот же SSO-JWT auth-service, но ждёт его в
 * заголовке {@code Authorization: Bearer}, а фронт в проде шлёт токен кукой
 * {@code dnd5_user_token} (заголовок не выставляет). Поэтому токен извлекается
 * так же, как в {@code AuthServiceAuthenticationFilter}, и подставляется
 * заголовком в исходящий запрос. Гостю токена нет — чтение открыто, форвард
 * уходит без {@code Authorization}.
 * <p>
 * Ответы сервиса, включая 4xx/5xx, отдаются фронту как есть: он разбирает тело
 * ProblemDetail (поле {@code detail}) и заголовок {@code Retry-After} у 429.
 * Поэтому обработчик ошибок RestTemplate отключён — иначе он бросил бы
 * исключение и подменил бы значимый ответ сервиса на 500.
 */
@Hidden
@RestController
@RequestMapping("/api/v1/comments")
public class CommentsProxyController {
    private static final String USER_TOKEN_COOKIE = "dnd5_user_token";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 15_000;

    private final RestTemplate restTemplate = buildRestTemplate();
    private final String baseUrl;
    private final URI baseUri;

    public CommentsProxyController(@Value("${comments-service.base-url}") String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.baseUri = URI.create(this.baseUrl);
    }

    @RequestMapping({"", "/**"})
    public ResponseEntity<byte[]> forward(HttpServletRequest request,
                                          @RequestBody(required = false) byte[] body) {
        HttpHeaders headers = new HttpHeaders();

        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        if (StringUtils.hasText(contentType)) {
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        }

        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (StringUtils.hasText(accept)) {
            headers.set(HttpHeaders.ACCEPT, accept);
        }

        String token = extractToken(request);
        if (token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
        }

        HttpMethod method = HttpMethod.resolve(request.getMethod());
        if (method == null) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }

        try {
            ResponseEntity<byte[]> upstream = restTemplate.exchange(
                    targetUri(request),
                    method,
                    new HttpEntity<>(body, headers),
                    byte[].class);

            return relay(upstream);
        } catch (IllegalArgumentException invalidTarget) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ResourceAccessException connectionError) {
            // Сервис недоступен (таймаут, отказ соединения) — отдаём 502, чтобы
            // фронт показал ошибку загрузки, а не пустой 500 монолита.
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /**
     * Копирует апстрим-ответ фронту, перенося только те заголовки, от которых
     * он зависит: тип содержимого (разбор JSON/ProblemDetail) и Retry-After
     * (пауза антиспам-лимита у 429). Остальное проставит контейнер.
     */
    private ResponseEntity<byte[]> relay(ResponseEntity<byte[]> upstream) {
        HttpHeaders headers = new HttpHeaders();

        MediaType contentType = upstream.getHeaders().getContentType();
        if (contentType != null) {
            headers.setContentType(contentType);
        }

        String retryAfter = upstream.getHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (StringUtils.hasText(retryAfter)) {
            headers.set(HttpHeaders.RETRY_AFTER, retryAfter);
        }

        return new ResponseEntity<>(upstream.getBody(), headers, upstream.getStatusCode());
    }

    /**
     * Полный адрес запроса к сервису: только в пределах настроенного baseUrl.
     * Запрещаем любые попытки выйти за origin/prefix базового URL.
     */
    private URI targetUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String query = request.getQueryString();

        String pathWithinProxy = requestUri;
        String mappingPrefix = "/api/v1/comments";
        if (pathWithinProxy.startsWith(mappingPrefix)) {
            pathWithinProxy = pathWithinProxy.substring(mappingPrefix.length());
        }
        if (!pathWithinProxy.startsWith("/")) {
            pathWithinProxy = "/" + pathWithinProxy;
        }

        URI resolved = UriComponentsBuilder.fromUri(baseUri)
                .replacePath(baseUri.getPath() + pathWithinProxy)
                .replaceQuery(query)
                .build(true)
                .toUri()
                .normalize();

        if (!sameOrigin(baseUri, resolved)) {
            throw new IllegalArgumentException("Resolved URI is outside configured origin");
        }

        String basePath = baseUri.getPath() == null ? "" : baseUri.getPath();
        String resolvedPath = resolved.getPath() == null ? "" : resolved.getPath();
        if (!resolvedPath.startsWith(basePath + "/") && !resolvedPath.equals(basePath)) {
            throw new IllegalArgumentException("Resolved URI is outside configured path prefix");
        }

        return resolved;
    }

    private boolean sameOrigin(URI expectedBase, URI candidate) {
        String expectedScheme = expectedBase.getScheme() == null ? "" : expectedBase.getScheme().toLowerCase(Locale.ROOT);
        String candidateScheme = candidate.getScheme() == null ? "" : candidate.getScheme().toLowerCase(Locale.ROOT);

        String expectedHost = expectedBase.getHost() == null ? "" : expectedBase.getHost().toLowerCase(Locale.ROOT);
        String candidateHost = candidate.getHost() == null ? "" : candidate.getHost().toLowerCase(Locale.ROOT);

        int expectedPort = expectedBase.getPort();
        int candidatePort = candidate.getPort();

        return expectedScheme.equals(candidateScheme)
                && expectedHost.equals(candidateHost)
                && expectedPort == candidatePort;
    }

    /**
     * Токен пользователя — из заголовка Authorization, иначе из куки. Копия
     * логики {@code AuthServiceAuthenticationFilter}: фронт в проде токен в
     * заголовке не шлёт, только кукой.
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (USER_TOKEN_COOKIE.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private static RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);

        RestTemplate template = new RestTemplate(factory);

        // Не бросать на 4xx/5xx: значимый ответ сервиса (ProblemDetail, 429 с
        // Retry-After, 409 «уже удалён») должен дойти до фронта, а не
        // превратиться в исключение и 500.
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });

        return template;
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
