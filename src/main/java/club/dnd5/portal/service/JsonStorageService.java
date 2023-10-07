package club.dnd5.portal.service;

import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.splells.Spell;


public interface JsonStorageService {
	String editSpellJson (Spell spell);
	String editCreatureJson (Creature creature);
}
