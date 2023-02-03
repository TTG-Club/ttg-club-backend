package club.dnd5.portal.dto.fvtt.export;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FSkill {
    private byte value;
    private String ability;
	private FAbilityBonuses bonuses = new FAbilityBonuses();
}
