package club.dnd5.portal.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class AuthServiceAuthenticationFilterTest {
    private static final String TOKEN = "header.payload.signature";

    private ExternalAuthClient authClient;
    private ExternalAuthUserSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        authClient = mock(ExternalAuthClient.class);
        synchronizer = mock(ExternalAuthUserSynchronizer.class);
        doNothing().when(synchronizer).sync(any());
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesOnSuccessfulValidation() throws Exception {
        when(authClient.me(TOKEN)).thenReturn(user());

        assertEquals("user@ttg.club", runFilter(filter(60_000, 600_000)).getName());
    }

    @Test
    void clearsAuthenticationOnUnauthorized() throws Exception {
        when(authClient.me(TOKEN)).thenThrow(HttpClientErrorException.create(
                UNAUTHORIZED, "Unauthorized", null, null, null));

        assertNull(runFilter(filter(60_000, 600_000)));
    }

    @Test
    void keepsUserOnTransientServerErrorWhenPreviouslyValidated() throws Exception {
        // freshTtl=0 => запись сразу перестаёт быть свежей, но остаётся пригодной как stale.
        AuthServiceAuthenticationFilter filter = filter(0, 600_000);
        when(authClient.me(TOKEN)).thenReturn(user());
        runFilter(filter); // первый запрос кладёт валидацию в кеш

        when(authClient.me(TOKEN)).thenThrow(HttpServerErrorException.create(
                INTERNAL_SERVER_ERROR, "Boom", null, null, null));

        // SSO отдаёт 5xx — но благодаря stale-кешу пользователь не разлогинивается.
        assertEquals("user@ttg.club", runFilter(filter).getName());
    }

    @Test
    void keepsUserOnNetworkTimeoutWhenPreviouslyValidated() throws Exception {
        AuthServiceAuthenticationFilter filter = filter(0, 600_000);
        when(authClient.me(TOKEN)).thenReturn(user());
        runFilter(filter);

        when(authClient.me(TOKEN)).thenThrow(new ResourceAccessException("timeout"));

        assertEquals("user@ttg.club", runFilter(filter).getName());
    }

    @Test
    void clearsAuthenticationOnTransientErrorWithoutPriorValidation() throws Exception {
        when(authClient.me(TOKEN)).thenThrow(new ResourceAccessException("timeout"));

        assertNull(runFilter(filter(60_000, 600_000)));
    }

    @Test
    void expiredStaleCacheNoLongerRescuesUser() throws Exception {
        // staleTtl=0 => запись мгновенно протухает целиком, fallback невозможен.
        AuthServiceAuthenticationFilter filter = filter(0, 0);
        when(authClient.me(TOKEN)).thenReturn(user());
        runFilter(filter);

        when(authClient.me(TOKEN)).thenThrow(new ResourceAccessException("timeout"));

        assertNull(runFilter(filter));
    }

    @Test
    void doesNotRevalidateFreshToken() throws Exception {
        AuthServiceAuthenticationFilter filter = filter(60_000, 600_000);
        when(authClient.me(TOKEN)).thenReturn(user());

        runFilter(filter);
        runFilter(filter);

        // Второй запрос попал в свежий кеш — повторного обращения к SSO нет.
        verify(authClient, times(1)).me(anyString());
    }

    private AuthServiceAuthenticationFilter filter(long freshTtlMs, long staleTtlMs) {
        return new AuthServiceAuthenticationFilter(
                authClient, synchronizer, new TokenAuthenticationCache(freshTtlMs, staleTtlMs, 1000));
    }

    private Authentication runFilter(AuthServiceAuthenticationFilter filter) throws Exception {
        SecurityContextHolder.clearContext();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/user/profile");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private ExternalAuthUser user() {
        ExternalAuthUser user = new ExternalAuthUser();
        user.setEmail("user@ttg.club");
        user.setUsername("user");
        user.setEnabled(true);
        user.setRoles(Collections.singletonList("USER"));
        return user;
    }
}
