package club.dnd5.portal.service;

import club.dnd5.portal.dto.fvtt.export.spell.Fspell;
import club.dnd5.portal.model.splells.Spell;
import org.springframework.stereotype.Service;

@Service
public interface SpellService {
	Fspell convertFromSpellIntoFspell (Integer id);
}
