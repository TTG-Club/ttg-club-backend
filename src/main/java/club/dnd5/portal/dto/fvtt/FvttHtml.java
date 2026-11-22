package club.dnd5.portal.dto.fvtt;

import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Подготовка HTML описаний (см. {@code MarkupHtmlRenderer}) для экспорта в Foundry VTT и Plutonium:
 * броски {@code <dice-roller>} → синтаксис VTT, внутренние ссылки → абсолютные на ttg.club.
 */
public final class FvttHtml {
	private static final String SITE = "https://ttg.club";
	/** {@code <dice-roller … formula="…" …>текст</dice-roller>} и старый самозакрывающийся вариант. */
	private static final Pattern DICE_ROLLER = Pattern.compile(
		"<dice-roller\\b[^>]*?\\bformula=\"([^\"]*)\"[^>]*?(?:/>|>.*?</dice-roller>)", Pattern.DOTALL);
	/** Внутренняя ссылка: {@code href="/path"}, но не {@code href="//host"}. */
	private static final Pattern INTERNAL_HREF = Pattern.compile("href=\"/(?!/)");

	private FvttHtml() {
	}

	/**
	 * Заменяет каждый {@code <dice-roller>} результатом {@code format} от его формулы (кириллическая
	 * «к» → «d»).
	 */
	public static String rolls(String html, UnaryOperator<String> format) {
		Matcher matcher = DICE_ROLLER.matcher(html);
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			String formula = matcher.group(1).strip().replace('к', 'd').replace('К', 'd');
			matcher.appendReplacement(out, Matcher.quoteReplacement(format.apply(formula)));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	/** Относительные ссылки сайта → абсолютные: в VTT они открываются вне ttg.club. */
	public static String absoluteLinks(String html) {
		return INTERNAL_HREF.matcher(html).replaceAll("href=\"" + SITE + "/");
	}
}
