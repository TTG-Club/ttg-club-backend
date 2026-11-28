package club.dnd5.portal.model.classes;

import club.dnd5.portal.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StartingEquipmentLineTest {
	@Test
	void emptyOrNullBecomesNull() {
		assertThat(StartingEquipmentLine.normalize(null)).isNull();
		assertThat(StartingEquipmentLine.normalize(new ArrayList<>())).isNull();
	}

	@Test
	void normalizesItemsChoicesAndCoins() {
		StartingEquipmentItem longbow = item("item", " weapon ", " /weapons/longbow ", " Длинный лук ", null);
		longbow.setCategory("SIMPLE_WEAPON");
		StartingEquipmentItem choice = item("choice", "WEAPON", "/weapons/club", "Простое оружие", 2);
		choice.setCategory("simple_weapon");
		choice.setDescription("  ");
		StartingEquipmentItem own = item("ITEM", "ARMOR", " ", "Мешочек", 1);
		own.setDescription(" с травами ");
		StartingEquipmentOption first = option(0, longbow, choice);
		StartingEquipmentOption second = option(10, own);

		List<StartingEquipmentLine> result = StartingEquipmentLine.normalize(List.of(line(first, second)));

		assertThat(result).hasSize(1);
		List<StartingEquipmentOption> options = result.get(0).getOptions();
		StartingEquipmentItem bow = options.get(0).getItems().get(0);
		assertThat(bow.getKind()).isEqualTo("ITEM");
		assertThat(bow.getSection()).isEqualTo("WEAPON");
		assertThat(bow.getUrl()).isEqualTo("/weapons/longbow");
		assertThat(bow.getName()).isEqualTo("Длинный лук");
		assertThat(bow.getQuantity()).isEqualTo(1);
		assertThat(bow.getCategory()).isNull();
		StartingEquipmentItem chosen = options.get(0).getItems().get(1);
		assertThat(chosen.getKind()).isEqualTo("CHOICE");
		assertThat(chosen.getCategory()).isEqualTo("SIMPLE_WEAPON");
		assertThat(chosen.getSection()).isNull();
		assertThat(chosen.getUrl()).isNull();
		assertThat(chosen.getDescription()).isNull();
		assertThat(chosen.getQuantity()).isEqualTo(2);
		assertThat(options.get(0).getCoins()).isNull();
		StartingEquipmentItem pouch = options.get(1).getItems().get(0);
		assertThat(pouch.getSection()).isNull();
		assertThat(pouch.getUrl()).isNull();
		assertThat(pouch.getDescription()).isEqualTo("с травами");
		assertThat(options.get(1).getCoins()).isEqualTo(10);
	}

	@Test
	void coinsOnlyOptionIsAllowed() {
		List<StartingEquipmentLine> result = StartingEquipmentLine.normalize(List.of(line(option(25))));
		assertThat(result.get(0).getOptions().get(0).getItems()).isEmpty();
		assertThat(result.get(0).getOptions().get(0).getCoins()).isEqualTo(25);
	}

	@Test
	void rejectsLineWithoutOptions() {
		assertInvalid(List.of(new StartingEquipmentLine()), "Стартовое снаряжение, строка 1: нужен хотя бы один вариант");
	}

	@Test
	void rejectsEmptyOption() {
		assertInvalid(List.of(line(option(0))), "Стартовое снаряжение, строка 1, вариант 1: нужен хотя бы один предмет или монеты");
	}

	@Test
	void rejectsItemWithUrlButNoSection() {
		assertInvalid(List.of(line(option(null, item("ITEM", null, "/items/arrows", "Стрелы", 20)))),
			"Стартовое снаряжение, строка 1, вариант 1, предмет 1: у предмета со ссылкой нужен раздел (WEAPON, ARMOR или ITEM)");
	}

	@Test
	void rejectsUrlFromAnotherSection() {
		assertInvalid(List.of(line(option(null, item("ITEM", "ARMOR", "/weapons/longbow", "Лук", 1)))),
			"Стартовое снаряжение, строка 1, вариант 1, предмет 1: адрес раздела ARMOR должен начинаться с /armors/");
	}

	@Test
	void rejectsUnknownSectionKindAndCategory() {
		assertInvalidContains(item("ITEM", "SPELL", "/spells/light", "Свет", 1), "неизвестный раздел «SPELL»");
		assertInvalidContains(item("THING", null, null, "Нечто", 1), "вид должен быть ITEM или CHOICE");
		StartingEquipmentItem choice = item("CHOICE", null, null, "Что-то", 1);
		choice.setCategory("SHIELD");
		assertInvalidContains(choice, "неизвестная категория выбора «SHIELD»");
		assertInvalidContains(item("CHOICE", null, null, "Что-то", 1), "неизвестная категория выбора");
	}

	@Test
	void rejectsBadNameAndQuantity() {
		assertInvalidContains(item("ITEM", null, null, " ", 1), "нужно название");
		assertInvalidContains(item("ITEM", null, null, "x".repeat(201), 1), "название длиннее 200 символов");
		assertInvalidContains(item("ITEM", null, null, "Стрелы", 0), "количество должно быть от 1 до 1000");
		assertInvalidContains(item("ITEM", null, null, "Стрелы", 1001), "количество должно быть от 1 до 1000");
	}

	@Test
	void rejectsTooManyLinesOptionsAndItems() {
		List<StartingEquipmentLine> lines = new ArrayList<>();
		for (int i = 0; i < 21; i++) {
			lines.add(line(option(1)));
		}
		assertInvalid(lines, "Стартовое снаряжение: не больше 20 строк");

		StartingEquipmentOption[] options = new StartingEquipmentOption[6];
		for (int i = 0; i < options.length; i++) {
			options[i] = option(1);
		}
		assertInvalid(List.of(line(options)), "Стартовое снаряжение, строка 1: не больше 5 вариантов");

		StartingEquipmentItem[] items = new StartingEquipmentItem[21];
		for (int i = 0; i < items.length; i++) {
			items[i] = item("ITEM", null, null, "Предмет", 1);
		}
		assertInvalid(List.of(line(option(null, items))), "Стартовое снаряжение, строка 1, вариант 1: не больше 20 предметов");
	}

	@Test
	void rejectsNegativeCoins() {
		assertInvalid(List.of(line(option(-1))), "Стартовое снаряжение, строка 1, вариант 1: монет должно быть от 0 до 100000");
	}

	private static void assertInvalidContains(StartingEquipmentItem item, String message) {
		assertThatThrownBy(() -> StartingEquipmentLine.normalize(List.of(line(option(null, item)))))
			.isInstanceOf(ApiException.class)
			.hasMessageContaining(message)
			.satisfies(badRequest());
	}

	private static void assertInvalid(List<StartingEquipmentLine> lines, String message) {
		assertThatThrownBy(() -> StartingEquipmentLine.normalize(lines))
			.isInstanceOf(ApiException.class)
			.hasMessage(message)
			.satisfies(badRequest());
	}

	private static Consumer<Throwable> badRequest() {
		return error -> assertThat(((ApiException) error).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	static StartingEquipmentLine line(StartingEquipmentOption... options) {
		StartingEquipmentLine line = new StartingEquipmentLine();
		Collections.addAll(line.getOptions(), options);
		return line;
	}

	static StartingEquipmentOption option(Integer coins, StartingEquipmentItem... items) {
		StartingEquipmentOption option = new StartingEquipmentOption();
		option.setCoins(coins);
		Collections.addAll(option.getItems(), items);
		return option;
	}

	static StartingEquipmentItem item(String kind, String section, String url, String name, Integer quantity) {
		StartingEquipmentItem item = new StartingEquipmentItem();
		item.setKind(kind);
		item.setSection(section);
		item.setUrl(url);
		item.setName(name);
		item.setQuantity(quantity);
		return item;
	}
}
