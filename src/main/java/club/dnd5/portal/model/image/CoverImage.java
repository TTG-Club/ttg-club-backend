package club.dnd5.portal.model.image;

import java.util.Locale;

/**
 * Обложки классов и рас: файл не хранится в базе, а ищется по английскому
 * названию — {@code classes/background/class-<name>.webp}.
 */
public final class CoverImage {
	private CoverImage() {
	}

	/**
	 * Путь обложки класса от корня картинок сайта (см. {@code ImageLinks}).
	 * @param englishName английское название класса
	 */
	public static String classPath(String englishName) {
		return "classes/background/class-" + slug(englishName) + ".webp";
	}

	/**
	 * Путь обложки расы от корня картинок сайта. У подрас своей обложки нет —
	 * берётся обложка основной расы.
	 * @param englishName английское название основной расы
	 */
	public static String racePath(String englishName) {
		return "races/background/race-" + slug(englishName) + ".webp";
	}

	private static String slug(String englishName) {
		return englishName.replace(' ', '-').toLowerCase(Locale.ROOT);
	}
}
