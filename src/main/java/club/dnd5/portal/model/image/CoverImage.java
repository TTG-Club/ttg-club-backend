package club.dnd5.portal.model.image;

import java.util.Locale;

/**
 * Обложки классов и рас: файл не хранится в базе, а ищется по английскому
 * названию — {@code classes/background/class-<name>.webp}. Обложка источника
 * лежит по аббревиатуре, а её адрес хранится в книге: у большинства книг
 * обложки нет.
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

	/**
	 * Путь обложки источника от корня картинок сайта. Аббревиатура — ключ книги
	 * и не меняется, поэтому имя файла строится по ней.
	 * @param source аббревиатура источника
	 */
	public static String bookPath(String source) {
		return "books/covers/" + source.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-") + ".webp";
	}

	private static String slug(String englishName) {
		return englishName.replace(' ', '-').toLowerCase(Locale.ROOT);
	}
}
