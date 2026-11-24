package club.dnd5.portal.util;

/**
 * Строки через {@code \r} → абзацы {@code <p>} — так старое API отдавало описания богов. Используется
 * только миграциями Liquibase (sourceFormat lines в 0029), но не удалять: Liquibase выполняет
 * customChange-классы при каждой пересборке базы и при откате.
 */
public final class HtmlConverter {
	public static String toHtml(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (String line : text.split("\\r")) {
			builder.append("<p>");
			builder.append(line);
			builder.append("</p>");
		}
		return builder.toString();
	}
}