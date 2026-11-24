package club.dnd5.portal.dto.fvtt.export;

import club.dnd5.portal.dto.fvtt.FvttHtml;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FDiscription {
	private String value;
	private String chat ="";
	private String unidentified ="";

	/** Броски {@code <dice-roller>} — в инлайн-броски Foundry {@code [[/r 2d6 + 3]]}. */
	public FDiscription(String description) {
		value = FvttHtml.rolls(description, formula -> "[[/r " + formula + "]]");
	}
}
