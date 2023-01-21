package club.dnd5.portal.model.foundary;

import club.dnd5.portal.model.foundary.data.FBonuses;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FAbility {
    public byte value;
    public byte proficient;
    private FAbilityBonuses bonuses = new FAbilityBonuses();
}
