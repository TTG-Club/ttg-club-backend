package club.dnd5.portal.controller.api;


import club.dnd5.portal.dto.fvtt.export.spell.Fspell;
import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.repository.JsonStorageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ParserController {

	private final JsonStorageRepository jsonStorageRepository;

	//		Integer counter = 0;
//		for (JsonNode jsonNode : request) {
//			JsonStorage jsonStorage = new JsonStorage();
//			jsonStorage.setJsonData(jsonNode.toString());
//			jsonStorage.setTypeJson(JsonType.CREATURE);
//			jsonStorage.setRefId(counter);
//			counter++;
//		}


	@PostMapping(value = "/api/v1/fspell")
	public void importSpells(@RequestBody List<JsonNode> request) {
		AtomicInteger counter = new AtomicInteger(0);
		jsonStorageRepository.saveAll(request.stream().map(jsonNode -> {
			JsonStorage jsonStorage = new JsonStorage();
			jsonStorage.setJsonData(jsonNode.toString());
			jsonStorage.setTypeJson(JsonType.SPELL);
			jsonStorage.setRefId(counter.getAndIncrement());
			return jsonStorage;
		}).collect(Collectors.toList()));
	}

	@PostMapping(value = "/api/v1/creature")
	public void importCreature(@RequestBody List<JsonNode> request) {
		AtomicInteger counter = new AtomicInteger(0);
		jsonStorageRepository.saveAll(request.stream().map(jsonNode -> {
			JsonStorage jsonStorage = new JsonStorage();
			jsonStorage.setJsonData(jsonNode.toString());
			jsonStorage.setTypeJson(JsonType.CREATURE);
			jsonStorage.setRefId(counter.getAndIncrement());
			return jsonStorage;
		}).collect(Collectors.toList()));
	}

	@GetMapping(value = "/api/v1/fspell")
	public List<JsonStorage> getAll () {
		return jsonStorageRepository.findAll();
	}
}
