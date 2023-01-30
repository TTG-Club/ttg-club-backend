package club.dnd5.portal.dto.fvtt.export.system;

import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.creature.Spellcater;
import club.dnd5.portal.dto.fvtt.export.FAC;
import club.dnd5.portal.dto.fvtt.export.FHP;
import club.dnd5.portal.dto.fvtt.export.FInit;
import club.dnd5.portal.dto.fvtt.export.FSenses;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FAttributes {
	private FAC ac;
	private FHP hp;
	private FInit init;
	private FMovement movement;
	public FSenses senses;
	public String spellcasting = "";
	private byte prof;
	private byte spelldc = 10;
	private byte spellLevel;

	public FAttributes(Creature creature) {
		ac = new FAC(creature.getAC());
		if (creature.getArmorTypes().contains(ArmorType.NATURAL)) {
			ac.setCalc("natural");
		}
		hp = new FHP(creature);
		init = new FInit();
		movement = new FMovement(creature);
		senses = new FSenses(creature);
		if (!creature.getSpellcasters().isEmpty()) {
			for (Spellcater spellcaster : creature.getSpellcasters()) {
				if (spellcaster.getLevel() > 0) {
					spellcasting = spellcaster.getSpellAbility().name().toLowerCase().substring(0, 3);
					spelldc = spellcaster.getDc();
				}
			}
		}
	}
}
