package club.dnd5.portal.dto.fvtt.export;

import club.dnd5.portal.model.creature.Creature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FHP {
    private short value;
    private short min = 0;
    private short max;
    private byte temp;
    private byte tempmax;
    private String formula;

	public FHP(Creature creature) {
		value = creature.getAverageHp();
		max = creature.getAverageHp();
		formula = creature.getHpFormula();
	}
}
