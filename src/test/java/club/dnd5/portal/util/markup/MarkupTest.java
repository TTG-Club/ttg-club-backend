package club.dnd5.portal.util.markup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Короткие поля (владения класса, снаряжение предыстории) в строку и списком. */
class MarkupTest {
	@Test
	void inlineHtmlWithoutParagraphWrapper() {
		assertThat(Markup.toInlineHtml(List.of("{@item Набор травника | url:herbalism_kit}", "и {@b ещё}")))
			.isEqualTo("<detail-tooltip type=\"item\"><a href=\"/items/herbalism_kit\">Набор травника</a></detail-tooltip> "
				+ "и <strong>ещё</strong>");
		assertThat(Markup.toInlineHtml(null)).isEmpty();
	}

	@Test
	void splitsListOutsideMarkers() {
		assertThat(Markup.splitInline(List.of("{@item набор для грима | url:disguise_kit}, "
				+ "{@link отличная одежда, шляпа | url:/items/fine_clothes}, кошель с 15 зм."), ", "))
			.containsExactly("{@item набор для грима | url:disguise_kit}",
				"{@link отличная одежда, шляпа | url:/items/fine_clothes}", "кошель с 15 зм.");
		assertThat(Markup.splitInline(null, ", ")).isEmpty();
	}
}
