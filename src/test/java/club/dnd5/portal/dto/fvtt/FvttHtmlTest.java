package club.dnd5.portal.dto.fvtt;

import club.dnd5.portal.util.markup.Markup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FvttHtmlTest {
	@Test
	void rollsFromRendererBecomeVttRolls() {
		String html = Markup.toHtml(List.of(
			"Урон {@roll 2к6 + 3} и {@roll к20 + 4 | text:+4 | label:Атака}, {@roll 1К8}"));

		assertThat(FvttHtml.rolls(html, formula -> "[[/r " + formula + "]]"))
			.isEqualTo("<p>Урон [[/r 2d6 + 3]] и [[/r d20 + 4]], [[/r 1d8]]</p>");
	}

	@Test
	void selfClosingRollOfOldHtml() {
		assertThat(FvttHtml.rolls("7 (<dice-roller formula=\"2к6\"/>) урона", formula -> "{@damage " + formula + "}"))
			.isEqualTo("7 ({@damage 2d6}) урона");
	}

	@Test
	void internalLinksBecomeAbsolute() {
		String html = Markup.toHtml(List.of(
			"{@spell шар | url:fireball}, {@link сайт | url:https://example.com}, {@link якорь | url:#a}"));

		assertThat(FvttHtml.absoluteLinks(html))
			.contains("href=\"https://ttg.club/spells/fireball\"")
			.contains("href=\"https://example.com\"")
			.contains("href=\"#a\"");
	}
}
