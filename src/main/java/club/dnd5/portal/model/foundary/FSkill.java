package club.dnd5.portal.model.foundary;

import club.dnd5.portal.model.foundary.data.FBonuses;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FSkill {
    private byte value;
    private String ability;
	private FAbilityBonuses bonuses = new FAbilityBonuses();
}
