package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.background.Personalization;
import club.dnd5.portal.model.background.PersonalizationType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackgroundPersonalizationTableApiTest {

	@Test
	void mapsPersonalizationsToRollTable() {
		Personalization first = personalization(PersonalizationType.TRAIT, "Первый результат");
		Personalization second = personalization(PersonalizationType.TRAIT, "Второй результат");

		BackgroundPersonalizationTableApi table = new BackgroundPersonalizationTableApi(
			PersonalizationType.TRAIT,
			Arrays.asList(first, second)
		);

		assertEquals(PersonalizationType.TRAIT.getName(), table.getName());
		assertEquals("к2", table.getFormula());
		assertEquals(PersonalizationType.TRAIT.getName(), table.getThead()[0]);
		ArrayList<Collection<String>> rows = new ArrayList<>(table.getTbody());
		assertEquals(Arrays.asList("1", "Первый результат"), rows.get(0));
		assertEquals(Arrays.asList("2", "Второй результат"), rows.get(1));
	}

	@Test
	void addsGroupedTablesToBackgroundDetails() {
		Background background = new Background();
		background.setId(1);
		background.setName("Тестовая предыстория");
		background.setEnglishName("Test Background");
		background.setSkills(Collections.emptyList());
		background.setPersonalizations(Arrays.asList(
			personalization(PersonalizationType.TRAIT, "Первая черта"),
			personalization(PersonalizationType.TRAIT, "Вторая черта"),
			personalization(PersonalizationType.IDEAL, "Первый идеал")
		));
		Book book = new Book("TEST");
		book.setName("Test Book");
		book.setType(TypeBook.OFFICAL);
		background.setBook(book);

		BackgroundDetailApi details = new BackgroundDetailApi(background);

		ArrayList<BackgroundPersonalizationTableApi> tables = new ArrayList<>(details.getPersonalizationTables());
		assertEquals(2, tables.size());
		assertEquals(PersonalizationType.TRAIT.getName(), tables.get(0).getName());
		assertEquals("к2", tables.get(0).getFormula());
		assertEquals(PersonalizationType.IDEAL.getName(), tables.get(1).getName());
		assertEquals("к1", tables.get(1).getFormula());
	}

	private Personalization personalization(PersonalizationType type, String text) {
		Personalization personalization = new Personalization();
		personalization.setType(type);
		personalization.setText(text);
		return personalization;
	}
}
