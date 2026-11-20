package club.dnd5.portal.util.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Описания в формате core-api: хранятся массивом строк-блоков разметки (jsonb) и так же отдаются API,
 * а HTML (экспорт VTTG/FVTT, поиск, старые ревизии) получается и переводится мостом
 * {@link MarkupHtmlRenderer} / {@link HtmlToMarkupConverter}.
 */
public final class Markup {
	/** Абзац, который рендер выдаёт для обычного блока: {@code <p>…</p>} без атрибутов. */
	private static final Pattern PARAGRAPH = Pattern.compile("(?s)<p>(.*)</p>");

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

	/**
	 * HTML короткого поля для вставки в строку («Доспехи: …»): каждый абзац без обёртки {@code <p>},
	 * абзацы через пробел; без блоков — пустая строка.
	 */
	public static String toInlineHtml(List<String> blocks) {
		if (blocks == null) {
			return "";
		}
		List<String> parts = new ArrayList<>();
		for (String block : blocks) {
			String html = MarkupHtmlRenderer.render(List.of(block));
			if (html == null || html.isBlank()) {
				continue;
			}
			Matcher paragraph = PARAGRAPH.matcher(html);
			parts.add(paragraph.matches() ? paragraph.group(1) : html);
		}
		return String.join(" ", parts);
	}

	/**
	 * Разметка короткого поля, разрезанная по разделителю вне маркеров {@code {@…}}: перечисление
	 * снаряжения предыстории API отдаёт списком. Абзацы склеиваются через пробел.
	 */
	public static List<String> splitInline(List<String> blocks, String separator) {
		List<String> parts = new ArrayList<>();
		if (blocks == null) {
			return parts;
		}
		String text = blocks.stream().map(String::trim).filter(block -> !block.isEmpty()).collect(Collectors.joining(" "));
		int depth = 0;
		int start = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '{') {
				depth++;
			} else if (c == '}' && depth > 0) {
				depth--;
			} else if (depth == 0 && text.startsWith(separator, i)) {
				parts.add(text.substring(start, i));
				start = i + separator.length();
				i = start - 1;
			}
		}
		parts.add(text.substring(start));
		parts.removeIf(String::isBlank);
		return parts;
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
