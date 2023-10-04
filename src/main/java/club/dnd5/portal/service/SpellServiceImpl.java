package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.spells.Duration;
import club.dnd5.portal.dto.fvtt.export.FDuration;
import club.dnd5.portal.dto.fvtt.export.spell.Fspell;
import club.dnd5.portal.dto.fvtt.export.system.FDescription;
import club.dnd5.portal.dto.fvtt.export.system.FSystemSpell;
import club.dnd5.portal.dto.fvtt.export.token.FFlags;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.SpellRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpellServiceImpl implements SpellService {

	//TODO rename methods

	//1 метод из спела ТТГ в фаундри
	//2 метод из моба ТТГ с фаундрами - там проблема с type

	//Fsystem Spell включает в себя много-уровенные дто, одно-уровневые поля, будут включаться в отдельном методе
	private final SpellRepository spellRepository;

	@Override
	public Fspell convertFromSpellIntoFspell(Integer id) {
		Spell spell = spellRepository.findById(id).orElseThrow(PageNotFoundException::new);
		Fspell fspell = new Fspell();

		fspell.setName(spell.getName() + " / " + spell.getEnglishName());
		fspell.setType("spell");
		fspell.setImg(spell.getImg());
		setFFlags(spell, fspell);
		fspell.setSource(spell.getBook().getSource());

		fspell.setFSystemSpell(fillFSystemSpell(spell, fspell));

		return fspell;
	}

	private void setFFlags(Spell spell, Fspell fspell) {
		FFlags flags = new FFlags();
		fspell.setFlags(flags);
	}


	private FSystemSpell fillFSystemSpell(Spell spell, Fspell fspell) {
		FSystemSpell fSystemSpell = new FSystemSpell();
		fSystemSpell.setDescription(convertingDescriptionFromDatabase(spell));
		fSystemSpell.setDuration(convertingDurationFromDatabase(spell));

		return fSystemSpell;
	}


	private FDuration convertingDurationFromDatabase(Spell spell) {
		FDuration fDuration = new FDuration();
		Duration duration = new Duration(spell.getDuration());
		//обратить внимание что например instant в фаундри 11 это - inst ! затестить это
		fDuration.setUnits(fDuration.getUnits());
		fDuration.setValue(fDuration.getValue());
		return null;
	}

	private FDescription convertingDescriptionFromDatabase(Spell spell) {
		FDescription description = new FDescription();
		description.setChat("");
		description.setValue(spell.getDescription());
		description.setUnidentified("");
		return description;
	}

}
