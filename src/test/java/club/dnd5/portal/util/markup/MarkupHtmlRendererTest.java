package club.dnd5.portal.util.markup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkupHtmlRendererTest {

	@Test
	void paragraphsInlineAndOldFrontendComponents() {
		assertThat(MarkupHtmlRenderer.render(List.of(
			"Урон {@roll 8к6} и {@b жирный {@i курсив}}.",
			"См. {@spell огненный шар | url:fireball}, {@link захват | url:/screens/grapple}, "
				+ "{@class волшебник | url:wizard}, {@link сайт | url:https://example.com | target:_blank}{@br}конец")))
			.isEqualTo("<p>Урон <dice-roller formula=\"8к6\">8к6</dice-roller> и <strong>жирный <em>курсив</em></strong>.</p>"
				+ "<p>См. <detail-tooltip type=\"spell\"><a href=\"/spells/fireball\">огненный шар</a></detail-tooltip>, "
				+ "<detail-tooltip type=\"screen\"><a href=\"/screens/grapple\">захват</a></detail-tooltip>, "
				+ "<a href=\"/classes/wizard\">волшебник</a>, "
				+ "<a href=\"https://example.com\" target=\"_blank\" rel=\"noopener noreferrer\">сайт</a><br>конец</p>");
	}

	@Test
	void rollWithLabel() {
		assertThat(MarkupHtmlRenderer.render(List.of("{@roll к20 + 4 | text:+4 | label:Атака}")))
			.isEqualTo("<p><dice-roller label=\"Атака\" formula=\"к20 + 4\">+4</dice-roller></p>");
	}

	@Test
	void samePageAnchorWithoutNewTab() {
		assertThat(MarkupHtmlRenderer.render(List.of("См. {@link Вдохновение барда | url:#c1}.")))
			.isEqualTo("<p>См. <a href=\"#c1\">Вдохновение барда</a>.</p>");
	}

	@Test
	void rollWithLabelAndEscaping() {
		assertThat(MarkupHtmlRenderer.render(List.of("{@roll к20 + 4 | text:+4} <script>&")))
			.isEqualTo("<p><dice-roller formula=\"к20 + 4\">+4</dice-roller> &lt;script&gt;&amp;</p>");
	}

	@Test
	void blocks() {
		assertThat(MarkupHtmlRenderer.render(List.of(
			"{@h Заголовок | level:2}",
			"{@p по центру | align:center}",
			"{@list {@li один}{@li два}{@list {@li а} | type:ordered}}",
			"{@quote раз{@br}{@br}два}",
			"{@separator}",
			"{@table {@tr {@th к6}{@th Эффект}}{@tr {@td 1 | align:center}{@td ​}}}")))
			.isEqualTo("<h4>Заголовок</h4>"
				+ "<p style=\"text-align: center;\">по центру</p>"
				+ "<ul><li>один</li><li>два<ol><li>а</li></ol></li></ul>"
				+ "<blockquote><p>раз</p><p>два</p></blockquote>"
				+ "<hr>"
				+ "<div class=\"table-responsive\"><table class=\"dnd5_table\"><thead><tr><th>к6</th><th>Эффект</th></tr></thead>"
				+ "<tbody><tr><td style=\"text-align: center;\">1</td><td></td></tr></tbody></table></div>");
	}

	@Test
	void unknownAndBrokenMarkersKeepText() {
		assertThat(MarkupHtmlRenderer.render(List.of("{@unknown текст} и {@b незакрытый")))
			.isEqualTo("<p>текст и {@b незакрытый</p>");
		assertThat(MarkupHtmlRenderer.render(List.of())).isNull();
		assertThat(MarkupHtmlRenderer.render(null)).isNull();
	}

	@Test
	void roundTripThroughConverterIsStable() {
		String html = "<p>4&nbsp;(<dice-roller formula=\"1к4 + 2\"/>) урона, спасбросок <span class=\"saving_throw\">Ловкости</span>. "
			+ "<detail-tooltip type=\"creature\"><a href=\"/bestiary/goblin\">Гоблин</a></detail-tooltip></p>"
			+ "<h5>Таблица</h5><table><tr><th>к4</th><th>Итог</th></tr><tr><td>1</td><td><em>да</em></td></tr></table>";
		List<String> blocks = HtmlToMarkupConverter.convert(html, new MarkupReport());
		assertThat(HtmlToMarkupConverter.convert(MarkupHtmlRenderer.render(blocks), new MarkupReport())).isEqualTo(blocks);
	}
}
