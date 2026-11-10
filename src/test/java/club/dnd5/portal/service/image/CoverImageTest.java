package club.dnd5.portal.service.image;

import club.dnd5.portal.model.image.CoverImage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoverImageTest {
	@Test
	void pathsMatchCardImages() {
		assertEquals("classes/background/class-blood-hunter.webp", CoverImage.classPath("Blood Hunter"));
		assertEquals("races/background/race-half-elf.webp", CoverImage.racePath("Half-Elf"));
	}
}
