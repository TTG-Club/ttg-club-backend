package club.dnd5.portal.service;


import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.FoundryCommon;
import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.repository.JsonStorageRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JsonStorageServiceImpl implements JsonStorageService {

	private final JsonStorageRepository jsonStorageRepository;

	private final SpellRepository spellRepository;

	private final BestiaryRepository bestiaryRepository;

	private JsonStorage editJsonEntity(Integer id, JsonType jsonType, FoundryCommon entity) {
		JsonStorageCompositeKey compositeKey = new JsonStorageCompositeKey(id, jsonType);
		JsonStorage jsonStorage = jsonStorageRepository.findById(compositeKey).orElseThrow(PageNotFoundException::new);
		String jsonText = modifyDescription(entity, jsonStorage.getJsonData());
		jsonStorage.setJsonData(jsonText);
		return jsonStorage;
	}

	public JsonStorage editSpellJson(Integer id) {
		return editJsonEntity(id, JsonType.SPELL, spellRepository.findById(id).orElseThrow(PageNotFoundException::new));
	}

	public JsonStorage editCreatureJson(Integer id) {
		return editJsonEntity(id, JsonType.CREATURE, bestiaryRepository.findById(id).orElseThrow(PageNotFoundException::new));
	}

	private String modifyDescription(FoundryCommon entity, String jsonText) {
		String description = entity.getDescription();
		if (Objects.isNull(entity.getDescription()) || entity.getDescription().isEmpty()) {
			return jsonText;
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(jsonText);
			((ObjectNode) rootNode.path("system").path("description"))
				.put("value", description);
			String modifiedJson = mapper.writeValueAsString(rootNode);
			return modifiedJson;
		} catch (IOException e) {
			e.printStackTrace();
			return jsonText;
		}
	}
}

