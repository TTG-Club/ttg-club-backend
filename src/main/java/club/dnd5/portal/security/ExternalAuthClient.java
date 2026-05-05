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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ExternalAuthClient {
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public ExternalAuthClient(@Value("${auth-service.base-url}") String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
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

        Map responseBody = response.getBody();
        String accessToken = stringValue(responseBody, "accessToken");
        JWTAuthResponse authResponse = new JWTAuthResponse(accessToken);
        String tokenType = stringValue(responseBody, "tokenType");
        if (StringUtils.hasText(tokenType)) {
            authResponse.setTokenType(tokenType);
        }
        return authResponse;
    }

    public ExternalAuthUser register(SignUpDto signUpDto) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", signUpDto.getUsername());
        body.put("email", signUpDto.getEmail());
        body.put("password", signUpDto.getPassword());

        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/auth/register"),
                HttpMethod.POST,
                jsonEntity(body),
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
                jsonEntity(body),
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

    private HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
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
