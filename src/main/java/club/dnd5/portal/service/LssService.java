package club.dnd5.portal.service;

import java.util.List;

public interface LssService {
	String findByName(String name);
	List<String> getAllSpellForLSS();
}
