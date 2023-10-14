package club.dnd5.portal.dto.fvtt.export.spell;

import club.dnd5.portal.dto.fvtt.export.FSave;
import club.dnd5.portal.dto.fvtt.export.system.FSystemSpell;
import club.dnd5.portal.dto.fvtt.export.token.FFlags;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FSpell {
	private String name;

	private String type;

	private String img;

	private FFlags flags;

	private String source;

	private Integer targetValue;

	private String targetUnits;

	private String targetType;

	private Integer rangeValue;

	private Integer usesValue;

	private String usesMax;

	private String ability;

	private String actionType;

	private String attackBonus;

	private String chatFlavor;

	private String formula;

	private FSave save;

	private Integer level;

	private String school;

	private FSystemSpell fSystemSpell;
}
