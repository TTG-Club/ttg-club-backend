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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class JsonStorageServiceImpl implements JsonStorageService {

	private final JsonStorageRepository jsonStorageRepository;

	private final SpellRepository spellRepository;

	private final BestiaryRepository bestiaryRepository;

	private JsonStorage editJsonEntity(Integer id, JsonType jsonType, FoundryCommon entity) {
		JsonStorageCompositeKey compositeKey = new JsonStorageCompositeKey(id, jsonType);
		JsonStorage jsonStorage = jsonStorageRepository.findById(compositeKey).orElseThrow(PageNotFoundException::new);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(jsonStorage.getJsonData());
			modifyName(entity, rootNode);
			modifyBiography(entity, rootNode);
			jsonStorage.setJsonData(mapper.writeValueAsString(rootNode));
		} catch (IOException e) {
			e.printStackTrace();
			return jsonStorage;
		}
		return jsonStorage;
	}

	public JsonStorage editSpellJson(Integer id) {
		return editJsonEntity(id, JsonType.SPELL, spellRepository.findById(id).orElseThrow(PageNotFoundException::new));
	}

	public JsonStorage editCreatureJson(Integer id) {
		return editJsonEntity(id, JsonType.CREATURE, bestiaryRepository.findById(id).orElseThrow(PageNotFoundException::new));
	}

	private void modifyBiography(FoundryCommon entity, JsonNode rootNode) {
		String description = entity.getDescription();
		if (Objects.isNull(entity.getDescription()) || entity.getDescription().isEmpty()) {
			return;
		}
		ObjectNode systemNode = (ObjectNode) rootNode.get("system");
		ObjectNode detailsNode = systemNode.with("details");
		detailsNode.remove("biography");
		ObjectNode biographyNode = detailsNode.with("biography");
		biographyNode.put("value", description);
		biographyNode.put("public", "");
	}

	private void modifyName(FoundryCommon entity, JsonNode rootNode) {
		String name = entity.getName();
		String englishName = "["+entity.getEnglishName() + "]";
		if (Objects.isNull(name) || entity.getName().isEmpty()) {
			return;
		}
		((ObjectNode) rootNode).put("name", name + " " + englishName);
	}
}

