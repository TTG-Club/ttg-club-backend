package club.dnd5.portal.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExternalAuthClientTest {
    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void takesFirstAddressFromForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.2");
        request.setRemoteAddr("10.0.0.3");

        assertEquals("203.0.113.7", ExternalAuthClient.resolveClientIp(request));
    }

    @Test
    void fallsBackToRemoteAddressWithoutForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.4");

        assertEquals("198.51.100.4", ExternalAuthClient.resolveClientIp(request));
    }

    @Test
    void readsClientAddressOfCurrentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.7");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("203.0.113.7", ExternalAuthClient.currentClientIp());
    }

    @Test
    void returnsNullOutsideRequest() {
        assertNull(ExternalAuthClient.currentClientIp());
    }
}
