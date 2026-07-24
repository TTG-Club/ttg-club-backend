package club.dnd5.portal.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;

public class AuthServiceAuthenticationFilter extends OncePerRequestFilter {
    private final ExternalAuthClient externalAuthClient;
    private final ExternalAuthUserSynchronizer userSynchronizer;
    private final TokenAuthenticationCache cache;

    public AuthServiceAuthenticationFilter(ExternalAuthClient externalAuthClient,
                                           ExternalAuthUserSynchronizer userSynchronizer,
                                           TokenAuthenticationCache cache) {
        this.externalAuthClient = externalAuthClient;
        this.userSynchronizer = userSynchronizer;
        this.cache = cache;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token) && !isAuthenticated()) {
            ExternalAuthUser user = resolveUser(token);
            if (user != null) {
                authenticate(request, user);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Возвращает пользователя для токена, либо {@code null}, если авторизацию нужно сбросить.
     *
     * <p>Ключевое отличие от прежней логики: транзиентный сбой SSO (таймаут, 5xx, обрыв,
     * 429) больше не приравнивается к невалидному токену. Только явный отказ авторизации
     * (401/403) сбрасывает сессию; при недоступности SSO используется недавно
     * провалидированный (stale) результат, чтобы пользователя не разлогинивало на ровном месте.
     */
    private ExternalAuthUser resolveUser(String token) {
        String key = cacheKey(token);
        TokenAuthenticationCache.Entry cached = cache.get(key);
        if (cached != null && cached.isFresh()) {
            return cached.getUser();
        }

        try {
            ExternalAuthUser user = externalAuthClient.me(token);
            userSynchronizer.sync(user);
            cache.put(key, user);
            return user;
        } catch (HttpStatusCodeException exception) {
            if (isAuthRejection(exception.getStatusCode())) {
                // Токен действительно невалиден/протух — сбрасываем.
                cache.evict(key);
                SecurityContextHolder.clearContext();
                return null;
            }
            // 5xx, 429 и прочее — транзиентно: пробуем пережить на stale-копии.
            return fallbackOrClear(cached);
        } catch (RuntimeException exception) {
            // ResourceAccessException (таймаут/обрыв соединения) и другие сетевые сбои.
            return fallbackOrClear(cached);
        }
    }

    private ExternalAuthUser fallbackOrClear(TokenAuthenticationCache.Entry cached) {
        if (cached != null && cached.isUsableStale(System.currentTimeMillis())) {
            return cached.getUser();
        }
        SecurityContextHolder.clearContext();
        return null;
    }

    private boolean isAuthRejection(HttpStatus status) {
        return status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN;
    }

    private void authenticate(HttpServletRequest request, ExternalAuthUser user) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                principalName(user), null, getAuthorities(user.getRoles()));
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String cacheKey(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(Character.forDigit((b >> 4) & 0xF, 16));
                builder.append(Character.forDigit(b & 0xF, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 гарантированно доступен на любой JVM; fallback на сам токен.
            return token;
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if ("dnd5_user_token".equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    private String principalName(ExternalAuthUser user) {
        return StringUtils.hasText(user.getEmail()) ? user.getEmail() : user.getUsername();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return AuthorityUtils.createAuthorityList("ROLE_USER");
        }

        return AuthorityUtils.createAuthorityList(roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role).toArray(String[]::new));
    }
}
