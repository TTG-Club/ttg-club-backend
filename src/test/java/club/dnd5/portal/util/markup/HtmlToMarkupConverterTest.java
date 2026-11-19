package club.dnd5.portal.util.markup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlToMarkupConverterTest {

	private static List<String> convert(String html) {
		return HtmlToMarkupConverter.convert(html, new MarkupReport());
	}

	@Test
	void paragraphsAndInlineFormatting() {
		assertThat(convert("<p>Бонус <strong>+1</strong> и <em>бонус </em>к КД.</p><p>Второй</p>"))
			.containsExactly("Бонус {@b +1} и {@i бонус} к КД.", "Второй");
	}

	@Test
	void sectionLinksAndTooltips() {
		assertThat(convert("<p><detail-tooltip type=\"spell\"><a href=\"/spells/fireball\">огненный шар</a></detail-tooltip>, "
			+ "<a href=\"/items/magic/bag_of_holding\">сумка</a>, <a href=\"/screens/grapple\">захват</a>, "
			+ "<a href=\"https://ttg.club/bestiary/goblin\">гоблин</a>, <a href=\"https://example.com/x\">сайт</a></p>"))
			.containsExactly("{@spell огненный шар | url:fireball}, {@magicItem сумка | url:bag_of_holding}, "
				+ "{@link захват | url:/screens/grapple}, {@creature гоблин | url:goblin}, "
				+ "{@link сайт | url:https://example.com/x | target:_blank}");
	}

	@Test
	void selfClosingDiceRollerDoesNotSwallowText() {
		assertThat(convert("<p>4&nbsp;(<dice-roller formula=\"1к4 + 2\"/>) урона, "
			+ "<dice-roller label=\"Атака\" formula=\"к20 + 4\">+4</dice-roller> к попаданию</p>"))
			.containsExactly("4 ({@roll 1к4 + 2}) урона, {@roll к20 + 4 | text:+4} к попаданию");
	}

	@Test
	void headingListAndQuote() {
		assertThat(convert("<h4>Заголовок</h4><ul><li>один</li><li><p>два</p><ol><li>а</li></ol></li></ul>"
			+ "<blockquote><p>раз</p><p>два</p></blockquote><hr>"))
			.containsExactly("{@h Заголовок | level:2}",
				"{@list {@li один}{@li два}{@list {@li а} | type:ordered}}",
				"{@quote раз{@br}{@br}два}",
				"{@separator}");
	}

	@Test
	void tableWithHeaderAlignAndEmptyCell() {
		MarkupReport report = new MarkupReport();
		List<String> blocks = HtmlToMarkupConverter.convert("<div class=\"table-responsive\"><table class=\"dnd5_table\">"
			+ "<thead><tr><th style=\"width: 20px\">к6</th><th>Эффект</th></tr></thead>"
			+ "<tbody><tr><td style=\"text-align: center;\">1</td><td>a | b</td></tr>"
			+ "<tr><td colspan=\"2\"></td></tr></tbody></table></div>", report);
		assertThat(blocks).containsExactly("{@table {@tr {@th к6}{@th Эффект}}"
			+ "{@tr {@td 1 | align:center}{@td a / b}}"
			+ "{@tr {@td ​}{@td ​}}}");
		assertThat(report.getCounts()).containsKeys("table:colspan", "text:reserved-char-in-marker");
	}

	@Test
	void semanticSpansKeepTextAndAreReported() {
		MarkupReport report = new MarkupReport();
		assertThat(HtmlToMarkupConverter.convert("<p>спасбросок <span class=\"saving_throw\">Харизмы</span></p>", report))
			.containsExactly("спасбросок Харизмы");
		assertThat(report.getCounts()).containsEntry("span:saving_throw", 1);
	}

	@Test
	void centeredParagraphAndEmptyInput() {
		assertThat(convert("<p class=\"text-center mb-1\">по центру</p><p>&nbsp;</p>"))
			.containsExactly("{@p по центру | align:center}");
		assertThat(convert(null)).isEmpty();
		assertThat(convert("  ")).isEmpty();
	}
}
