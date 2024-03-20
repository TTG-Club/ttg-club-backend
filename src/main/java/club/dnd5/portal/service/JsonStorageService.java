package club.dnd5.portal.service;

import club.dnd5.portal.model.FoundryVersion;
import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.exporter.JsonStorage;

import java.util.List;
import java.util.Optional;

public interface JsonStorageService {
	Optional<JsonStorage> editSpellJson (Integer id, FoundryVersion versionFoundry);
	Optional<JsonStorage> editCreatureJson (Integer id, FoundryVersion versionFoundry);
	List<String> getAllJson(JsonType jsonType, 	FoundryVersion versionFoundry);
}

