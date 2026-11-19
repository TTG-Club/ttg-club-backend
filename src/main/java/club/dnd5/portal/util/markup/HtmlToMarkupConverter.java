package club.dnd5.portal.util.markup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Перевод HTML-описаний старого сайта в разметку core-api ({@code core-app/docs/markup-formatting-guide.md}).
 * <p>
 * Результат — список блоков: каждый элемент — строка-исходник одного блока (абзац с инлайн-маркерами
 * {@code {@b …}}, {@code {@roll …}}, {@code {@spell …|url:…}} либо блочный маркер {@code {@h}},
 * {@code {@list}}, {@code {@table}}, {@code {@quote}}, {@code {@separator}}). Такой массив строк —
 * штатная форма хранения core-api (рендер разбирает каждую строку парсером), в БД он кладётся JSON-ом.
 * <p>
 * В разметке нет экранирования: внутри маркера символы {@code { } |} недопустимы — фигурные скобки
 * выбрасываются, {@code |} заменяется на {@code /}. Всё, что переводится неоднозначно или теряется
 * (стили ячеек, colspan, картинки, неизвестные теги и классы), отмечается в {@link MarkupReport}.
 */
public final class HtmlToMarkupConverter {
	/** Пустая ячейка таблицы: {@code {@td}} без текста парсер не принимает. */
	static final String CELL_PLACEHOLDER = "​";
	private static final String BR = "{@br}";

	/** Раздел старого сайта → маркер-ссылка core-api (url — часть адреса после раздела). */
	private static final Map<String, String> SECTION_MARKERS = Map.of(
		"spells", "spell",
		"bestiary", "creature",
		"classes", "class",
		"races", "species",
		"traits", "feat",
		"feats", "feat",
		"backgrounds", "background",
		"items", "item");

	/** Хосты, ссылки на которые — внутренние (адрес приводится к пути). */
	private static final Pattern OWN_HOST = Pattern.compile(
		"^https?://(?:www\\.)?(?:5e14\\.)?(?:dev\\.)?(?:ttg\\.club|dnd5\\.club)", Pattern.CASE_INSENSITIVE);

	/** Классы, которые только оформляли текст старого сайта: снимаются молча. */
	private static final Set<String> STYLE_ONLY_CLASSES = Set.of(
		"text-justify", "mb-1", "mb-2", "pl-2", "pl-4", "title", "content", "indent", "spell-list",
		"table-responsive", "table-wrapper", "dnd5_table", "table", "table-striped", "table-sm",
		"lead", "caption_text", "additionalInfo");

	/** Классы span, семантика которых в разметке не выражается — текст остаётся, класс в отчёт. */
	private static final Set<String> SPAN_SEMANTIC_CLASSES = Set.of(
		"saving_throw", "advantage", "disadvantage", "dice_text");

	/** Самозакрывающийся пользовательский тег (с дефисом в имени): {@code <dice-roller formula="…"/>}. */
	private static final Pattern SELF_CLOSING_CUSTOM = Pattern.compile("<([a-zA-Z][\\w]*-[\\w-]*)((?:\\s[^<>]*?)?)\\s*/>");
	private static final Pattern WHITESPACE =Pattern.compile("[ \\t\\n\\r\\f]+");
	private static final Pattern LEADING_BRS = Pattern.compile("^(?:\\s| |\\{@br\\})+");
	private static final Pattern TRAILING_BRS = Pattern.compile("(?:\\s| |\\{@br\\})+$");
	private static final Pattern MANY_BRS = Pattern.compile("(?:\\{@br\\}\\s*){3,}");
	private static final Pattern TEXT_ALIGN = Pattern.compile("text-align\\s*:\\s*(left|center|right)",
		Pattern.CASE_INSENSITIVE);

	private final MarkupReport report;
	private final List<String> blocks = new ArrayList<>();
	private final StringBuilder paragraph = new StringBuilder();

	private HtmlToMarkupConverter(MarkupReport report) {
		this.report = report;
	}

	/**
	 * Перевести HTML в блоки разметки.
	 *
	 * @param html   описание (HTML-фрагмент); {@code null} и пустое дают пустой список
	 * @param report куда складывать замечания
	 * @return блоки разметки (без пустых)
	 */
	public static List<String> convert(String html, MarkupReport report) {
		if (html == null || html.isBlank()) {
			return List.of();
		}
		HtmlToMarkupConverter converter = new HtmlToMarkupConverter(report);
		// HTML-парсер не признаёт «/>» у пользовательских тегов: <dice-roller …/> захватил бы текст после себя
		String closed = SELF_CLOSING_CUSTOM.matcher(html).replaceAll("<$1$2></$1>");
		converter.blockLevel(Jsoup.parseBodyFragment(closed).body());
		converter.flush(null);
		return converter.blocks;
	}

	// ---------------------------------------------------------------- блочный уровень

	private void blockLevel(Element container) {
		for (Node child : container.childNodes()) {
			if (child instanceof TextNode text) {
				paragraph.append(text(text, false));
			} else if (child instanceof Element element) {
				blockElement(element);
			}
		}
	}

	private void blockElement(Element el) {
		String tag = el.normalName();
		switch (tag) {
			case "p" -> {
				flush(null);
				String align = alignOf(el);
				paragraph.append(inlineChildren(el, align != null));
				flush(align);
			}
			case "div", "section", "article", "header", "footer", "main", "center", "tbody", "thead" -> {
				flush(null);
				noteClasses(el);
				blockLevel(el);
				flush(null);
			}
			case "h1", "h2", "h3", "h4", "h5", "h6" -> {
				flush(null);
				addHeading(el);
			}
			case "ul", "ol" -> {
				flush(null);
				addBlock(list(el));
			}
			case "table" -> {
				flush(null);
				addBlock(table(el));
			}
			case "blockquote" -> {
				flush(null);
				String body = blocksInline(el, BR + BR);
				if (!body.isEmpty()) {
					addBlock("{@quote " + body + "}");
				}
			}
			case "hr" -> {
				flush(null);
				blocks.add("{@separator}");
			}
			case "br" -> paragraph.append(BR);
			case "details" -> {
				flush(null);
				report.note("tag:details", el.outerHtml());
				blockLevel(el);
				flush(null);
			}
			case "summary" -> {
				flush(null);
				String body = inlineChildren(el, true).trim();
				if (!body.isEmpty()) {
					addBlock("{@b " + body + "}");
				}
			}
			default -> paragraph.append(inline(el, false));
		}
	}

	private void flush(String align) {
		String body = cleanBrs(paragraph.toString());
		paragraph.setLength(0);
		if (isBlank(body)) {
			return;
		}
		blocks.add(align == null ? body : "{@p " + body + " | align:" + align + "}");
	}

	private void addBlock(String block) {
		if (block != null && !block.isEmpty()) {
			blocks.add(block);
		}
	}

	private void addHeading(Element el) {
		String body = cleanBrs(inlineChildren(el, true));
		if (isBlank(body)) {
			return;
		}
		int level = switch (el.normalName()) {
			case "h4" -> 2;
			case "h5" -> 3;
			case "h6" -> 4;
			default -> 1; // h1–h3: самый крупный заголовок разметки (рисуется как h3)
		};
		String align = alignOf(el);
		blocks.add("{@h " + body + " | level:" + level + (align == null ? "" : " | align:" + align) + "}");
	}

	// ---------------------------------------------------------------- списки и таблицы

	private String list(Element el) {
		StringBuilder items = new StringBuilder();
		for (Element child : el.children()) {
			if (child.normalName().equals("li")) {
				String body = cleanBrs(liBody(child));
				items.append("{@li ").append(isBlank(body) ? CELL_PLACEHOLDER : body).append('}');
				for (Element nested : child.children()) {
					if (nested.normalName().equals("ul") || nested.normalName().equals("ol")) {
						items.append(list(nested));
					}
				}
			} else if (child.normalName().equals("ul") || child.normalName().equals("ol")) {
				items.append(list(child));
			} else {
				report.note("list:non-li", child.outerHtml());
			}
		}
		if (items.isEmpty()) {
			return null;
		}
		return "{@list " + items + (el.normalName().equals("ol") ? " | type:ordered" : "") + "}";
	}

	/** Содержимое пункта без вложенных списков (они идут отдельными {@list} следом). */
	private String liBody(Element li) {
		Element copy = li.clone();
		copy.children().stream()
			.filter(c -> c.normalName().equals("ul") || c.normalName().equals("ol"))
			.toList()
			.forEach(Node::remove);
		return blocksInline(copy, BR);
	}

	private String table(Element el) {
		List<Element> rows = new ArrayList<>();
		String caption = null;
		boolean headerFromThead = false;
		for (Element child : el.children()) {
			switch (child.normalName()) {
				case "tr" -> rows.add(child);
				case "thead" -> {
					headerFromThead = rows.isEmpty();
					rows.addAll(child.children().stream().filter(r -> r.normalName().equals("tr")).toList());
				}
				case "tbody", "tfoot" ->
					rows.addAll(child.children().stream().filter(r -> r.normalName().equals("tr")).toList());
				case "caption" -> caption = cleanBrs(blocksInline(child, BR));
				case "colgroup", "col" -> { }
				default -> report.note("table:child:" + child.normalName(), child.outerHtml());
			}
		}
		if (!el.select("table table").isEmpty()) {
			report.note("table:nested", el.outerHtml());
		}
		if (rows.isEmpty()) {
			return null;
		}
		StringBuilder out = new StringBuilder("{@table ");
		if (!isBlank(caption)) {
			out.append("{@caption ").append(caption).append('}');
		}
		for (int i = 0; i < rows.size(); i++) {
			Element row = rows.get(i);
			List<Element> cells = row.children().stream()
				.filter(c -> c.normalName().equals("td") || c.normalName().equals("th"))
				.toList();
			if (cells.isEmpty()) {
				continue;
			}
			boolean header = i == 0 && (headerFromThead || cells.stream().allMatch(c -> c.normalName().equals("th")));
			out.append("{@tr ");
			for (Element cell : cells) {
				String body = cleanBrs(blocksInline(cell, BR));
				if (!header && cell.normalName().equals("th") && !isBlank(body)) {
					body = "{@b " + body + "}";
				}
				String align = alignOf(cell);
				String marker = header ? "th" : "td";
				String cellMarkup = "{@" + marker + " " + (isBlank(body) ? CELL_PLACEHOLDER : body)
					+ (align == null ? "" : " | align:" + align) + "}";
				out.append(cellMarkup);
				int colspan = span(cell, "colspan");
				if (colspan > 1) {
					report.note("table:colspan", row.outerHtml());
					for (int k = 1; k < colspan; k++) {
						out.append("{@").append(marker).append(' ').append(CELL_PLACEHOLDER).append('}');
					}
				}
				if (span(cell, "rowspan") > 1) {
					report.note("table:rowspan", row.outerHtml());
				}
			}
			out.append('}');
		}
		return out.append('}').toString();
	}

	private static int span(Element cell, String attr) {
		try {
			return cell.hasAttr(attr) ? Integer.parseInt(cell.attr(attr).trim()) : 1;
		} catch (NumberFormatException e) {
			return 1;
		}
	}

	/**
	 * Содержимое контейнера одной инлайн-строкой (для ячейки, пункта, цитаты): блоки внутри
	 * (абзацы, div, заголовки) соединяются разделителем {@code separator}.
	 */
	private String blocksInline(Element container, String separator) {
		List<String> parts = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		for (Node child : container.childNodes()) {
			if (child instanceof TextNode text) {
				current.append(text(text, true));
				continue;
			}
			if (!(child instanceof Element el)) {
				continue;
			}
			switch (el.normalName()) {
				case "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "blockquote", "section", "summary", "details" -> {
					parts.add(current.toString());
					current.setLength(0);
					String inner = blocksInline(el, separator);
					parts.add(el.normalName().startsWith("h") && !el.normalName().equals("h") && !isBlank(inner)
						? "{@b " + cleanBrs(inner) + "}" : inner);
				}
				case "ul", "ol" -> {
					parts.add(current.toString());
					current.setLength(0);
					report.note("inline:list", el.outerHtml());
					for (Element li : el.children()) {
						parts.add("• " + cleanBrs(blocksInline(li, BR)));
					}
				}
				case "table" -> {
					parts.add(current.toString());
					current.setLength(0);
					report.note("inline:table", el.outerHtml());
					for (Element tr : el.select("tr")) {
						parts.add(String.join(" / ", tr.children().stream()
							.map(td -> cleanBrs(blocksInline(td, BR))).toList()));
					}
				}
				case "hr" -> {
					parts.add(current.toString());
					current.setLength(0);
				}
				default -> current.append(inline(el, true));
			}
		}
		parts.add(current.toString());
		List<String> nonEmpty = parts.stream().map(HtmlToMarkupConverter::cleanBrs).filter(p -> !isBlank(p)).toList();
		return MANY_BRS.matcher(String.join(separator, nonEmpty)).replaceAll(BR + BR);
	}

	// ---------------------------------------------------------------- инлайн

	private String inlineChildren(Element el, boolean inMarker) {
		StringBuilder out = new StringBuilder();
		for (Node child : el.childNodes()) {
			if (child instanceof TextNode text) {
				out.append(text(text, inMarker));
			} else if (child instanceof Element element) {
				out.append(inline(element, inMarker));
			}
		}
		return out.toString();
	}

	private String inline(Element el, boolean inMarker) {
		String tag = el.normalName();
		return switch (tag) {
			case "strong", "b" -> wrap("b", el);
			case "em", "i" -> wrap("i", el);
			case "u" -> wrap("u", el);
			case "s", "strike", "del" -> wrap("s", el);
			case "sup" -> wrap("sup", el);
			case "sub" -> wrap("sub", el);
			case "mark" -> wrap("mark", el);
			case "br" -> BR;
			case "a" -> link(el);
			case "dice-roller", "dice" -> roll(el);
			case "detail-tooltip" -> inlineChildren(el, inMarker);
			case "span" -> {
				for (String cls : el.classNames()) {
					if (SPAN_SEMANTIC_CLASSES.contains(cls)) {
						report.note("span:" + cls, el.outerHtml());
					} else if (!STYLE_ONLY_CLASSES.contains(cls)) {
						report.note("span:other-class:" + cls, el.outerHtml());
					}
				}
				yield inlineChildren(el, inMarker);
			}
			case "nobr", "small", "font", "abbr", "label", "code", "t", "big", "tt", "cite" -> {
				if (!tag.equals("nobr") && !tag.equals("small")) {
					report.note("tag:" + tag, el.outerHtml());
				}
				yield inlineChildren(el, inMarker);
			}
			case "img", "iframe", "script", "style", "video", "audio", "svg" -> {
				report.note("tag:" + tag, el.outerHtml());
				yield "";
			}
			case "p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "ul", "ol", "table", "blockquote", "li" -> {
				// блок внутри инлайна (например абзац внутри ссылки) — сплющиваем
				report.note("inline:block:" + tag, el.outerHtml());
				yield blocksInline(el, BR);
			}
			default -> {
				report.note("tag:" + tag, el.outerHtml());
				yield inlineChildren(el, inMarker);
			}
		};
	}

	/** {@code {@name …}} с сохранением пробелов по краям снаружи маркера. */
	private String wrap(String name, Element el) {
		return wrapBody(name, inlineChildren(el, true), "");
	}

	private static String wrapBody(String name, String raw, String attrs) {
		String body = cleanBrs(raw);
		if (isBlank(body)) {
			return leadingSpace(raw).isEmpty() && trailingSpace(raw).isEmpty() ? "" : " ";
		}
		return leadingSpace(raw) + "{@" + name + " " + body + attrs + "}" + trailingSpace(raw);
	}

	private String link(Element a) {
		String raw = inlineChildren(a, true);
		String href = a.attr("href").trim();
		if (href.isEmpty() || href.startsWith("#") || href.startsWith("javascript:")) {
			report.note("link:empty-href", a.outerHtml());
			return raw;
		}
		if (isBlank(cleanBrs(raw))) {
			report.note("link:empty-text", a.outerHtml());
			return raw;
		}
		Matcher own = OWN_HOST.matcher(href);
		if (own.find()) {
			href = href.substring(own.end());
			if (href.isEmpty()) {
				href = "/";
			}
		}
		if (href.matches("(?i)^https?://.*")) {
			return wrapBody("link", raw, " | url:" + attrValue(href) + " | target:_blank");
		}
		if (href.matches("(?i)^[a-z]+:.*")) {
			report.note("link:scheme", a.outerHtml());
			return raw;
		}
		String path = href.startsWith("/") ? href : "/" + href;
		if (!href.startsWith("/")) {
			report.note("link:relative", a.outerHtml());
		}
		int query = path.indexOf('?');
		if (query >= 0) {
			report.note("link:query", a.outerHtml());
		}
		String[] parts = path.substring(1).split("/", 2);
		String section = parts[0].toLowerCase(Locale.ROOT);
		String rest = parts.length > 1 ? parts[1] : "";
		if (section.equals("items") && rest.startsWith("magic/")) {
			return wrapBody("magicItem", raw, " | url:" + attrValue(rest.substring("magic/".length())));
		}
		String marker = SECTION_MARKERS.get(section);
		if (marker != null && !rest.isEmpty() && query < 0) {
			return wrapBody(marker, raw, " | url:" + attrValue(rest));
		}
		return wrapBody("link", raw, " | url:" + attrValue(path));
	}

	private String roll(Element el) {
		String formula = el.hasAttr("formula") ? el.attr("formula").trim() : el.text().trim();
		String shown = cleanBrs(inlineChildren(el, true)).trim();
		for (Attribute attribute : el.attributes()) {
			String key = attribute.getKey();
			if (!key.equals("formula") && !key.equals("label")) {
				report.note("dice:attr:" + key, el.outerHtml());
			}
		}
		formula = sanitize(formula).trim();
		if (formula.isEmpty()) {
			report.note("dice:no-formula", el.outerHtml());
			return shown;
		}
		boolean showFormula = shown.isEmpty() || shown.replace(" ", "").equalsIgnoreCase(formula.replace(" ", ""));
		return "{@roll " + formula + (showFormula ? "" : " | text:" + attrValue(shown)) + "}";
	}

	// ---------------------------------------------------------------- текст

	private String text(TextNode node, boolean inMarker) {
		String text = WHITESPACE.matcher(node.getWholeText()).replaceAll(" ");
		if (inMarker) {
			String clean = sanitize(text);
			if (!clean.equals(text)) {
				report.note("text:reserved-char-in-marker", text);
			}
			return clean;
		}
		if (text.contains("{@")) {
			report.note("text:marker-like", text);
		}
		return text;
	}

	/** Внутри маркера нельзя {@code { } |}: скобки убираем, черту заменяем на «/». */
	private static String sanitize(String value) {
		return value.replace("{", "").replace("}", "").replace('|', '/');
	}

	/** Значение атрибута маркера: без пробелов по краям и без служебных символов. */
	private static String attrValue(String value) {
		return sanitize(value).trim();
	}

	private String alignOf(Element el) {
		for (String cls : el.classNames()) {
			switch (cls) {
				case "text-center" -> {
					return "center";
				}
				case "text-right" -> {
					return "right";
				}
				default -> {
					if (!STYLE_ONLY_CLASSES.contains(cls) && !cls.startsWith("text-")) {
						report.note("class:" + el.normalName() + "." + cls, el.outerHtml());
					}
				}
			}
		}
		Matcher m = TEXT_ALIGN.matcher(el.attr("style"));
		if (m.find()) {
			String align = m.group(1).toLowerCase(Locale.ROOT);
			return align.equals("left") ? null : align;
		}
		String attr = el.attr("align").toLowerCase(Locale.ROOT);
		return attr.equals("center") || attr.equals("right") ? attr : null;
	}

	private void noteClasses(Element el) {
		for (String cls : el.classNames()) {
			if (!STYLE_ONLY_CLASSES.contains(cls) && !cls.startsWith("text-")) {
				report.note("class:" + el.normalName() + "." + cls, el.outerHtml());
			}
		}
	}

	private static String cleanBrs(String value) {
		String result = LEADING_BRS.matcher(value).replaceAll("");
		return TRAILING_BRS.matcher(result).replaceAll("");
	}

	private static boolean isBlank(String value) {
		return value == null || value.replace(' ', ' ').replace(CELL_PLACEHOLDER, "").isBlank();
	}

	private static String leadingSpace(String raw) {
		int i = 0;
		while (i < raw.length() && (raw.charAt(i) == ' ' || raw.charAt(i) == ' ')) {
			i++;
		}
		return raw.substring(0, i);
	}

	private static String trailingSpace(String raw) {
		int i = raw.length();
		while (i > 0 && (raw.charAt(i - 1) == ' ' || raw.charAt(i - 1) == ' ')) {
			i--;
		}
		return raw.substring(i);
	}
}
