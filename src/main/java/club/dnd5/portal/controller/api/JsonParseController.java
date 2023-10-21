package club.dnd5.portal.controller.api;

import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.JsonStorageRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.repository.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequiredArgsConstructor
public class JsonParseController {

	private final Set<String> ROLES = new HashSet<>(Arrays.asList("ADMIN"));

	private final JsonStorageRepository jsonStorageRepository;

	private final BestiaryRepository bestiaryRepository;

	private final UserRepository userRepository;

	private final SpellRepository spellRepository;

	@PostMapping(value = "/api/v1/fspell")
	public void importSpells(@RequestBody List<JsonNode> request, @RequestParam Integer versionFoundry) {
		checkUserPermissions();
		jsonStorageRepository.saveAll(
			request.stream()
				.map(jsonNode -> processJsonNode(jsonNode, JsonType.SPELL, versionFoundry))
				.filter(Objects::nonNull)
				.collect(Collectors.toList())
		);
	}

	@PostMapping(value = "/api/v1/fcreature")
	public void importCreature(@RequestBody List<JsonNode> request, @RequestParam Integer versionFoundry) {
		checkUserPermissions();
		if (versionFoundry == 10) {
			JsonNode nodeCreature = request.get(0).get("monster");
			List<JsonStorage> processedDataList = StreamSupport.stream(nodeCreature.spliterator(), false)
				.map(jsonNode -> processJsonNode(jsonNode, JsonType.CREATURE, versionFoundry))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

			jsonStorageRepository.saveAll(processedDataList);
		} else {
			jsonStorageRepository.saveAll(
				request.stream()
					.map(jsonNode -> processJsonNode(jsonNode, JsonType.CREATURE, 11))
					.filter(Objects::nonNull)
					.collect(Collectors.toList())
			);
		}
	}

	private JsonStorage processJsonNode(JsonNode jsonNode, JsonType jsonType, Integer versionFoundry) {
		JsonStorage jsonStorage = new JsonStorage();
		jsonStorage.setJsonData(jsonNode.toString());
		jsonStorage.setTypeJson(jsonType);
		String name = jsonNode.get("name").asText();
		if (versionFoundry == 10) {
			if (name.contains("(")) {
				name = name
					.split("\\(")[1]
					.replaceAll("\\(", "")
					.replaceAll("\\)", "")
					.trim();
			} else {
				return null;
			}
		} else {
			if (name.contains("/")) {
				name = name.split("/")[1].trim().replaceAll("-", " ").trim();
			} else {
				return null;
			}
		}
		Optional<?> entityOptional = null;
		Integer id = null;
		if (jsonType == JsonType.SPELL) {
			entityOptional = spellRepository.findByEnglishName(name);
		} else if (jsonType == JsonType.CREATURE) {
			entityOptional = bestiaryRepository.findByEnglishName(name);
		}
		if (entityOptional.isPresent()) {
			if (jsonType == JsonType.SPELL) {
				id = ((Spell) entityOptional.get()).getId();
			} else if (jsonType == JsonType.CREATURE) {
				id = ((Creature) entityOptional.get()).getId();
			}
		} else {
			return null;
		}
		jsonStorage.setName(name);
		jsonStorage.setRefId(id);
		jsonStorage.setVersionFoundry(versionFoundry);
		return jsonStorage;
	}

	@GetMapping(value = "/api/v1/fspell")
	public List<JsonStorage> getAll() {
		return jsonStorageRepository.findAll();
	}

	private void checkUserPermissions() {
		User user = getCurrentUser();
		if (user.getRoles().stream().map(Role::getName).noneMatch(ROLES::contains)) {
			throw new RuntimeException("You don't have permission!");
		}
	}

	private User getCurrentUser() {
		SecurityContext context = SecurityContextHolder.getContext();
		String userName = context.getAuthentication().getName();
		return userRepository.findByEmailOrUsername(userName, userName)
			.orElseThrow(() -> new UsernameNotFoundException(userName));
	}
}
