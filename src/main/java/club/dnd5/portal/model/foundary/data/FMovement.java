package club.dnd5.portal.model.foundary.data;

import club.dnd5.portal.model.creature.Creature;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FMovement {
	private byte walk = 50;
	private short burrow = 0;
	private short climb = 0;
	private short fly = 0;
	private short swim = 0;

	private String units = "ft";
	private boolean hover = false;

	public FMovement(Creature creature) {
		walk = creature.getSpeed();
		if (creature.getClimbingSpeed() != null) {
			climb = creature.getClimbingSpeed();
		}
		if (creature.getFlySpeed() != null) {
			fly = creature.getFlySpeed();
		}
		if (creature.getSwimmingSpped() != null) {
			swim = creature.getSwimmingSpped();
		}
		if (creature.getDiggingSpeed() != null) {
			burrow = creature.getDiggingSpeed();
		}
		if (creature.getHover() != null && creature.getHover() == 1) {
			hover = true;
		}
	}
}
