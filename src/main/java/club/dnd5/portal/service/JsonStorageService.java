package club.dnd5.portal.service;

import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.exporter.JsonStorage;
import club.dnd5.portal.model.splells.Spell;


public interface JsonStorageService {
	JsonStorage editSpellJson (Integer id);
	JsonStorage editCreatureJson (Integer id);
}

