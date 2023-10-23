package club.dnd5.portal.service;

import club.dnd5.portal.model.JsonType;
import club.dnd5.portal.model.exporter.JsonStorage;

import java.util.List;
import java.util.Optional;

public interface JsonStorageService {
	Optional<JsonStorage> editSpellJson (Integer id, Integer versionFoundry);
	Optional<JsonStorage> editCreatureJson (Integer id, Integer versionFoundry);
	List<String> getAllJson(JsonType jsonType, Integer versionFoundry);
}

