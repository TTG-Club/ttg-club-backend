package club.dnd5.portal.util.markup;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Описание в теле формы мастерской: массив строк-блоков разметки core-api. Строка — это HTML старых
 * ревизий из аудита (снимки сохранены до перевода на разметку): она переводится в разметку, чтобы
 * восстановление старой ревизии продолжало работать.
 */
public class MarkupBlocksDeserializer extends ValueDeserializer<List<String>> {
	@Override
	public List<String> deserialize(JsonParser parser, DeserializationContext context) {
		if (parser.currentToken() == JsonToken.VALUE_STRING) {
			return Markup.fromHtml(linesToParagraphs(parser.getString()));
		}
		if (parser.currentToken() != JsonToken.START_ARRAY) {
			return context.reportInputMismatch(this, "Описание должно быть массивом блоков разметки");
		}
		List<String> blocks = new ArrayList<>();
		for (JsonToken token = parser.nextToken(); token != JsonToken.END_ARRAY; token = parser.nextToken()) {
			if (token != JsonToken.VALUE_STRING) {
				return context.reportInputMismatch(this, "Блок разметки должен быть строкой");
			}
			blocks.add(parser.getString());
		}
		return blocks;
	}

	/**
	 * Текст без тегов с переносами строк (описания богов хранились так, абзацы добавляло API — см.
	 * sourceFormat lines в changeset 0029) → абзацы {@code <p>}; иначе конвертер склеил бы строки.
	 */
	static String linesToParagraphs(String value) {
		if (value.indexOf('<') >= 0 || (value.indexOf('\n') < 0 && value.indexOf('\r') < 0)) {
			return value;
		}
		StringBuilder html = new StringBuilder();
		for (String line : value.split("\\r\\n|\\r|\\n")) {
			if (!line.isBlank()) {
				html.append("<p>").append(line.strip()).append("</p>");
			}
		}
		return html.toString();
	}

	@Override
	public List<String> getNullValue(DeserializationContext context) {
		return null;
	}
}
