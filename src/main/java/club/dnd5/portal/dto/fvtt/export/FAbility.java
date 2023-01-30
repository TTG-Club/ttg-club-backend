package club.dnd5.portal.dto.fvtt.export;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FAbility {
    public byte value;
    public byte proficient;
    private FAbilityBonuses bonuses = new FAbilityBonuses();
}
