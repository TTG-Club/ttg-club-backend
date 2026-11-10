package club.dnd5.portal.service.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Адреса картинок сайта.
 * <p>
 * Картинки лежат в S3 под {@code <app.s3.prefix>/<путь>} и раздаются самим
 * сайтом по {@code /s3/<ключ>} ({@code S3ImageController}), как на новом сайте:
 * {@code classes/Bard.webp} → {@code /s3/5e14/classes/Bard.webp}.
 * <p>
 * Методы статические, потому что адреса строятся и в DTO ({@code ClassApi},
 * {@code RaceApi}), которые создаются через {@code new}. Значения берутся из
 * настроек при старте; до старта (в юнит-тестах) — значения по умолчанию.
 */
@Component
public class ImageLinks {
	static final String DEFAULT_BASE = "/s3/5e14";
	static final String DEFAULT_SITE = "https://5e14.ttg.club";

	private static volatile String base = DEFAULT_BASE;
	private static volatile String site = DEFAULT_SITE;

	public ImageLinks(@Value("${app.img.url}") String imageUrl, @Value("${app.url:}") String appUrl) {
		configure(imageUrl, appUrl);
	}

	static void configure(String imageUrl, String appUrl) {
		base = trimSlash(imageUrl == null || imageUrl.isBlank() ? DEFAULT_BASE : imageUrl);
		site = trimSlash(appUrl == null || appUrl.isBlank() ? DEFAULT_SITE : appUrl);
	}

	/**
	 * Адрес картинки для сайта: {@code /s3/5e14/<путь>}.
	 * @param path путь от корня картинок старого сайта, например {@code classes/Bard.webp}
	 */
	public static String url(String path) {
		return base + "/" + (path.startsWith("/") ? path.substring(1) : path);
	}

	/**
	 * Полный адрес с доменом сайта — для мест, где относительный адрес не
	 * сработает: экспорт в Foundry и VTTG, og:image.
	 * @param url адрес картинки; уже полный возвращается как есть
	 */
	public static String absolute(String url) {
		if (url == null || !url.startsWith("/") || url.startsWith("//")) {
			return url;
		}
		return site + url;
	}

	/** Полный адрес картинки по пути: {@code https://5e14.ttg.club/s3/5e14/<путь>}. */
	public static String absoluteUrl(String path) {
		return absolute(url(path));
	}

	private static String trimSlash(String value) {
		String result = value.trim();
		while (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}
}
