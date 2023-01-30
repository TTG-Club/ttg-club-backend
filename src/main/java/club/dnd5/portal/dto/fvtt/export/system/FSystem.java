package club.dnd5.portal.dto.fvtt.export.system;

import club.dnd5.portal.dto.fvtt.export.FSkills;
import club.dnd5.portal.dto.fvtt.export.FTraits;
import club.dnd5.portal.dto.fvtt.export.spell.FSpells;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.dto.fvtt.export.system.details.FDetails;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@Getter
@Setter
public class FSystem {
    public FAbilities abilities;
    public FAttributes attributes;
    public FDetails details;
    public FTraits traits;
    public FCurrency currency;
    public FSkills skills;
    public FSpells spells;
    public FBonuses bonuses;
    public FResources resources;

	public FSystem(Creature creature) {
		abilities = new FAbilities(creature);
		attributes = new FAttributes(creature);
		details = new FDetails(creature);
		traits = new FTraits(creature);
		skills = new  FSkills(creature.getSkills());
		if (!creature.getSpellcasters().isEmpty())
		{
			spells = new FSpells(creature);
		} else {
			spells = new FSpells();
		}
		bonuses = new FBonuses();
		resources = new FResources(creature);
	}
}
