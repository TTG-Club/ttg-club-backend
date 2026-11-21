package club.dnd5.portal.util.markup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Разбор разметки core-api в дерево узлов — по грамматике парсера core-app
 * ({@code core-app/app/shared/ui/markup/parser.ts}): {@code {@имя тело | ключ:значение | …}}, вложенные
 * маркеры, имена без учёта регистра, алиасы. Если атрибуты не разбираются (часть после {@code |} без
 * двоеточия), весь текст после имени — содержимое, как в core-app. Незакрытый маркер остаётся текстом.
 */
public final class MarkupParser {
	private static final int MAX_DEPTH = 7;

	/** Алиас (в нижнем регистре) → каноническое имя маркера. */
	private static final Map<String, String> ALIASES = new LinkedHashMap<>();

	static {
		alias("bold", "bold", "b");
		alias("italic", "italic", "i");
		alias("underline", "underline", "u");
		alias("strikethrough", "strikethrough", "s");
		alias("superscript", "superscript", "sup");
		alias("subscript", "subscript", "sub");
		alias("highlight", "highlight", "mark");
		alias("break", "break", "br");
		alias("link", "link", "a");
		alias("kbd", "kbd");
		alias("badge", "badge");
		alias("roll", "roll", "dice");
		alias("heading", "heading", "h");
		alias("paragraph", "paragraph", "p");
		alias("quote", "quote", "blockquote", "q");
		alias("separator", "separator");
		alias("list", "list");
		alias("li", "li", "listitem");
		alias("table", "table");
		alias("tr", "tr");
		alias("th", "th");
		alias("td", "td");
		alias("caption", "caption");
		alias("class", "class");
		alias("species", "species", "race");
		alias("spell", "spell");
		alias("feat", "feat");
		alias("background", "background");
		alias("magicItem", "magicitem", "magic-item");
		alias("creature", "creature", "bestiary");
		alias("item", "item");
		alias("glossary", "glossary");
	}

	private static void alias(String type, String... names) {
		for (String name : names) {
			ALIASES.put(name.toLowerCase(Locale.ROOT), type);
		}
	}

	/** Узел разметки: текст ({@code type == null}) или маркер. */
	public record Node(String type, String text, Map<String, String> attrs, List<Node> content) {
		static Node text(String text) {
			return new Node(null, text, Map.of(), List.of());
		}

		public boolean isText() {
			return type == null;
		}

		/** Текст узла без маркеров (для формулы броска, url и т. п.). */
		public String plainText() {
			if (isText()) {
				return text;
			}
			StringBuilder out = new StringBuilder();
			content.forEach(node -> out.append(node.plainText()));
			return out.toString();
		}
	}

	private MarkupParser() {
	}

	public static List<Node> parse(String source) {
		return parse(source == null ? "" : source, 0);
	}

	private static List<Node> parse(String source, int depth) {
		List<Node> nodes = new ArrayList<>();
		StringBuilder text = new StringBuilder();
		int i = 0;
		while (i < source.length()) {
			int start = source.indexOf("{@", i);
			if (start < 0) {
				text.append(source, i, source.length());
				break;
			}
			int end = depth >= MAX_DEPTH ? -1 : matchingBrace(source, start);
			if (end < 0) {
				text.append(source, i, source.length());
				break;
			}
			text.append(source, i, start);
			Node marker = marker(source.substring(start + 2, end), depth);
			if (marker == null) {
				text.append(source, start, end + 1);
			} else {
				flushText(nodes, text);
				nodes.add(marker);
			}
			i = end + 1;
		}
		flushText(nodes, text);
		return nodes;
	}

	private static void flushText(List<Node> nodes, StringBuilder text) {
		if (!text.isEmpty()) {
			nodes.add(Node.text(text.toString()));
			text.setLength(0);
		}
	}

	/** Индекс «}», закрывающей маркер с «{@» в позиции {@code start}, или -1. */
	private static int matchingBrace(String s, int start) {
		int depth = 0;
		for (int i = start; i < s.length(); i++) {
			if (s.startsWith("{@", i)) {
				depth++;
				i++;
			} else if (s.charAt(i) == '}') {
				depth--;
				if (depth == 0) {
					return i;
				}
			}
		}
		return -1;
	}

	/** Маркер из внутренности {@code имя тело | атрибуты}; неизвестное имя — содержимое как текст-узлы. */
	private static Node marker(String inner, int depth) {
		int space = indexOfWhitespace(inner);
		String name = (space < 0 ? inner : inner.substring(0, space)).toLowerCase(Locale.ROOT);
		String body = space < 0 ? "" : inner.substring(space + 1);
		String type = ALIASES.get(name);
		List<String> parts = splitTopLevelPipes(body);
		String contentSource = parts.getFirst();
		Map<String, String> attrs = new LinkedHashMap<>();
		boolean attrsValid = true;
		for (String part : parts.subList(1, parts.size())) {
			int colon = part.indexOf(':');
			if (colon <= 0) {
				attrsValid = false;
				break;
			}
			attrs.put(part.substring(0, colon).trim(), part.substring(colon + 1).trim());
		}
		if (!attrsValid) {
			attrs.clear();
			contentSource = body;
		}
		if (parts.size() > 1 && attrsValid) {
			contentSource = contentSource.stripTrailing();
		}
		List<Node> content = parse(contentSource, depth + 1);
		if (type == null) {
			// неизвестный маркер: текст содержимого (как recoverMarkerText в core-app)
			return new Node("unknown", null, attrs, content);
		}
		return new Node(type, null, attrs, content);
	}

	private static int indexOfWhitespace(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (Character.isWhitespace(s.charAt(i))) {
				return i;
			}
		}
		return -1;
	}

	/** Делит по «|», не входящим во вложенные маркеры. */
	private static List<String> splitTopLevelPipes(String body) {
		List<String> parts = new ArrayList<>();
		int depth = 0;
		int from = 0;
		for (int i = 0; i < body.length(); i++) {
			if (body.startsWith("{@", i)) {
				depth++;
				i++;
			} else if (body.charAt(i) == '}' && depth > 0) {
				depth--;
			} else if (body.charAt(i) == '|' && depth == 0) {
				parts.add(body.substring(from, i));
				from = i + 1;
			}
		}
		parts.add(body.substring(from));
		return parts;
	}
}
