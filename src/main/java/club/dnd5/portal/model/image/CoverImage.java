package club.dnd5.portal.model.image;

import java.util.Locale;

/**
 * Обложки классов и рас: файл не хранится в базе, а ищется по английскому
 * названию — {@code classes/background/class-<name>.webp}.
 */
public final class CoverImage {
	/** Адрес, с которого раздаются обложки (как в карточках классов и рас). */
	public static final String HOST = "https://img.ttg.club/";

	private CoverImage() {
	}

	/**
	 * Путь обложки класса от корня img.ttg.club.
	 * @param englishName английское название класса
	 */
	public static String classPath(String englishName) {
		return "classes/background/class-" + slug(englishName) + ".webp";
	}

	/**
	 * Путь обложки расы от корня img.ttg.club. У подрас своей обложки нет —
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
