package club.dnd5.portal.util;

import lombok.Builder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Шаблоны действий и умений существ (теги 5etools {@code {@atk mw}}, {@code {@hit 4}},
 * {@code {@beast_name}}…) в html. С этапа 3 шаблонов в базе нет: их один раз разворачивает
 * миграция 0035 ({@code CreatureMarkupChange}).
 *
 * <p>Используется только миграциями Liquibase, но не удалять: Liquibase выполняет customChange-классы
 * при каждой пересборке базы и при откате 0035.
 */
public class MarkdownUtil {
	private static final List<Markdown> markdowns = Arrays.asList(
		Markdown.builder().mark("{@atk mw}").template("<em>Рукопашная атака оружием:</em>").build(),
		Markdown.builder().mark("{@atk rw}").template("<em>Дальнобойная атака оружием:</em>").build(),
		Markdown.builder().mark("{@atk r,mw}").template("<em>Дальнобойная или рукопашная атака оружием:</em>").build(),
		Markdown.builder().mark("{@atk ms}").template("<em>Рукопашная атака заклинанием:</em>").build(),
		Markdown.builder().mark("{@atk rs}").template("<em>Дальнобойная атака заклинанием:</em>").build(),
		Markdown.builder().mark("{@atk r,ms}").template("<em>Дальнобойная или рукопашная атака заклинанием:</em>").build(),
		Markdown.builder().mark("{@h}").template("<em>Попадание:</em>").build(),

		Markdown.builder().mark("\\{@(b|bold)\\s[А-Яа-я0-9+\\-\\(\\)\\s]+}")
			.pattern("[А-Яа-я0-9+\\-\\(\\)\\s]+")
			.template("<strong>%s</strong>")
			.build(),
		Markdown.builder().mark("\\{@(i|italic)\\s[А-Яа-я0-9+\\-\\(\\)\\s]+}")
			.pattern("[А-Яа-я0-9+\\-\\(\\)\\s]+")
			.template("<em>%s</em>")
			.build(),

		Markdown.builder().mark("\\{@damage\\s\\d+(к|d)\\d+(\\s?(\\-|\\+|\\*)?\\s?\\d+)?}")
			.pattern("\\d+(к|d)\\d+(\\s?(\\-|\\+|\\*)?\\s?\\d+)?")
			.template("<dice-roller label=\"Урон\" formula=\"%s\"/>")
			.build(),
		Markdown.builder().mark("\\{@heal\\s\\d+(к|d)\\d+(\\s?\\+\\s?\\d+)?\\}")
			.pattern("\\d+(к|d)\\d+(\\s?\\+\\s?\\d+)?")
			.template("<dice-roller label=\"Восстоновления хитов\" formula=\"%s\"/>")
			.build(),
		Markdown.builder().mark("\\{@dice\\s[0-9]{0,}(к|d)[0-9]+(\\s?\\+\\s?\\d+)?\\}")
			.pattern("[0-9]{0,}(к|d)[0-9]+(\\s?\\+\\s?\\d+)?")
			.template("<dice-roller formula=\"%s\"/>")
			.build(),
		Markdown.builder().mark("\\{@hit\\s\\d+}")
			.pattern("\\d+")
			.template("<dice-roller label=\"Атака\" formula=\"к20 + %1$s\">+%1$s</dice-roller>")
			.build()
	);

	/**
	 * Шаблон умения существа в HTML — так его раньше показывал API (флаг markdown). Нужен только
	 * миграции 0035, которая один раз разворачивает шаблоны и переводит текст в разметку core-api.
	 * У существ, кроме НПС (раса 102), {@code {@beast_name}} в середине фразы пишется строчными.
	 *
	 * @param description текст из базы
	 * @param beastName   название существа для {@code {@beast_name}}
	 * @param raceId      раса существа (102 — НПС)
	 * @return html
	 */
	public static String featHtml(String description, String beastName, Integer raceId) {
		if (description == null) {
			return null;
		}
		String name = Objects.toString(beastName, "");
		String result = description;
		if (!Objects.equals(raceId, 102)) {
			result = result.replaceAll("\\.\\s\\{@beast_name\\}", Matcher.quoteReplacement(". " + name));
			result = result.replaceAll(">\\{@beast_name\\}", Matcher.quoteReplacement(">" + name));
			result = result.replaceAll("\\s\\{@beast_name\\}", Matcher.quoteReplacement(" " + name.toLowerCase()));
		} else {
			result = result.replace("{@beast_name}", name);
		}
		for (Markdown markdown : markdowns) {
			result = markdown.convert(result);
		}
		return result;
	}

	/**
	 * Шаблон действия существа в HTML (см. {@link #featHtml}): {@code {@beast_name}} — название как есть.
	 *
	 * @param description текст из базы
	 * @param beastName   название существа для {@code {@beast_name}}
	 * @return html
	 */
	public static String toHtml(String description, String beastName) {
		if (description == null) {
			return null;
		}
		String result = description.replace("{@beast_name}", Objects.toString(beastName, ""));
		for (Markdown markdown : markdowns) {
			result = markdown.convert(result);
		}
		return result;
	}

	@Builder
	public static class Markdown {
		private String mark;
		private String pattern;
		private String template;

		public String convert(String description) {
			if (Objects.isNull(pattern)) {
				return description.replace(mark, template);
			}
			Matcher matcherMark = Pattern.compile(mark).matcher(description);
			while (matcherMark.find()) {
				String group = matcherMark.group();
				Matcher matcher = Pattern.compile(pattern).matcher(group);
				if(matcher.find()) {
					String part = matcher.group();
					description = description.replace(group,
						String.format(template, part));
				}
			}
			return description;
		}
	}
}
