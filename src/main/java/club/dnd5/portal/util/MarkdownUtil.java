package club.dnd5.portal.util;

import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.CreatureFeat;
import lombok.Builder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Конвертер markdown в html
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
	 * Описание умения существа для ответа API: markdown переводится в html, если умение размечено.
	 * Сущность не меняется — иначе внутри транзакции (создание и правка в мастерской) html
	 * записался бы в базу вместо исходного markdown.
	 *
	 * @param feat  умение
	 * @param beast существо (для {@code {@beast_name}})
	 * @return описание для ответа
	 */
	public static String html(CreatureFeat feat, Creature beast) {
		if (!feat.isMarkdown() || feat.getDescription() == null) {
			return feat.getDescription();
		}
		String description = feat.getDescription();
		if (!java.util.Objects.equals(beast.getRaceId(), 102)) {
			description = description.replaceAll("\\.\\s\\{@beast_name\\}", ". " + beast.getName());
			description = description.replaceAll(">\\{@beast_name\\}", ">" + beast.getName());
			description = description.replaceAll("\\s\\{@beast_name\\}",
					" " + beast.getName().toLowerCase());
		} else {
			description = description.replace("{@beast_name}", beast.getName());
		}
		for (Markdown markdown: markdowns) {
			description = markdown.convert(description);
		}
		return description;
	}

	/**
	 * Описание действия существа для ответа API: markdown переводится в html, если действие размечено.
	 * Сущность не меняется (см. {@link #html(CreatureFeat, Creature)}).
	 *
	 * @param action действие
	 * @param beast  существо (для {@code {@beast_name}})
	 * @return описание для ответа
	 */
	public static String html(final Action action, final Creature beast) {
		if (!action.isMarkdown() || action.getDescription() == null) {
			return action.getDescription();
		}
		return toHtml(action.getDescription(), beast.getName());
	}

	/**
	 * Разметка действия или умения существа в html — без правки самой сущности.
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
