package club.dnd5.portal.service;

import club.dnd5.portal.model.FoundryVersion;

import java.util.List;

public interface LssService {
	String findByIdAndFoundryVersion(Integer spellId, FoundryVersion foundryVersion);
	List<String> getAllSpellForLSS();
}
