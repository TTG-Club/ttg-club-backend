package club.dnd5.portal.security;

import club.dnd5.portal.dto.user.ChangePassword;
import club.dnd5.portal.dto.user.LoginDto;
import club.dnd5.portal.dto.user.SignUpDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class ExternalAuthClient {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String FRONTEND_ORIGIN_HEADER = "X-Frontend-Origin";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,2048}$");

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String frontendOrigin;

    public ExternalAuthClient(
            @Value("${auth-service.base-url}") String baseUrl,
            @Value("${app.url:}") String frontendOrigin,
            @Value("${auth-service.connect-timeout-ms:2000}") int connectTimeoutMs,
            @Value("${auth-service.read-timeout-ms:4000}") int readTimeoutMs) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.frontendOrigin = trimTrailingSlash(frontendOrigin);
        this.restTemplate = buildRestTemplate(connectTimeoutMs, readTimeoutMs);
    }

    private static RestTemplate buildRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }

    public JWTAuthResponse login(LoginDto loginDto) {
        Map<String, Object> body = new HashMap<>();
        body.put("login", loginDto.getUsernameOrEmail());
        body.put("password", loginDto.getPassword());

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/auth/login"),
                HttpMethod.POST,
                jsonEntity(body),
                Map.class);

        return toAuthResponse(response.getBody());
    }

    public JWTAuthResponse refresh(String refreshToken) {
        Map<String, Object> body = new HashMap<>();
        body.put("refreshToken", refreshToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/auth/refresh"),
                HttpMethod.POST,
                jsonEntity(body),
                Map.class);

        return toAuthResponse(response.getBody());
    }

    public ExternalAuthUser register(SignUpDto signUpDto) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", signUpDto.getUsername());
        body.put("email", signUpDto.getEmail());
        body.put("password", signUpDto.getPassword());

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/auth/register"),
                HttpMethod.POST,
                jsonEntity(body, true),
                Map.class);

        return toExternalUser(response.getBody(), ExternalAuthTokenClaims.fromToken(null));
    }

    public ExternalAuthUser me(String token) {
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/auth/me"),
                HttpMethod.GET,
                bearerEntity(token),
                Map.class);

        return toExternalUser(response.getBody(), ExternalAuthTokenClaims.fromToken(token));
    }

    public void requestPasswordReset(String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        restTemplate.exchange(
                url("/api/account/password/reset-request"),
                HttpMethod.POST,
                jsonEntity(body, true),
                Void.class);
    }

    public void confirmPasswordReset(String token, String newPassword) {
        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("newPassword", newPassword);
        restTemplate.exchange(
                url("/api/account/password/reset-confirm"),
                HttpMethod.POST,
                jsonEntity(body),
                Void.class);
    }

    public void validatePasswordResetToken(String token) {
        restTemplate.exchange(
                urlWithToken("/api/account/password/reset-token/validate", token),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Void.class);
    }

    public void verifyEmail(String token) {
        restTemplate.exchange(
                urlWithToken("/api/auth/verify-email", token),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Void.class);
    }

    public void changePassword(String accessToken, ChangePassword passwordDto) {
        Map<String, Object> body = new HashMap<>();
        body.put("currentPassword", passwordDto.getCurrentPassword());
        body.put("newPassword", passwordDto.getPassword());
        restTemplate.exchange(
                url("/api/account/change-password"),
                HttpMethod.POST,
                bearerEntity(accessToken, body),
                Void.class);
    }

    public boolean validateAccessToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        me(token);
        return true;
    }

    private ExternalAuthUser toExternalUser(Map body, ExternalAuthTokenClaims claims) {
        ExternalAuthUser user = new ExternalAuthUser();
        user.setId(firstNotBlank(stringValue(body, "id"), claims.getSubject()));
        user.setUsername(firstNotBlank(stringValue(body, "username"), claims.getUsername()));
        user.setEmail(firstNotBlank(stringValue(body, "email"), claims.getEmail()));
        user.setEnabled(booleanValue(body, "enabled", true));
        user.setEmailVerified(booleanValue(body, "emailVerified", false));
        user.setRoles(firstNotEmpty(rolesValue(body, "roles"), claims.getRoles()));
        return user;
    }

    private JWTAuthResponse toAuthResponse(Map body) {
        JWTAuthResponse authResponse = new JWTAuthResponse(stringValue(body, "accessToken"));
        authResponse.setRefreshToken(stringValue(body, "refreshToken"));
        authResponse.setExpiresIn(longValue(body, "expiresIn"));
        authResponse.setRefreshExpiresIn(longValue(body, "refreshExpiresIn"));

        String tokenType = stringValue(body, "tokenType");
        if (StringUtils.hasText(tokenType)) {
            authResponse.setTokenType(tokenType);
        }
        return authResponse;
    }

    private HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> body) {
        return jsonEntity(body, false);
    }

    private HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> body, boolean includeFrontendOrigin) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (includeFrontendOrigin && StringUtils.hasText(frontendOrigin)) {
            headers.set(FRONTEND_ORIGIN_HEADER, frontendOrigin);
        }

        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> bearerEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Map<String, Object>> bearerEntity(String token, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
        return new HttpEntity<>(body, headers);
    }

    private String url(String path) {
        return baseUrl + path;
    }

    private String urlWithToken(String path, String token) {
        validateToken(token);
        return UriComponentsBuilder.fromHttpUrl(url(path))
                .queryParam("token", token)
                .toUriString();
    }

    private static void validateToken(String token) {
        if (!StringUtils.hasText(token) || !TOKEN_PATTERN.matcher(token).matches()) {
            throw new IllegalArgumentException("Invalid token format");
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String stringValue(Map body, String key) {
        if (body == null || body.get(key) == null) {
            return null;
        }
        return String.valueOf(body.get(key));
    }

    private static boolean booleanValue(Map body, String key, boolean defaultValue) {
        if (body == null || body.get(key) == null) {
            return defaultValue;
        }
        Object value = body.get(key);
        return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(String.valueOf(value));
    }

    private static long longValue(Map body, String key) {
        if (body == null || body.get(key) == null) {
            return 0;
        }
        Object value = body.get(key);
        return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(String.valueOf(value));
    }

    private static String firstNotBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private static List<String> firstNotEmpty(List<String> first, List<String> second) {
        return first == null || first.isEmpty() ? second : first;
    }

    private static List<String> rolesValue(Map body, String key) {
        if (body == null || body.get(key) == null) {
            return Collections.emptyList();
        }

        Object value = body.get(key);
        if (value instanceof List) {
            List<String> roles = new ArrayList<>();
            ((List<?>) value).forEach(role -> roles.add(String.valueOf(role)));
            return roles;
        }

        if (value instanceof String && StringUtils.hasText((String) value)) {
            List<String> roles = new ArrayList<>();
            for (String role : ((String) value).split(",")) {
                if (StringUtils.hasText(role)) {
                    roles.add(role.trim());
                }
            }
            return roles;
        }

        return Collections.emptyList();
    }
}
