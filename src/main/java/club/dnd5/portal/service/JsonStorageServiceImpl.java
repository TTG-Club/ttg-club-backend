package club.dnd5.portal.service;


import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.SpellRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class JsonStorageServiceImpl implements JsonStorageService {


	public String editSpellJson(Spell spell) {
		String jsonText = spell.getSpellJson().getJsonData();
		jsonText = modifyDescription(spell, jsonText);
		return jsonText;
	}

	public String editCreatureJson(Creature creature) {
		String jsonText = creature.getCreatureJson().getJsonData();
		jsonText = modifyDescription(creature, jsonText);
		return jsonText;
	}

	private String modifyDescription(Object entity, String jsonText) {
		String description = (entity instanceof Spell) ? ((Spell) entity).getDescription() : ((Creature) entity).getDescription();

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

