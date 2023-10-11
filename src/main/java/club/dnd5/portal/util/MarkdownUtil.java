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

		Markdown.builder().mark("\\{@damage\\s\\d+(к|d)\\d+(\\s?\\−\\s?\\d+)?\\}")
			.pattern("\\d+(к|d)\\d+(\\s?\\−\\s?\\d+)?")
			.template("<dice-roller label=\"Бросок урона\" formula=\"%1$s\"/>%1$s</dice-roller>")
			.build(),
		Markdown.builder().mark("\\{@damage\\s\\d+(к|d)\\d+(\\s?\\+\\s?\\d+)?\\}")
			.pattern("\\d+(к|d)\\d+(\\s?\\+\\s?\\d+)?")
			.template("<dice-roller label=\"Бросок урона\" formula=\"%s\"/>")
			.build(),
		Markdown.builder().mark("\\{@heal\\s\\d+(к|d)\\d+(\\s?\\+\\s?\\d+)?\\}")
			.pattern("\\d+(к|d)\\d+(\\s?\\+\\s?\\d+)?")
			.template("<dice-roller label=\"Бросок восстоновления хитов\" formula=\"%s\"/>")
			.build(),
		Markdown.builder().mark("\\{@dice\\s\\d+(к|d)\\d+(\\s?\\+\\s?\\d+)?\\}")
			.pattern("\\d+(к|d)\\d+(\\s?\\+\\s?\\d+)?")
			.template("<dice-roller formula=\"%s\"/>")
			.build(),
		Markdown.builder().mark("\\{@hit\\s\\d+}")
			.pattern("\\d+")
			.template("<dice-roller label=\"Бросок атаки\" formula=\"к20 + %1$s\">+%1$s</dice-roller>")
			.build()

	);

	/**
	 * Зменяет markdown на html в действиях существ
	 * @param feat действие
	 * @param beast существо
	 */
	public static void convert(CreatureFeat feat, Creature beast) {
		if (!feat.isMarkdown()) {
			return;
		}
		String description = feat.getDescription();
		description = description.replace("@{beast_name}", beast.getName());
		for (Markdown markdown: markdowns) {
			description = markdown.convert(description);
		}
		feat.setDescription(description);
	}

	/**
	 * Зменяет markdown на html в действиях существ
	 * @param action действие
	 * @param beast существо
	 */
	public static void convert(final Action action, final Creature beast) {
		if (!action.isMarkdown()) {
			return;
		}
		String description = action.getDescription();
		description = description.replace("{@beast_name}", beast.getName());
		for (Markdown markdown: markdowns) {
			description = markdown.convert(description);
 		}
		action.setDescription(description);
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
