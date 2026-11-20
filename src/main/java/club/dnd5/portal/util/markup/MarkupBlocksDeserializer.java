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
			return Markup.fromHtml(parser.getString());
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

	@Override
	public List<String> getNullValue(DeserializationContext context) {
		return null;
	}
}
