package club.dnd5.portal.service.image;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImageGalleryServiceTest {
	@Test
	void acceptsAbsoluteAndSitePaths() {
		assertThat(ImageGalleryService.isAllowedUrl("https://example.com/a.webp")).isTrue();
		assertThat(ImageGalleryService.isAllowedUrl("http://example.com/a.webp")).isTrue();
		assertThat(ImageGalleryService.isAllowedUrl("/s3/5e14/classes/Bard.webp")).isTrue();
	}

	@Test
	void rejectsOtherSchemesAndHostRelative() {
		assertThat(ImageGalleryService.isAllowedUrl("javascript:alert(1)")).isFalse();
		assertThat(ImageGalleryService.isAllowedUrl("data:image/png;base64,x")).isFalse();
		assertThat(ImageGalleryService.isAllowedUrl("//evil.example/a.webp")).isFalse();
		assertThat(ImageGalleryService.isAllowedUrl("/\\evil.example/a.webp")).isFalse();
		assertThat(ImageGalleryService.isAllowedUrl("/")).isFalse();
		assertThat(ImageGalleryService.isAllowedUrl("img.webp")).isFalse();
	}
}
