package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.spells.SpellFvtt;
import club.dnd5.portal.dto.api.spells.SpellsFvtt;
import club.dnd5.portal.dto.fvtt.export.FBeastiary;
import club.dnd5.portal.dto.fvtt.plutonium.FBeast;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.JsonStorageRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.service.JsonStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Foundry", description = "API для экспорта в Foundry")
@RestController
public class FvttApiController {

	private final JsonStorageService jsonStorageService;

	private final JsonStorageRepository jsonStorageRepository;
	private final BestiaryRepository bestiaryRepository;

	private SpellRepository spellRepository;

	@Operation(summary = "Загрузка заклинания в json в формате FVTT")
	@GetMapping(value = "/api/fvtt/spell", produces = "application/json")
	public ResponseEntity<byte[]> getSpellsFvtt(
		@RequestParam(required = false) Integer id,
		@RequestParam(required = false, defaultValue = "11") Integer versionFoundry
	) {
		if (id != null) {
			JsonStorage jsonStorage = jsonStorageService.editSpellJson(id, versionFoundry);
			HttpHeaders responseHeaders = new HttpHeaders();
			String file = String.format("attachment; filename=\"%s.json\"", jsonStorage.getName());
			responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, file);
			responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			byte[] jsonDataBytes = jsonStorage.getJsonData().getBytes(StandardCharsets.UTF_8);
			return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(jsonDataBytes);
		} else {
			List<JsonStorage> jsonStorageList = jsonStorageRepository
				.findAllByTypeJsonAndVersionFoundry(JsonType.SPELL, versionFoundry);
			byte[] jsonDataBytes = convertListToJson(jsonStorageList);

			HttpHeaders responseHeaders = new HttpHeaders();
			String file = String.format("attachment; filename=\"%s.json\"", "spells");
			responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, file);
			responseHeaders.setContentType(MediaType.APPLICATION_JSON);
			return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(jsonDataBytes);
		}
	}

	@Operation(summary = "Загрузка существа в json в формате FVTT")
	@GetMapping(value = "/api/fvtt/bestiary", produces = "application/json")
	public ResponseEntity<byte[]> getCreatureFvtt(
		@RequestParam(required = false) Integer id,
		@RequestParam(required = false, defaultValue = "11") Integer versionFoundry
	) {
		if (id != null) {
			JsonStorage jsonStorage = jsonStorageService.editCreatureJson(id, versionFoundry);
			HttpHeaders responseHeaders = createResponseHeaders(jsonStorage.getName());
			byte[] jsonDataBytes = jsonStorage.getJsonData().getBytes(StandardCharsets.UTF_8);
			return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(jsonDataBytes);
		} else {
			HttpHeaders responseHeaders = createResponseHeaders("creature_list");
			List<JsonStorage> bestiaryList = jsonStorageService.getAllJson(JsonType.CREATURE, versionFoundry);
			String jsonData = null;
			try {
				jsonData = new ObjectMapper().writeValueAsString(bestiaryList);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			byte[] jsonDataBytes = jsonData.getBytes(StandardCharsets.UTF_8);
			return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(jsonDataBytes);
		}
	}


	@Operation(summary = "Список заклинаний в json в формате FVTT")
	@CrossOrigin
	@GetMapping(value = "/api/fvtt/v1/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public SpellsFvtt getSpells(String search, String exact) {
		Specification<Spell> specification = null;
		if (search != null) {
			if (exact != null) {
				specification = (root, query, cb) -> cb.equal(root.get("name"), search.trim().toUpperCase());
			} else {
				String likeSearch = "%" + search + "%";
				specification = (root, query, cb) -> cb.or(cb.like(root.get("altName"), likeSearch),
					cb.like(root.get("englishName"), likeSearch),
					cb.like(root.get("name"), likeSearch));
			}
		}
		return new SpellsFvtt(spellRepository.findAll(specification)
			.stream()
			.map(SpellFvtt::new)
			.collect(Collectors.toList())
		);
	}

	@Operation(summary = "Список SRD заклинаний")
	@CrossOrigin
	@GetMapping(value = "/api/fvtt/v1/srd/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public SpellsFvtt getSrdSpells() {
		Specification<Spell> specification = (root, query, cb) -> cb.isNotNull(root.get("srd"));
		return new SpellsFvtt(spellRepository.findAll(specification)
			.stream()
			.map(SpellFvtt::new)
			.collect(Collectors.toList())
		);
	}

	@Operation(summary = "Загрузка всех существ в json в формате FVTT")
	@CrossOrigin
	@GetMapping("/api/fvtt/v1/bestiary")
	public FBeastiary getCreatures() {
		List<FBeast> list = bestiaryRepository.findAll()
			.stream()
			.map(FBeast::new)
			.collect(Collectors.toList());
		return new FBeastiary(list);
	}

	private byte[] convertListToJson(List<JsonStorage> jsonStorageList) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsBytes(jsonStorageList);
		} catch (JsonProcessingException e) {
			return new byte[0];
		}
	}

	private byte[] serializeToJson(Object object) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsBytes(object);
		} catch (Exception e) {
			return new byte[0];
		}
	}

	private HttpHeaders createResponseHeaders(String filename) {
		HttpHeaders responseHeaders = new HttpHeaders();
		String file = String.format("attachment; filename=\"%s.json\"", filename);
		responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, file);
		responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return responseHeaders;
	}


}
