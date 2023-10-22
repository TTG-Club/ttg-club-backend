package club.dnd5.portal.service;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.FoundryCommon;
import club.dnd5.portal.model.JsonStorageCompositeKey;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.model.token.Token;
import club.dnd5.portal.repository.JsonStorageRepository;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class JsonStorageServiceImpl implements JsonStorageService {

	private final JsonStorageRepository jsonStorageRepository;

	private final TokenRepository tokenRepository;

	private final SpellRepository spellRepository;

	private final BestiaryRepository bestiaryRepository;

	private final String imgType = "круглый";

	private final String magicSchool = "https://img.ttg.club/magic/";

	private final String srcType = "вид сверху";

	public List<String> getAllJson(JsonType jsonType, Integer versionFoundry) {
		List<JsonStorage> jsonStorageList = jsonStorageRepository.findAllByTypeJsonAndVersionFoundry(jsonType, versionFoundry);
		if (jsonStorageList.isEmpty()) {
			throw new PageNotFoundException();
		}
		switch (jsonType) {
			case CREATURE:
				jsonStorageList = getAllJsonCreatures(jsonStorageList, versionFoundry);
				break;
			case SPELL:
				jsonStorageList = getAllJsonSpells(jsonStorageList, versionFoundry);
				break;
		}
		List<String> listFoundryCommon = new ArrayList<>();
		jsonStorageList.stream()
			.map(element -> listFoundryCommon.add(element.getJsonData()))
			.collect(Collectors.toList());
		return listFoundryCommon;
	}

	private List<JsonStorage> getAllJsonSpells(List<JsonStorage> jsonStorageList, Integer versionFoundry){
		return jsonStorageList.stream()
			.map(element -> editSpellJson(element.getRefId(), versionFoundry).get())
			.collect(Collectors.toList());
	}

	private List<JsonStorage>  getAllJsonCreatures(List<JsonStorage> jsonStorageList, Integer versionFoundry) {
		return jsonStorageList.stream()
			.map(element -> editCreatureJson(element.getRefId(), versionFoundry).get())
			.collect(Collectors.toList());
	}

	@SneakyThrows
	private Optional<JsonStorage> editJsonEntity(Integer id, JsonType jsonType, FoundryCommon entity, Integer versionFoundry) {
		JsonStorageCompositeKey compositeKey = new JsonStorageCompositeKey(id, jsonType, versionFoundry);
		JsonStorage jsonStorage = jsonStorageRepository.findById(compositeKey).orElseThrow(PageNotFoundException::new);
		ObjectMapper mapper = new ObjectMapper();
		try {
			JsonNode rootNode = mapper.readTree(jsonStorage.getJsonData());
			modifyName(entity, rootNode);
			if (versionFoundry != 10) {
				modifyBiography(entity, rootNode);
			}
			if (jsonStorage.getTypeJson().equals(JsonType.CREATURE)) {
				if (versionFoundry != 10) {
					modifyImgCreature(jsonStorage.getRefId(), rootNode);
				}
			} else {
				modifyImgSpell(jsonStorage.getRefId(), rootNode);
			}
			jsonStorage.setJsonData(mapper.writeValueAsString(rootNode));
		} catch (IOException e) {
			e.printStackTrace();
			return Optional.ofNullable(jsonStorage);
		}
		return Optional.ofNullable(jsonStorage);
	}

	public Optional<JsonStorage> editSpellJson(Integer id, Integer versionFoundry) {
		return editJsonEntity(id, JsonType.SPELL, spellRepository.findById(id).orElseThrow(PageNotFoundException::new), versionFoundry);
	}

	public Optional<JsonStorage> editCreatureJson(Integer id, Integer versionFoundry) {
		return editJsonEntity(id, JsonType.CREATURE, bestiaryRepository.findById(id).orElseThrow(PageNotFoundException::new), versionFoundry);
	}

	private void modifyBiography(FoundryCommon entity, JsonNode rootNode) {
		String description = entity.getDescription();
		ObjectNode systemNode = (ObjectNode) rootNode.get("system");
		if (systemNode != null) {
			ObjectNode detailsNode = systemNode.with("details");
			detailsNode.remove("biography");

			ObjectNode biographyNode = detailsNode.with("biography");
			biographyNode.put("value", description);
			biographyNode.put("public", "");
		}
	}

	private void modifyName(FoundryCommon entity, JsonNode rootNode) {
		String name = entity.getName();
		String englishName = "[" + entity.getEnglishName() + "]";
		if (Objects.isNull(name) || entity.getName().isEmpty()) {
			return;
		}
		((ObjectNode) rootNode).put("name", name + " " + englishName);
	}

	private void modifyImgSpell(Integer spellId, JsonNode rootNode) {
		if (((ObjectNode) rootNode).get("img").asText().contains("laaru")) {
			Spell spell = spellRepository.findById(spellId).get();
			String link = magicSchool + spell.getSchool().getMagicSchool(spell.getSchool().getName())+ ".webp";
			((ObjectNode) rootNode).put("img", link);
		}
	}

	private void modifyImgCreature(Integer creatureId, JsonNode rootNode) {
		String imgFiveETools = "";
		Optional<Token> imgToken = tokenRepository.findByRefIdAndType(creatureId, imgType).stream().findFirst();
		if (imgToken.isPresent()) {
			((ObjectNode) rootNode).put("img", imgToken.get().getUrl());
		} else {
			Creature creature = bestiaryRepository.findById(creatureId).get();
			imgFiveETools = StringUtils.capitalizeWords(String.format("https://5e.tools/img/%s/%s.png",
				creature.getBook().getSource(), creature.getEnglishName()));
			((ObjectNode) rootNode).put("img", imgFiveETools);
		}
		Optional<Token> srcToken = tokenRepository.findByRefIdAndType(creatureId, srcType).stream().findFirst();
		ObjectNode prototypeNode = (ObjectNode) rootNode.get("prototypeToken");
		if (prototypeNode != null) {
			ObjectNode textureNode = prototypeNode.with("texture");
			if (srcToken.isPresent()) {
				textureNode.put("src", srcToken.get().getUrl());
			} else {
				if (imgToken.isPresent()) {
					textureNode.put("src", imgToken.get().getUrl());
				} else {
					textureNode.put("src", imgFiveETools);
				}
			}
		}
	}
}

