package club.dnd5.portal.service;

import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.FoundryVersion;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.JsonStorageRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LssServiceImp implements LssService {
	private static final FoundryVersion STANDARD_FOUNDRY_VERSION = FoundryVersion.V11;
	private final SpellRepository spellRepository;
	private final JsonStorageRepository jsonStorageRepository;
	private final List<String> fieldsToRemove = Arrays.asList("_id", "img", "effects", "folder", "sort", "flags", "ownership", "_stats");

	@Override
	public String findById(Integer spellId) {
		JsonStorage jsonStorage = jsonStorageRepository
			.findByRefIdAndTypeJsonAndVersionFoundry(spellId, JsonType.SPELL, STANDARD_FOUNDRY_VERSION).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Storage was not Found"));
		return convertFromJsonStorageToSpellLSS(jsonStorage);
	}

	private String convertFromJsonStorageToSpellLSS(JsonStorage jsonStorage) {
		int spellId = jsonStorage.getRefId();
		Optional<Spell> optionalSpell = spellRepository.findById(spellId);
		String jsonData;
		try {
			jsonData = generateClassValueInJsonSpell(optionalSpell, jsonStorage.getJsonData());
		} catch (JsonProcessingException exception) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Problem with the Json Processing");
		}
		return jsonData;
	}

	private String generateClassValueInJsonSpell(Optional<Spell> optionalSpell, String jsonData) throws JsonProcessingException {
		List<String> classList = new ArrayList<>();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(jsonData);
		removeChildrenNodeWhichDontUsedForLssFormat(objectMapper, jsonNode);
		Map<String, Object> nodeMap = objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
		});

		if (optionalSpell.isPresent()) {
			Spell spell = optionalSpell.get();
			if (spell.getHeroClass().size() >= 1) {
				for (HeroClass heroClass : spell.getHeroClass()) {
					classList.add(heroClass.getEnglishName().toLowerCase());
				}
				nodeMap.put("classes", classList);
			}
		}
		jsonNode = objectMapper.convertValue(nodeMap, JsonNode.class);
		return objectMapper.writeValueAsString(jsonNode);
	}

	private void removeChildrenNodeWhichDontUsedForLssFormat(ObjectMapper objectMapper, JsonNode jsonNode) throws JsonProcessingException {
		ObjectNode object = (ObjectNode) jsonNode;
		fieldsToRemove.stream().forEach(object::remove);
		objectMapper.writeValueAsString(object);
	}
}
