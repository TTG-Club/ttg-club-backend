package club.dnd5.portal.controller.api;


import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.JsonStorageRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ParserController {

	private final JsonStorageRepository jsonStorageRepository;

	private final BestiaryRepository bestiaryRepository;

	private final SpellRepository spellRepository;

	@PostMapping(value = "/api/v1/fspell")
	public void importSpells(@RequestBody List<JsonNode> request) {
		jsonStorageRepository.saveAll(request.stream().map(jsonNode -> {
			JsonStorage jsonStorage = new JsonStorage();
			jsonStorage.setJsonData(jsonNode.toString());
			jsonStorage.setTypeJson(JsonType.SPELL);
			String name = jsonNode.get("name").asText();
			if (name.contains("/")) {
				name = name.split("/")[1].trim().replaceAll("-", " ").trim();
			}
			else {
				return null;
			}
			System.out.println("Name - " + name);
			Optional<Spell> spellOptional = spellRepository.findByEnglishName(name);
			if (spellOptional.isPresent()) {
				Integer idSpell = spellOptional.get().getId();
				jsonStorage.setName(name);
				jsonStorage.setRefId(idSpell);
				return jsonStorage;
			} else {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList()));
	}

	@PostMapping(value = "/api/v1/fcreature")
	public void importCreature(@RequestBody List<JsonNode> request) {
		jsonStorageRepository.saveAll(request.stream().map(jsonNode -> {
			JsonStorage jsonStorage = new JsonStorage();
			jsonStorage.setJsonData(jsonNode.toString());
			jsonStorage.setTypeJson(JsonType.CREATURE);
			String name = jsonNode.get("name").asText();
			if (name.contains("/")) {
				name = name.split("/")[1].trim().replaceAll("-", " ").trim();
			}
			else {
				return null;
			}
			System.out.println("Name - " + name);
			Optional<Creature> creatureOptional = bestiaryRepository.findByEnglishName(name);
			if (creatureOptional.isPresent()) {
				Integer idSpell = creatureOptional.get().getId();
				jsonStorage.setName(name); // take english parts.
				jsonStorage.setRefId(idSpell);
				return jsonStorage;
			} else {
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList()));
	}

	@GetMapping(value = "/api/v1/fspell")
	public List<JsonStorage> getAll () {
		return jsonStorageRepository.findAll();
	}
}
