package club.dnd5.portal.util;

import club.dnd5.portal.util.markup.Markup;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Шаблоны действий и умений существ разворачиваются миграцией 0035 так же, как раньше их показывал
 * API, и дальше переводятся в разметку core-api с подписью броска.
 */
class MarkdownUtilTest {
	private static final String SOURCE = "<p>{@atk mw} {@hit 4} к попаданию. {@h} 5 ({@damage 1к6 + 2}) урона. {@beast_name} рычит.</p>";

	@Test
	void actionTemplateToHtml() {
		assertThat(MarkdownUtil.toHtml(SOURCE, "Гоблин"))
			.contains("<em>Рукопашная атака оружием:</em>")
			.contains("<dice-roller label=\"Атака\" formula=\"к20 + 4\">+4</dice-roller>")
			.contains("<dice-roller label=\"Урон\" formula=\"1к6 + 2\"/>")
			.contains("Гоблин рычит")
			.doesNotContain("{@");
	}

	@Test
	void featTemplateKeepsCaseRules() {
		// у существ, кроме НПС, имя внутри предложения — со строчной буквы, после точки — как есть
		assertThat(MarkdownUtil.featHtml("<p>Если {@beast_name} падает. {@beast_name} встаёт.</p>", "Гоблин", 0))
			.isEqualTo("<p>Если гоблин падает. Гоблин встаёт.</p>");
		assertThat(MarkdownUtil.featHtml("<p>Если {@beast_name} падает.</p>", "Ксандр", 102))
			.isEqualTo("<p>Если Ксандр падает.</p>");
	}

	@Test
	void expandedTemplateBecomesMarkupWithRollLabels() {
		assertThat(Markup.fromHtml(MarkdownUtil.toHtml(SOURCE, "Гоблин")))
			.containsExactly("{@i Рукопашная атака оружием:} {@roll к20 + 4 | text:+4 | label:Атака} к попаданию. "
				+ "{@i Попадание:} 5 ({@roll 1к6 + 2 | label:Урон}) урона. Гоблин рычит.");
	}
}
