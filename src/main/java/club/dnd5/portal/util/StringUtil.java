package club.dnd5.portal.util;

public final class StringUtil {
	private StringUtil() {}

	public static String getUrl(String url) {
		return url.toLowerCase().replace(" ", "_");
	}

	public static String capitalize(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		return Character.toTitleCase(value.charAt(0)) + value.substring(1);
	}

	public static String capitalizeWords(String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		StringBuilder result = new StringBuilder(value.length());
		boolean capitalizeNext = true;
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (Character.isWhitespace(current)) {
				capitalizeNext = true;
				result.append(current);
			} else if (capitalizeNext) {
				result.append(Character.toTitleCase(current));
				capitalizeNext = false;
			} else {
				result.append(current);
			}
		}
		return result.toString();
	}
}
