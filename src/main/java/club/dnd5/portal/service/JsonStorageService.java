package club.dnd5.portal.service;

import club.dnd5.portal.model.exporter.JsonStorage;

public interface JsonStorageService {
	JsonStorage editSpellJson (Integer id);
	JsonStorage editCreatureJson (Integer id);
}

