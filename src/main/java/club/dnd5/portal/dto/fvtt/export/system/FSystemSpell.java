package club.dnd5.portal.dto.fvtt.export.system;

import club.dnd5.portal.dto.fvtt.export.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FSystemSpell {
	private FComponents components;
	private FMaterials materials;
	private FPreparation preparation;
	private FConsume consume;
	private FCritical critical;

	private FDamage damage; //++

	private FActivation activation;
	private FDuration duration;

	private FSave save;

	private FScaling scaling;

	private FUses uses;

	private FRange range;

	private FTarget target;

	private FDescription description;
}
