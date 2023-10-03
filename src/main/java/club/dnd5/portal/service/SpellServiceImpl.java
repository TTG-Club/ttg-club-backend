package club.dnd5.portal.service;

import club.dnd5.portal.dto.fvtt.export.FActivation;
import club.dnd5.portal.dto.fvtt.export.FConsume;
import club.dnd5.portal.dto.fvtt.export.FDuration;
import club.dnd5.portal.dto.fvtt.export.spell.Fspell;
import club.dnd5.portal.dto.fvtt.export.system.FSystem;
import club.dnd5.portal.dto.fvtt.export.token.FFlags;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.SpellRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SpellServiceImpl implements SpellService {

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


		FFlags flags = new FFlags();
		fspell.setFlags(flags);

		fspell.setDescription(spell.getDescription());

		return fspell;
	}

	private void setFSystem(Spell spell, Fspell fspell) {

	}

}
