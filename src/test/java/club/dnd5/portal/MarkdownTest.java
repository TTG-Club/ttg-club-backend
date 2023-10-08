package club.dnd5.portal;

import club.dnd5.portal.util.MarkdownUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarkdownTest {
	@Test
	void testDamageConvert() {
		String description = "цель получает {@damage 1к6+1} дробящего урона и {@damage 6к6} психического урона";
		MarkdownUtil.Markdown markdown = MarkdownUtil.Markdown.builder()
			.mark("\\{@damage\\s\\d*(к|d)\\d+(\\s?\\+\\s?\\d+)?\\}")
			.pattern("\\d*(к|d)\\d+(\\s?\\+\\s?\\d+)?")
			.template("<dice-roller formula=\"%s\"/>")
			.build();
		assertEquals("цель получает <dice-roller formula=\"1к6+1\"/> дробящего урона и <dice-roller formula=\"6к6\"/> психического урона",
			markdown.convert(description),
			"Markup damage error");
	}

	@Test
	void testAttack() {
		String description = "@{atk mw} @{hit 14} к попаданию, досягаемость 10 фт., одна цель";
		MarkdownUtil.Markdown markdown = MarkdownUtil.Markdown
			.builder().mark("@{atk mw}")
			.template("<em>Рукопашная атака оружием:</em>")
			.build();
		assertEquals("<em>Рукопашная атака оружием:</em> @{hit 14} к попаданию, досягаемость 10 фт., одна цель",
			markdown.convert(description),
			"Markdown attack error");
	}

	@Test
	void testHit() {
		String description = "<p>@{atk mw} @{hit 14} к попаданию, досягаемость 10 фт., одна цель.";
		MarkdownUtil.Markdown markdown = MarkdownUtil.Markdown.builder().mark("@\\{hit\\s\\d+}")
			.pattern("\\d+")
			.template("<dice-roller label=\"Бросок атаки\" formula=\"к20 + %1$s\">+%1$s</dice-roller>")
			.build();
		assertEquals("<p>@{atk mw} <dice-roller label=\"Бросок атаки\" formula=\"к20 + 14\">+14</dice-roller> к попаданию, досягаемость 10 фт., одна цель.",
			markdown.convert(description),
			"Markdown hit error");
	}
	@Test
	void testSpell() {
		String description = "@{spell shield} ";
		MarkdownUtil.Markdown markdown = MarkdownUtil.Markdown.builder().mark("\\{@spell\\s[Aаa-zZя_\\']+\\}")
			.pattern("[Aа-Zя_\']+")
			.template("<detail-tooltip type=\"spell\"><a href=\"/spells/%1$s\">щит [%1$s]</a></detail-tooltip>")
			.build();
		assertEquals("<detail-tooltip type=\"spell\"><a href=\"/spells/shield\">щит [shield]</a></detail-tooltip>",
			markdown.convert(description),
			"Markdown spell error");
	}
}
