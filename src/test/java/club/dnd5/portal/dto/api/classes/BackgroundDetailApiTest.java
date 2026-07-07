package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.Language;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackgroundDetailApiTest {

	@Test
	void exposesLegacyLanguageText() {
		Background background = background();
		background.setLanguage("Один язык на выбор");

		BackgroundDetailApi result = new BackgroundDetailApi(background);

		assertThat(result.getLanguage()).isEqualTo("Один язык на выбор");
	}

	@Test
	void exposesStructuredLanguages() {
		Background background = background();
		Language common = new Language();
		common.setName("Общий");
		background.setLanguages(List.of(common));

		BackgroundDetailApi result = new BackgroundDetailApi(background);

		assertThat(result.getLanguages()).containsExactly("Общий");
	}

	private Background background() {
		Book book = new Book("TEST");
		book.setType(TypeBook.OFFICAL);

		Background background = new Background();
		background.setName("Тест");
		background.setEnglishName("Test");
		background.setBook(book);
		background.setSkills(List.of());
		return background;
	}
}
