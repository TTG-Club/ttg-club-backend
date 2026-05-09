package club.dnd5.portal.config;

import club.dnd5.portal.config.properties.RateLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RateLimitFilterTest {
	private static final String HEARTBEAT_URI = "/api/online/heartbeat";

	@Test
	void heartbeatShouldSkipRateLimit() throws Exception {
		RateLimitFilter filter = new RateLimitFilter(propertiesWithSingleToken());

		MockHttpServletResponse firstHeartbeatResponse = execute(filter, "POST", HEARTBEAT_URI);
		MockHttpServletResponse secondHeartbeatResponse = execute(filter, "POST", HEARTBEAT_URI);
		MockHttpServletResponse limitedResponse = execute(filter, "GET", "/api/spells/search");
		MockHttpServletResponse rejectedResponse = execute(filter, "GET", "/api/spells/search");

		assertEquals(200, firstHeartbeatResponse.getStatus());
		assertNull(firstHeartbeatResponse.getHeader("X-RateLimit-Remaining"));
		assertEquals(200, secondHeartbeatResponse.getStatus());
		assertNull(secondHeartbeatResponse.getHeader("X-RateLimit-Remaining"));
		assertEquals(200, limitedResponse.getStatus());
		assertEquals("0", limitedResponse.getHeader("X-RateLimit-Remaining"));
		assertEquals(429, rejectedResponse.getStatus());
	}

	private static MockHttpServletResponse execute(
			RateLimitFilter filter,
			String method,
			String uri
	) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
		request.setRemoteAddr("127.0.0.1");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		return response;
	}

	private static RateLimitProperties propertiesWithSingleToken() {
		RateLimitProperties properties = new RateLimitProperties();
		properties.setCapacity(1);
		properties.setWindow(Duration.ofHours(1));
		return properties;
	}
}
