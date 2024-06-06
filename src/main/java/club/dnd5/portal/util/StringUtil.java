package club.dnd5.portal.util;

public final class StringUtil {
	private StringUtil() {}

	public static String getUrl(String url) {
		return url.toLowerCase().replace(" ", "_");
	}
}
