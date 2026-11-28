package club.dnd5.portal.model.classes;

import club.dnd5.portal.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Строка стартового снаряжения класса — элемент jsonb-списка {@code classes.starting_equipment}
 * (db/changelog 0054). Один вариант — снаряжение без выбора, два и больше — «а) … или б) …».
 * Текст {@code equipment} остаётся для чтения и как запасной вариант, если структура не заполнена.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class StartingEquipmentLine {
	public static final int MAX_LINES = 20;
	public static final int MAX_OPTIONS = 5;
	public static final int MAX_ITEMS = 20;
	public static final int MAX_QUANTITY = 1000;
	public static final int MAX_COINS = 100_000;
	public static final int MAX_NAME_LENGTH = 200;
	public static final int MAX_DESCRIPTION_LENGTH = 500;
	public static final int MAX_URL_LENGTH = 255;

	/** Разделы справочника и префиксы их адресов на сайте. */
	private static final Map<String, String> SECTION_URL_PREFIXES = Map.of(
		"WEAPON", "/weapons/",
		"ARMOR", "/armors/",
		"ITEM", "/items/");

	/** Категории выбора у позиции CHOICE. */
	public static final Set<String> CATEGORIES = Set.of(
		"SIMPLE_WEAPON", "MARTIAL_WEAPON",
		"SIMPLE_MELEE_WEAPON", "MARTIAL_MELEE_WEAPON",
		"SIMPLE_RANGED_WEAPON", "MARTIAL_RANGED_WEAPON",
		"ARMOR_LIGHT", "ARMOR_MEDIUM", "ARMOR_HEAVY",
		"INSTRUMENT", "ARTISAN_TOOLS");

	/** Варианты строки. */
	private List<StartingEquipmentOption> options = new ArrayList<>();

	/**
	 * Проверяет стартовое снаряжение из запроса мастерской и приводит его к хранимому виду:
	 * строки обрезаются, пустые необязательные поля становятся {@code null}, количество по
	 * умолчанию — 1, коды разделов и категорий — в верхнем регистре, поля, не относящиеся к виду
	 * позиции, очищаются.
	 *
	 * @param lines снаряжение из запроса; {@code null} не допускается (его обрабатывает вызывающий)
	 * @return снаряжение для сохранения; {@code null}, если строк нет
	 * @throws ApiException 400 с описанием первой ошибки
	 */
	public static List<StartingEquipmentLine> normalize(List<StartingEquipmentLine> lines) {
		if (lines == null || lines.isEmpty()) {
			return null;
		}
		if (lines.size() > MAX_LINES) {
			throw invalid("Стартовое снаряжение: не больше " + MAX_LINES + " строк");
		}
		List<StartingEquipmentLine> result = new ArrayList<>(lines.size());
		for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
			String lineLabel = "Стартовое снаряжение, строка " + (lineIndex + 1);
			StartingEquipmentLine line = lines.get(lineIndex);
			if (line == null || line.getOptions() == null || line.getOptions().isEmpty()) {
				throw invalid(lineLabel + ": нужен хотя бы один вариант");
			}
			if (line.getOptions().size() > MAX_OPTIONS) {
				throw invalid(lineLabel + ": не больше " + MAX_OPTIONS + " вариантов");
			}
			StartingEquipmentLine normalizedLine = new StartingEquipmentLine();
			for (int optionIndex = 0; optionIndex < line.getOptions().size(); optionIndex++) {
				String optionLabel = lineLabel + ", вариант " + (optionIndex + 1);
				normalizedLine.getOptions().add(normalizeOption(line.getOptions().get(optionIndex), optionLabel));
			}
			result.add(normalizedLine);
		}
		return result;
	}

	private static StartingEquipmentOption normalizeOption(StartingEquipmentOption option, String label) {
		if (option == null) {
			throw invalid(label + ": нужен хотя бы один предмет или монеты");
		}
		List<StartingEquipmentItem> items = option.getItems() == null ? List.of() : option.getItems();
		if (items.size() > MAX_ITEMS) {
			throw invalid(label + ": не больше " + MAX_ITEMS + " предметов");
		}
		Integer coins = option.getCoins();
		if (coins != null && (coins < 0 || coins > MAX_COINS)) {
			throw invalid(label + ": монет должно быть от 0 до " + MAX_COINS);
		}
		if (coins != null && coins == 0) {
			coins = null;
		}
		if (items.isEmpty() && coins == null) {
			throw invalid(label + ": нужен хотя бы один предмет или монеты");
		}
		StartingEquipmentOption normalized = new StartingEquipmentOption();
		normalized.setCoins(coins);
		for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
			normalized.getItems().add(normalizeItem(items.get(itemIndex), label + ", предмет " + (itemIndex + 1)));
		}
		return normalized;
	}

	private static StartingEquipmentItem normalizeItem(StartingEquipmentItem item, String label) {
		if (item == null) {
			throw invalid(label + ": пустой предмет");
		}
		String kind = upperOrNull(item.getKind());
		if (!StartingEquipmentItem.KIND_ITEM.equals(kind) && !StartingEquipmentItem.KIND_CHOICE.equals(kind)) {
			throw invalid(label + ": вид должен быть ITEM или CHOICE");
		}
		String name = trimToNull(item.getName());
		if (name == null) {
			throw invalid(label + ": нужно название");
		}
		if (name.length() > MAX_NAME_LENGTH) {
			throw invalid(label + ": название длиннее " + MAX_NAME_LENGTH + " символов");
		}
		int quantity = item.getQuantity() == null ? 1 : item.getQuantity();
		if (quantity < 1 || quantity > MAX_QUANTITY) {
			throw invalid(label + ": количество должно быть от 1 до " + MAX_QUANTITY);
		}
		String description = trimToNull(item.getDescription());
		if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
			throw invalid(label + ": уточнение длиннее " + MAX_DESCRIPTION_LENGTH + " символов");
		}

		StartingEquipmentItem normalized = new StartingEquipmentItem();
		normalized.setKind(kind);
		normalized.setName(name);
		normalized.setQuantity(quantity);
		normalized.setDescription(description);
		if (StartingEquipmentItem.KIND_CHOICE.equals(kind)) {
			String category = upperOrNull(item.getCategory());
			if (category == null || !CATEGORIES.contains(category)) {
				throw invalid(label + ": неизвестная категория выбора «" + item.getCategory() + "»");
			}
			normalized.setCategory(category);
			return normalized;
		}
		String url = trimToNull(item.getUrl());
		if (url == null) {
			// свой предмет — без ссылки на справочник
			return normalized;
		}
		String section = upperOrNull(item.getSection());
		if (section == null) {
			throw invalid(label + ": у предмета со ссылкой нужен раздел (WEAPON, ARMOR или ITEM)");
		}
		String prefix = SECTION_URL_PREFIXES.get(section);
		if (prefix == null) {
			throw invalid(label + ": неизвестный раздел «" + item.getSection() + "», нужен WEAPON, ARMOR или ITEM");
		}
		if (url.length() > MAX_URL_LENGTH) {
			throw invalid(label + ": адрес длиннее " + MAX_URL_LENGTH + " символов");
		}
		if (!url.startsWith(prefix) || url.length() == prefix.length() || url.chars().anyMatch(Character::isWhitespace)) {
			throw invalid(label + ": адрес раздела " + section + " должен начинаться с " + prefix);
		}
		normalized.setSection(section);
		normalized.setUrl(url);
		return normalized;
	}

	private static ApiException invalid(String message) {
		return new ApiException(HttpStatus.BAD_REQUEST, message);
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private static String upperOrNull(String value) {
		String trimmed = trimToNull(value);
		return trimmed == null ? null : trimmed.toUpperCase(Locale.ROOT);
	}
}
