package club.dnd5.portal.service.image;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImageLinksTest {
	@AfterEach
	void reset() {
		ImageLinks.configure(null, null);
	}

	@Test
	void buildsSiteAndAbsoluteLinks() {
		ImageLinks.configure("/s3/5e14/", "https://5e14.ttg.club/");

		assertEquals("/s3/5e14/classes/Bard.webp", ImageLinks.url("classes/Bard.webp"));
		assertEquals("/s3/5e14/classes/Bard.webp", ImageLinks.url("/classes/Bard.webp"));
		assertEquals("https://5e14.ttg.club/s3/5e14/magic/Evocation.png", ImageLinks.absoluteUrl("magic/Evocation.png"));
		assertEquals("https://5e14.ttg.club/s3/5e14/tokens/round/a.webp", ImageLinks.absolute("/s3/5e14/tokens/round/a.webp"));
		assertEquals("https://5e.tools/img/a.png", ImageLinks.absolute("https://5e.tools/img/a.png"));
		assertNull(ImageLinks.absolute(null));
	}

	@Test
	void defaultsWithoutSettings() {
		ImageLinks.configure("", "");

		assertEquals("/s3/5e14/races/background/race-elf.webp", ImageLinks.url("races/background/race-elf.webp"));
		assertEquals("https://5e14.ttg.club/s3/5e14/x.webp", ImageLinks.absoluteUrl("x.webp"));
	}
}
