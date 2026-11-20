package club.dnd5.portal.util.markup;

import club.dnd5.portal.dto.api.spell.SpellSaveApi;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarkupBlocksDeserializerTest {
	private final ObjectMapper mapper = JsonMapper.builder().build();

	@Test
	void readsMarkupBlocks() {
		SpellSaveApi api = mapper.readValue(
			"{\"description\":[\"Первый {@b блок}\",\"{@roll 2к6}\"],\"upper\":[]}", SpellSaveApi.class);

		assertEquals(List.of("Первый {@b блок}", "{@roll 2к6}"), api.getDescription());
		assertEquals(List.of(), api.getUpper());
	}

	@Test
	void convertsHtmlOfOldRevisions() {
		SpellSaveApi api = mapper.readValue(
			"{\"description\":\"<p>Урон <dice-roller formula=\\\"2к6\\\">2к6</dice-roller></p><p>Второй</p>\",\"upper\":null}",
			SpellSaveApi.class);

		assertEquals(List.of("Урон {@roll 2к6}", "Второй"), api.getDescription());
		assertNull(api.getUpper());
	}

	@Test
	void rejectsOtherValues() {
		assertThrows(MismatchedInputException.class,
			() -> mapper.readValue("{\"description\":[1]}", SpellSaveApi.class));
		assertThrows(MismatchedInputException.class,
			() -> mapper.readValue("{\"description\":{}}", SpellSaveApi.class));
	}

	@Test
	void trimsBlocks() {
		assertEquals(List.of("а", "б"), Markup.trimBlocks(List.of(" а ", "  ", "б")));
		assertNull(Markup.trimBlocks(null));
	}
}
