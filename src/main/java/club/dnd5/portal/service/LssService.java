package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.spells.SpellLSS;

import java.util.List;

public interface LssService {
	SpellLSS findByName(String name);
	List<SpellLSS> getAllSpellForLSS();
}
