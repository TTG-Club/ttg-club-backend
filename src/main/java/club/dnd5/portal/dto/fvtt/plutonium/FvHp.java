package club.dnd5.portal.dto.fvtt.plutonium;

import club.dnd5.portal.model.creature.Creature;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FvHp {
    public short average;
    public String formula;
    public FvHp(Creature creature){
    	average = creature.getAverageHp();
    	formula = creature.getHpFormula();
    }
}
