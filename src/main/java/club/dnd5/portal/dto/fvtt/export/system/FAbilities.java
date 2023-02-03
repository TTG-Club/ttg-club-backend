package club.dnd5.portal.dto.fvtt.export.system;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.dto.fvtt.export.FAbility;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({ "str", "dex", "con", "int", "wis", "cha" })

@Getter
@Setter
public class FAbilities {
	@JsonProperty("str")
	private FAbility str;
	@JsonProperty("dex")
	private FAbility dex;
	@JsonProperty("con")
	private FAbility con;
    @JsonProperty("int")
	private FAbility intel;
	@JsonProperty("wis")
	private FAbility wis;
	@JsonProperty("cha")
	private FAbility cha;

	public FAbilities(Creature creature) {
		FAbility str = new FAbility();
		str.setValue(creature.getStrength());
		str.setProficient(isProficient(creature, AbilityType.STRENGTH));
		this.str = str;

		FAbility dex = new FAbility();
		dex.setValue(creature.getDexterity());
		dex.setProficient(isProficient(creature, AbilityType.DEXTERITY));
		this.dex = dex;

		FAbility con = new FAbility();
		con.setValue(creature.getConstitution());
		con.setProficient(isProficient(creature, AbilityType.CONSTITUTION));
		this.con = con;

		FAbility intel = new FAbility();
		intel.setValue(creature.getIntellect());
		intel.setProficient(isProficient(creature, AbilityType.INTELLIGENCE));
		this.intel = intel;

		FAbility wis = new FAbility();
		wis.setValue(creature.getWizdom());
		wis.setProficient(isProficient(creature, AbilityType.WISDOM));
		this.wis = wis;

		FAbility cha = new FAbility();
		cha.setValue(creature.getCharisma());
		cha.setProficient(isProficient(creature, AbilityType.CHARISMA));
		this.cha = cha;
	}

	private byte isProficient(Creature creature, AbilityType type) {
		return (byte) (creature.getSavingThrows().stream()
				.anyMatch(st -> st.getAbility().equals(type)) ? 1 : 0);
	}
}
