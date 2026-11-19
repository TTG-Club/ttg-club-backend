package club.dnd5.portal.util.markup;

import club.dnd5.portal.util.markup.MarkupParser.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Мост этапа 3: разметка core-api → HTML старого фронтенда (компоненты {@code <dice-roller>},
 * {@code <detail-tooltip>}, таблицы {@code dnd5_table}). Нужен, пока фронтенд не рендерит разметку сам.
 * Обратная операция — {@link HtmlToMarkupConverter}: {@code convert(render(blocks)) == blocks}.
 */
public final class MarkupHtmlRenderer {
	private static final Set<String> BLOCK_TYPES = Set.of("heading", "paragraph", "quote", "separator", "list", "table");

	/** Маркер-ссылка → путь раздела на старом сайте и тип всплывающей подсказки (null — без неё). */
	private record Section(String path, String tooltip) { }

	private static final Map<String, Section> SECTIONS = Map.of(
		"spell", new Section("/spells/", "spell"),
		"creature", new Section("/bestiary/", "creature"),
		"magicItem", new Section("/items/magic/", "magic-item"),
		"item", new Section("/items/", "item"),
		"feat", new Section("/traits/", "trait"),
		"class", new Section("/classes/", null),
		"species", new Section("/races/", null),
		"background", new Section("/backgrounds/", null),
		"glossary", new Section("/screens/", "screen"));

	/** Первый сегмент внутренней ссылки {@code {@link}} → тип подсказки. */
	private static final Map<String, String> LINK_TOOLTIPS = Map.of(
		"screens", "screen",
		"conditions", "screen",
		"weapons", "weapon",
		"armors", "armor",
		"rules", "rule",
		"options", "option",
		"gods", "god");

	private MarkupHtmlRenderer() {
	}

	/** Блоки разметки → HTML; {@code null} для пустого описания. */
	public static String render(List<String> blocks) {
		if (blocks == null || blocks.isEmpty()) {
			return null;
		}
		StringBuilder out = new StringBuilder();
		for (String block : blocks) {
			renderBlock(MarkupParser.parse(block), out);
		}
		return out.toString();
	}

	/** Блок: блочные маркеры — сами по себе, подряд идущий инлайн — абзацем {@code <p>}. */
	private static void renderBlock(List<Node> nodes, StringBuilder out) {
		List<Node> run = new ArrayList<>();
		for (Node node : nodes) {
			if (!node.isText() && BLOCK_TYPES.contains(node.type())) {
				paragraph(run, out);
				block(node, out);
			} else {
				run.add(node);
			}
		}
		paragraph(run, out);
	}

	private static void paragraph(List<Node> run, StringBuilder out) {
		String html = inline(run).strip();
		run.clear();
		if (!html.isEmpty()) {
			out.append("<p>").append(html).append("</p>");
		}
	}

	private static void block(Node node, StringBuilder out) {
		switch (node.type()) {
			case "heading" -> {
				int level = Math.clamp(parseInt(node.attrs().get("level"), 1), 1, 4) + 2;
				out.append("<h").append(level).append(align(node)).append('>')
					.append(inline(node.content()).strip()).append("</h").append(level).append('>');
			}
			case "paragraph" -> out.append("<p").append(align(node)).append('>')
				.append(inline(node.content()).strip()).append("</p>");
			case "separator" -> out.append("<hr>");
			case "quote" -> quote(node, out);
			case "list" -> list(node, out);
			case "table" -> table(node, out);
			default -> out.append(inline(List.of(node)));
		}
	}

	private static void quote(Node node, StringBuilder out) {
		out.append("<blockquote>");
		List<Node> paragraph = new ArrayList<>();
		List<Node> content = node.content();
		for (int i = 0; i < content.size(); i++) {
			Node child = content.get(i);
			boolean doubleBreak = isBreak(child) && i + 1 < content.size() && isBreak(content.get(i + 1));
			if (doubleBreak) {
				paragraph(paragraph, out);
				i++;
			} else {
				paragraph.add(child);
			}
		}
		paragraph(paragraph, out);
		out.append("</blockquote>");
	}

	private static boolean isBreak(Node node) {
		return "break".equals(node.type());
	}

	private static void list(Node node, StringBuilder out) {
		boolean ordered = "ordered".equals(node.attrs().get("type"));
		List<StringBuilder> items = new ArrayList<>();
		for (Node child : node.content()) {
			if ("li".equals(child.type())) {
				items.add(new StringBuilder(inline(child.content()).strip().replace(HtmlToMarkupConverter.CELL_PLACEHOLDER, "")));
			} else if ("list".equals(child.type())) {
				StringBuilder nested = new StringBuilder();
				list(child, nested);
				if (items.isEmpty()) {
					items.add(new StringBuilder());
				}
				items.getLast().append(nested);
			}
		}
		out.append(ordered ? "<ol>" : "<ul>");
		items.forEach(item -> out.append("<li>").append(item).append("</li>"));
		out.append(ordered ? "</ol>" : "</ul>");
	}

	private static void table(Node node, StringBuilder out) {
		StringBuilder head = new StringBuilder();
		StringBuilder body = new StringBuilder();
		String caption = null;
		for (Node child : node.content()) {
			if ("caption".equals(child.type())) {
				caption = inline(child.content()).strip();
			} else if ("tr".equals(child.type())) {
				List<Node> cells = child.content().stream()
					.filter(c -> "th".equals(c.type()) || "td".equals(c.type()))
					.toList();
				boolean header = !cells.isEmpty() && cells.stream().allMatch(c -> "th".equals(c.type()));
				StringBuilder row = header ? head : body;
				row.append("<tr>");
				for (Node cell : cells) {
					String tag = header ? "th" : "td";
					row.append('<').append(tag).append(align(cell)).append('>')
						.append(inline(cell.content()).strip().replace(HtmlToMarkupConverter.CELL_PLACEHOLDER, ""))
						.append("</").append(tag).append('>');
				}
				row.append("</tr>");
			}
		}
		out.append("<div class=\"table-responsive\"><table class=\"dnd5_table\">");
		if (caption != null && !caption.isEmpty()) {
			out.append("<caption>").append(caption).append("</caption>");
		}
		if (!head.isEmpty()) {
			out.append("<thead>").append(head).append("</thead>");
		}
		out.append("<tbody>").append(body).append("</tbody></table></div>");
	}

	// ---------------------------------------------------------------- инлайн

	private static String inline(List<Node> nodes) {
		StringBuilder out = new StringBuilder();
		for (Node node : nodes) {
			if (node.isText()) {
				out.append(escape(node.text()));
				continue;
			}
			switch (node.type()) {
				case "bold" -> wrap(out, "strong", node);
				case "italic" -> wrap(out, "em", node);
				case "underline" -> wrap(out, "u", node);
				case "strikethrough" -> wrap(out, "s", node);
				case "superscript" -> wrap(out, "sup", node);
				case "subscript" -> wrap(out, "sub", node);
				case "highlight" -> wrap(out, "mark", node);
				case "break" -> out.append("<br>");
				case "roll" -> {
					String formula = node.plainText().strip();
					String shown = node.attrs().getOrDefault("text", formula);
					out.append("<dice-roller formula=\"").append(escape(formula)).append("\">")
						.append(escape(shown)).append("</dice-roller>");
				}
				case "link" -> link(out, node);
				case "spell", "creature", "magicItem", "item", "feat", "class", "species", "background", "glossary" ->
					section(out, node);
				case "badge", "kbd" -> wrap(out, "span", node);
				default -> out.append(inline(node.content()));
			}
		}
		return out.toString();
	}

	private static void wrap(StringBuilder out, String tag, Node node) {
		out.append('<').append(tag).append('>').append(inline(node.content())).append("</").append(tag).append('>');
	}

	private static void section(StringBuilder out, Node node) {
		String url = node.attrs().get("url");
		String text = inline(node.content());
		if (url == null || url.isBlank()) {
			out.append(text);
			return;
		}
		Section section = SECTIONS.get(node.type());
		anchor(out, section.path() + url.strip(), text, section.tooltip(), false);
	}

	private static void link(StringBuilder out, Node node) {
		String url = node.attrs().get("url");
		String text = inline(node.content());
		if (url == null || url.isBlank()) {
			out.append(text);
			return;
		}
		url = url.strip();
		if (url.startsWith("/")) {
			String first = url.substring(1).split("/", 2)[0].toLowerCase(Locale.ROOT);
			anchor(out, url, text, LINK_TOOLTIPS.get(first), false);
		} else {
			anchor(out, url, text, null, "_blank".equals(node.attrs().get("target")));
		}
	}

	private static void anchor(StringBuilder out, String href, String text, String tooltip, boolean blank) {
		if (tooltip != null) {
			out.append("<detail-tooltip type=\"").append(tooltip).append("\">");
		}
		out.append("<a href=\"").append(escape(href)).append('"');
		if (blank) {
			out.append(" target=\"_blank\" rel=\"noopener noreferrer\"");
		}
		out.append('>').append(text).append("</a>");
		if (tooltip != null) {
			out.append("</detail-tooltip>");
		}
	}

	private static String align(Node node) {
		String align = node.attrs().get("align");
		if (align == null || !(align.equals("center") || align.equals("right") || align.equals("left"))) {
			return "";
		}
		return " style=\"text-align: " + align + ";\"";
	}

	private static int parseInt(String value, int fallback) {
		try {
			return value == null ? fallback : Integer.parseInt(value.strip());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static String escape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
