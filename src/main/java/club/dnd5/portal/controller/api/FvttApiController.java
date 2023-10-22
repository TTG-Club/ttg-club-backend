package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.spells.SpellFvtt;
import club.dnd5.portal.dto.api.spells.SpellsFvtt;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.JsonStorageRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.service.JsonStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Tag(name = "Foundry", description = "API для экспорта в Foundry")
@RestController
@RequestMapping(value = "/api/fvtt/v1")
public class FvttApiController {

	private final JsonStorageService jsonStorageService;

	private final JsonStorageRepository jsonStorageRepository;

	private final BestiaryRepository bestiaryRepository;

	private final SpellRepository spellRepository;

	@Operation(summary = "Загрузка заклинания в json в формате FVTT")
	@GetMapping(value = "/spell", produces = "application/json")
	public ResponseEntity<byte[]> getSpellsFvtt(
		@RequestParam(required = false) Integer id,
		@RequestParam(required = false, defaultValue = "11") Integer version
	) {
		if (Objects.nonNull(id)) {
			JsonStorage jsonStorage = jsonStorageService.editSpellJson(id, version).get();
			HttpHeaders responseHeaders = createResponseHeaders(jsonStorage.getName());
			byte[] jsonDataBytes = jsonStorage.getJsonData().getBytes(StandardCharsets.UTF_8);
			return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(jsonDataBytes);
		} else {
			HttpHeaders responseHeaders = createResponseHeaders("spells_list");
			List<String> spellsList = jsonStorageService.getAllJson(JsonType.SPELL, version);
			String jsonData = null;
			try {
				jsonData = new ObjectMapper().writeValueAsString(spellsList);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			byte[] jsonDataBytes = jsonData.getBytes(StandardCharsets.UTF_8);
			return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(jsonDataBytes);
		}
	}

	@Operation(summary = "Загрузка существа в json в формате FVTT")
	@GetMapping(value = "/bestiary", produces = "application/json")
	public ResponseEntity<byte[]> getCreatureFvtt(
		@RequestParam(required = false) Integer id,
		@RequestParam(required = false, defaultValue = "11") Integer version
	) {
		if (Objects.nonNull(id)) {
			JsonStorage jsonStorage = jsonStorageService.editCreatureJson(id, version).get();
			HttpHeaders responseHeaders = createResponseHeaders(jsonStorage.getName());
			byte[] jsonDataBytes = jsonStorage.getJsonData().getBytes(StandardCharsets.UTF_8);
			return ResponseEntity.ok()
				.headers(responseHeaders)
				.body(jsonDataBytes);
		} else {
			HttpHeaders responseHeaders = createResponseHeaders("creature_list");
			List<String> bestiaryList = jsonStorageService.getAllJson(JsonType.CREATURE, version);
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

	@Deprecated
	@Operation(summary = "Список заклинаний в json в формате FVTT")
	@CrossOrigin
	@GetMapping(value = "/spells", produces = MediaType.APPLICATION_JSON_VALUE)
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

	@Deprecated
	@Operation(summary = "Список SRD заклинаний")
	@CrossOrigin
	@GetMapping(value = "/srd/spells", produces = MediaType.APPLICATION_JSON_VALUE)
	public SpellsFvtt getSrdSpells() {
		Specification<Spell> specification = (root, query, cb) -> cb.isNotNull(root.get("srd"));
		return new SpellsFvtt(spellRepository.findAll(specification)
			.stream()
			.map(SpellFvtt::new)
			.collect(Collectors.toList())
		);
	}

	private HttpHeaders createResponseHeaders(String filename) {
		HttpHeaders responseHeaders = new HttpHeaders();
		String file = String.format("attachment; filename=\"%s.json\"", filename);
		responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, file);
		responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		return responseHeaders;
	}
}
