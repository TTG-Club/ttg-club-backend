package club.dnd5.portal.controller.api.image;

import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.service.image.ImageGalleryService;
import club.dnd5.portal.service.image.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/** Картинки мастерской меняют только модераторы и админы. */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ImageWorkshopApiControllerSecurityTest.TestConfig.class)
class ImageWorkshopApiControllerSecurityTest {
	@Autowired
	private ImageWorkshopApiController controller;

	@Test
	@WithAnonymousUser
	void anonymousIsDenied() {
		for (Executable call : calls()) {
			assertThrows(AccessDeniedException.class, call);
		}
	}

	@Test
	@WithMockUser(roles = "USER")
	void regularUserIsDenied() {
		for (Executable call : calls()) {
			assertThrows(AccessDeniedException.class, call);
		}
	}

	@Test
	@WithMockUser(roles = "MODERATOR")
	void moderatorIsAllowed() {
		for (Executable call : calls()) {
			assertDoesNotThrow(call);
		}
	}

	private Executable[] calls() {
		return new Executable[] {
			() -> controller.upload(ImageType.CREATURE, null),
			() -> controller.delete("/s3/5e14/creatures/a.webp"),
			() -> controller.getGallery(ImageType.CREATURE, 1),
			() -> controller.replaceGallery(ImageType.CREATURE, 1, new club.dnd5.portal.dto.api.image.ImageGalleryApi(List.of())),
			() -> controller.addToGallery(ImageType.CREATURE, 1, null),
			() -> controller.getCover(ImageType.CLASS, 1),
			() -> controller.uploadCover(ImageType.CLASS, 1, null)
		};
	}

	@Configuration
	@EnableMethodSecurity
	static class TestConfig {
		@Bean
		ImageWorkshopApiController controller() {
			return new ImageWorkshopApiController(mock(ImageStorageService.class), mock(ImageGalleryService.class));
		}
	}
}
