package club.dnd5.portal.util.markup;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Описания в формате core-api: хранятся массивом строк-блоков разметки (jsonb) и так же отдаются API,
 * а HTML (экспорт VTTG/FVTT, поиск, старые ревизии) получается и переводится мостом
 * {@link MarkupHtmlRenderer} / {@link HtmlToMarkupConverter}.
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

	/**
	 * HTML из блоков для кода, который разбирает HTML (экспорт в Foundry, VTTG); без блоков — пустая
	 * строка, а не {@code null}.
	 */
	public static String htmlOrEmpty(List<String> blocks) {
		String html = toHtml(blocks);
		return html == null ? "" : html;
	}

	/** Блоки без пробелов по краям и без пустых; {@code null} остаётся {@code null}. */
	public static List<String> trimBlocks(List<String> blocks) {
		if (blocks == null) {
			return null;
		}
		return blocks.stream().map(String::trim).filter(block -> !block.isEmpty()).collect(Collectors.toList());
	}

	/** {@code null}, если блоков нет (для необязательных описаний вроде «На больших уровнях»). */
	public static List<String> emptyToNull(List<String> blocks) {
		return blocks == null || blocks.isEmpty() ? null : blocks;
	}
}
