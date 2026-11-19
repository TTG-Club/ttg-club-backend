package club.dnd5.portal.util.markup;

import java.util.List;

/**
 * Описания в формате core-api: хранятся массивом строк-блоков разметки (jsonb), старому фронтенду
 * отдаются HTML-ом через мост {@link MarkupHtmlRenderer}, из редактора мастерской приходят HTML-ом
 * и переводятся {@link HtmlToMarkupConverter}.
 */
public final class Markup {
	private Markup() {
	}

	/** HTML из мастерской → блоки разметки; {@code null} остаётся {@code null}, пустой текст — пустой список. */
	public static List<String> fromHtml(String html) {
		if (html == null) {
			return null;
		}
		return HtmlToMarkupConverter.convert(html, new MarkupReport());
	}

	/** Блоки разметки → HTML старого фронтенда; пустое описание — {@code null}. */
	public static String toHtml(List<String> blocks) {
		return MarkupHtmlRenderer.render(blocks);
	}

	/** {@code null}, если блоков нет (для необязательных описаний вроде «На больших уровнях»). */
	public static List<String> emptyToNull(List<String> blocks) {
		return blocks == null || blocks.isEmpty() ? null : blocks;
	}
}
