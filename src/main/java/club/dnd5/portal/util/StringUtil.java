package club.dnd5.portal.util;

public class StringUtil {
	public static String getUrl(String url) {
		return url.toLowerCase().replace(" ", "_");
	}
}
