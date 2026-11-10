package club.dnd5.portal.service.image;

import club.dnd5.portal.config.properties.ImageProperties;
import club.dnd5.portal.config.properties.S3Properties;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.image.ImageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageStorageServiceTest {
	private S3Client s3Client;
	private ObjectProvider<S3Client> provider;
	private ImageProperties imageProperties;
	private ImageStorageService service;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		s3Client = mock(S3Client.class);
		provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(s3Client);
		S3Properties s3Properties = new S3Properties();
		s3Properties.setBucket("ttg");
		s3Properties.setPrefix("5e14");
		imageProperties = new ImageProperties();
		service = new ImageStorageService(provider, s3Properties, imageProperties);
		ReflectionTestUtils.setField(service, "imageUrl", "https://img.ttg.club/");
		service.init();
	}

	@Test
	void fileSlugKeepsLatinDigitsAndSingleDashes() {
		assertEquals("my-cool-dragon-2", ImageStorageService.fileSlug("  My  Cool__Dragon (2).PNG"));
		assertEquals("image", ImageStorageService.fileSlug("Дракон.png"));
		assertEquals("image", ImageStorageService.fileSlug(null));
		assertEquals("a-b", ImageStorageService.fileSlug("---a---b---.webp"));
	}

	@Test
	void fileSlugCutsToLimitWithoutTrailingDash() {
		assertEquals("a".repeat(60), ImageStorageService.fileSlug("a".repeat(100) + ".png"));
		assertEquals("a".repeat(59), ImageStorageService.fileSlug("a".repeat(59) + "-b.png"));
		assertEquals("a".repeat(58) + "-b", ImageStorageService.fileSlug("a".repeat(58) + "-bc.png"));
	}

	@Test
	void fileSlugIsFastOnLongDashRuns() {
		String name = "a" + "-".repeat(200_000) + "!";
		long start = System.nanoTime();
		assertEquals("a", ImageStorageService.fileSlug(name));
		assertTrue(System.nanoTime() - start < 1_000_000_000L);
	}

	@Test
	@SuppressWarnings("unchecked")
	void uploadsWebpIntoSectionFolder() throws IOException {
		MockMultipartFile file = new MockMultipartFile("file", "Красный Дракон 01.png", "image/png", png(40, 20));

		String url = service.upload(ImageType.CREATURE, file);

		assertTrue(url.matches("https://img\\.ttg\\.club/creatures/\\d+-01\\.webp"), url);
		ArgumentCaptor<Consumer<PutObjectRequest.Builder>> request = ArgumentCaptor.forClass(Consumer.class);
		verify(s3Client).putObject(request.capture(), any(RequestBody.class));
		PutObjectRequest.Builder builder = PutObjectRequest.builder();
		request.getValue().accept(builder);
		PutObjectRequest built = builder.build();
		assertEquals("ttg", built.bucket());
		assertEquals("5e14/" + url.substring("https://img.ttg.club/".length()), built.key());
		assertEquals("image/webp", built.contentType());
	}

	@Test
	void rejectsNotAnImage() {
		MockMultipartFile file = new MockMultipartFile("file", "notes.png", "image/png", "hello".getBytes());

		ApiException exception = assertThrows(ApiException.class, () -> service.upload(ImageType.GOD, file));
		assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getStatus());
	}

	@Test
	void rejectsTooLargeFile() throws IOException {
		imageProperties.setMaxBytes(10);
		MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", png(10, 10));

		ApiException exception = assertThrows(ApiException.class, () -> service.upload(ImageType.GOD, file));
		assertEquals(HttpStatus.CONTENT_TOO_LARGE, exception.getStatus());
	}

	@Test
	void shrinksLargeImageInsteadOfRejecting() throws IOException {
		imageProperties.setMaxWidth(32);
		imageProperties.setMaxHeight(32);
		MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", png(128, 64));

		BufferedImage result = ImageIO.read(new java.io.ByteArrayInputStream(service.convert(file)));

		assertEquals(32, result.getWidth());
		assertEquals(16, result.getHeight());
	}

	@Test
	void reportsMissingStorage() throws IOException {
		when(provider.getIfAvailable()).thenReturn(null);
		MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", png(10, 10));

		ApiException exception = assertThrows(ApiException.class, () -> service.upload(ImageType.GOD, file));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
	}

	@Test
	void pathOfAcceptsOnlyOwnImages() {
		assertEquals("gods/zeus.webp", service.pathOf("https://img.ttg.club/gods/zeus.webp?v=1"));
		assertNull(service.pathOf("https://i.imgur.com/zeus.webp"));
		assertNull(service.pathOf("https://img.ttg.club/../secret"));
		assertThrows(ApiException.class, () -> service.delete("https://i.imgur.com/zeus.webp"));
	}

	@Test
	void fileSlugKeepsLatinOnly() {
		assertEquals("ancient-red-dragon", ImageStorageService.fileSlug("Ancient Red_Dragon.JPG"));
		assertEquals("image", ImageStorageService.fileSlug("Дракон.png"));
		assertEquals("image", ImageStorageService.fileSlug(null));
	}

	private static byte[] png(int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", out);
			return out.toByteArray();
		}
	}
}
