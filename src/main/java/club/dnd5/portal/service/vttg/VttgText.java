package club.dnd5.portal.service.vttg;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.parser.TagSet;

/**
 * Тексты записей для VTTG.
 *
 * <p>VTTG рендерит описания как markdown с инлайновым HTML, поэтому HTML сайта уезжает
 * как есть. Правятся только две вещи, которые вне сайта не работают: компонент броска
 * {@code <dice-roller>} сворачивается в свою формулу, а относительные ссылки получают
 * адрес сайта — VTTG узнаёт ссылку на запись компендиума по пути, а неузнанную
 * открывает в браузере.</p>
 */
final class VttgText {
	static final String SITE_URL = "https://ttg.club";

	private VttgText() {
	}

	/**
	 * Описание записи в разметке VTTG.
	 *
	 * @param source HTML сайта; пусто — пустая строка
	 */
	static String html(String source) {
		if (isBlank(source)) {
			return "";
		}
		Document document = parse(source);
		for (Element roller : document.select("dice-roller")) {
			String text = roller.text().trim();
			roller.replaceWith(new TextNode(text.isEmpty() ? roller.attr("formula") : text));
		}
		for (Element link : document.select("a[href^=/]")) {
			link.attr("href", SITE_URL + link.attr("href"));
		}
		return document.body().html().trim();
	}

	/**
	 * Разбирает фрагмент HTML сайта.
	 *
	 * <p>По спецификации HTML {@code <dice-roller/>} не самозакрывающийся и забирает в себя
	 * текст до конца абзаца, поэтому тегу явно разрешено закрываться слэшем.</p>
	 */
	private static Document parse(String source) {
		TagSet tags = TagSet.Html();
		tags.valueOf("dice-roller", Parser.NamespaceHtml).set(Tag.SelfClose);
		Document document = Document.createShell("");
		document.outputSettings().prettyPrint(false);
		document.body().appendChildren(Parser.htmlParser().tagSet(tags).parseFragmentInput(source, document.body(), ""));
		return document;
	}

	/**
	 * Короткий текст для вставки внутрь строки или ячейки: разметка сайта сохраняется,
	 * а обёртка из одного абзаца снимается, чтобы не вкладывать абзац в абзац.
	 */
	static String inline(String source) {
		String html = html(source);
		if (html.startsWith("<p>") && html.endsWith("</p>") && html.indexOf("<p>", 1) < 0) {
			return html.substring(3, html.length() - 4);
		}
		return html;
	}

	/** Текст для вставки в HTML. */
	static String escape(String text) {
		return text == null ? "" : Entities.escape(text);
	}

	/** Обязательная строка записи: пусто — пустая строка, а не {@code null}. */
	static String orEmpty(String text) {
		return text == null ? "" : text.trim();
	}

	static boolean isBlank(String text) {
		return text == null || text.trim().isEmpty();
	}

	/** Обрезанный текст; пусто — {@code null}, чтобы поле не уезжало. */
	static String optional(String text) {
		return isBlank(text) ? null : text.trim();
	}
}
