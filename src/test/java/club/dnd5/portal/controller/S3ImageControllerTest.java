package club.dnd5.portal.controller;

import club.dnd5.portal.config.properties.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class S3ImageControllerTest {
	private S3Client s3Client;
	private S3ImageController controller;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		s3Client = mock(S3Client.class);
		ObjectProvider<S3Client> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(s3Client);
		S3Properties properties = new S3Properties();
		properties.setBucket("ttg");
		properties.setPrefix("5e14");
		controller = new S3ImageController(provider, properties);
	}

	@Test
	void keyIsOnlyUnderSitePrefix() {
		assertEquals("5e14/classes/Bard.webp", S3ImageController.objectKey("/s3/5e14/classes/Bard.webp", "5e14"));
		assertEquals("5e14/creatures/captain n'ghathrod.webp",
			S3ImageController.objectKey("/s3/5e14/creatures/captain n'ghathrod.webp", "5e14"));
		assertNull(S3ImageController.objectKey("/s3/avatars/user/a.webp", "5e14"));
		assertNull(S3ImageController.objectKey("/s3/5e14/../avatars/a.webp", "5e14"));
		assertNull(S3ImageController.objectKey("/s3/5e14//a.webp", "5e14"));
		assertNull(S3ImageController.objectKey("/s3/5e14/a\\b.webp", "5e14"));
		assertNull(S3ImageController.objectKey("/s3/5e14/", "5e14"));
		assertNull(S3ImageController.objectKey("/api/v1/classes", "5e14"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void streamsObjectWithTypeAndCache() throws Exception {
		byte[] body = {1, 2, 3};
		GetObjectResponse meta = GetObjectResponse.builder()
			.contentType("image/webp").contentLength(3L).eTag("\"abc\"").build();
		when(s3Client.getObject(any(Consumer.class))).thenAnswer(invocation -> {
			GetObjectRequest.Builder builder = GetObjectRequest.builder();
			((Consumer<GetObjectRequest.Builder>) invocation.getArgument(0)).accept(builder);
			GetObjectRequest request = builder.build();
			assertEquals("ttg", request.bucket());
			assertEquals("5e14/classes/Bard.webp", request.key());
			return new ResponseInputStream<>(meta, AbortableInputStream.create(new ByteArrayInputStream(body)));
		});

		MockHttpServletResponse response = get("/s3/5e14/classes/Bard.webp");

		assertEquals(200, response.getStatus());
		assertEquals("image/webp", response.getContentType());
		assertEquals(S3ImageController.DEFAULT_CACHE, response.getHeader(HttpHeaders.CACHE_CONTROL));
		assertEquals("\"abc\"", response.getHeader(HttpHeaders.ETAG));
		assertArrayEquals(body, response.getContentAsByteArray());
	}

	@Test
	@SuppressWarnings("unchecked")
	void missingObjectIs404AndForeignKeyIsNotRequested() throws Exception {
		when(s3Client.getObject(any(Consumer.class)))
			.thenThrow(S3Exception.builder().statusCode(403).message("AccessDenied").build());

		assertEquals(404, get("/s3/5e14/classes/none.webp").getStatus());
		assertEquals(404, get("/s3/avatars/u/a.webp").getStatus());
	}

	private MockHttpServletResponse get(String uri) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
		MockHttpServletResponse response = new MockHttpServletResponse();
		controller.image(request, response);
		return response;
	}
}
