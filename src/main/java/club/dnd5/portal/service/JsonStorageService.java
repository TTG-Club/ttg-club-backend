package club.dnd5.portal.service;

import club.dnd5.portal.model.exporter.JsonStorage;

public interface JsonStorageService {
	JsonStorage editSpellJson (Integer id, Integer versionFoundry);
	JsonStorage editCreatureJson (Integer id, Integer versionFoundry);
}

