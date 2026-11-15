package club.dnd5.portal.validation;

import org.jsoup.parser.Parser;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlValidator implements ConstraintValidator<ValidHtml, Object> {
	private static final int MAX_PARSE_ERRORS = 100;
	private static final Set<String> VOID_ELEMENTS = new HashSet<>(Arrays.asList(
		"area", "base", "br", "col", "embed", "hr", "img", "input", "link",
		"meta", "param", "source", "track", "wbr"
	));
	private static final Pattern SELF_CLOSING_TAG = Pattern.compile(
		"<([A-Za-z][A-Za-z0-9:_-]*)((?:[^<>\"'/]|\"[^\"]*\"|'[^']*')*)/>"
	);
	private static final Set<String> OPTIONAL_END_ELEMENTS = new HashSet<>(Arrays.asList(
		"html", "head", "body", "li", "dt", "dd", "p", "rt", "rp", "optgroup",
		"option", "colgroup", "thead", "tbody", "tfoot", "tr", "td", "th"
	));

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null || !(value instanceof CharSequence)) {
			return true;
		}

		String html = value.toString();
		if (html.trim().isEmpty()) {
			return true;
		}

		Parser parser = Parser.htmlParser();
		parser.setTrackErrors(MAX_PARSE_ERRORS);
		parser.parseInput(expandSelfClosingTags(html), "");
		return parser.getErrors().isEmpty() && hasBalancedRequiredTags(html);
	}

	/**
	 * В контенте легитимно встречаются самозакрывающиеся теги
	 * (например, {@code <dice-roller formula="3к6 + 5"/>}). jsoup считает их
	 * незакрытыми, поэтому перед разбором разворачиваем их в пару открывающий/закрывающий.
	 */
	private String expandSelfClosingTags(String html) {
		Matcher matcher = SELF_CLOSING_TAG.matcher(html);
		StringBuilder result = new StringBuilder(html.length());
		while (matcher.find()) {
			String name = matcher.group(1);
			String replacement = VOID_ELEMENTS.contains(name.toLowerCase(Locale.ROOT))
				? matcher.group()
				: "<" + name + matcher.group(2) + "></" + name + ">";
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private boolean hasBalancedRequiredTags(String html) {
		Deque<String> openElements = new ArrayDeque<>();
		int position = 0;

		while ((position = html.indexOf('<', position)) >= 0) {
			if (html.startsWith("<!--", position)) {
				int commentEnd = html.indexOf("-->", position + 4);
				if (commentEnd < 0) {
					return false;
				}
				position = commentEnd + 3;
				continue;
			}

			int tagEnd = findTagEnd(html, position + 1);
			if (tagEnd < 0) {
				return !looksLikeTagStart(html, position + 1);
			}

			String tag = html.substring(position + 1, tagEnd).trim();
			position = tagEnd + 1;
			if (tag.isEmpty() || tag.charAt(0) == '!' || tag.charAt(0) == '?') {
				continue;
			}

			boolean closing = tag.charAt(0) == '/';
			int nameStart = closing ? 1 : 0;
			while (nameStart < tag.length() && Character.isWhitespace(tag.charAt(nameStart))) {
				nameStart++;
			}
			int nameEnd = nameStart;
			while (nameEnd < tag.length() && isTagNameCharacter(tag.charAt(nameEnd))) {
				nameEnd++;
			}
			if (nameEnd == nameStart) {
				continue;
			}

			String name = tag.substring(nameStart, nameEnd).toLowerCase(Locale.ROOT);
			if (VOID_ELEMENTS.contains(name) || OPTIONAL_END_ELEMENTS.contains(name)) {
				continue;
			}

			if (closing) {
				if (openElements.isEmpty() || !openElements.pop().equals(name)) {
					return false;
				}
			} else if (!tag.endsWith("/")) {
				openElements.push(name);
			}
		}

		return openElements.isEmpty();
	}

	private int findTagEnd(String html, int start) {
		char quote = 0;
		for (int index = start; index < html.length(); index++) {
			char current = html.charAt(index);
			if (quote != 0) {
				if (current == quote) {
					quote = 0;
				}
			} else if (current == '\'' || current == '"') {
				quote = current;
			} else if (current == '>') {
				return index;
			}
		}
		return -1;
	}

	private boolean looksLikeTagStart(String html, int position) {
		if (position >= html.length()) {
			return false;
		}
		char first = html.charAt(position);
		return first == '/' || first == '!' || first == '?' || Character.isLetter(first);
	}

	private boolean isTagNameCharacter(char character) {
		return Character.isLetterOrDigit(character) || character == '-' || character == ':' || character == '_';
	}
}
