package club.dnd5.portal.dto.fvtt.export;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FFly {
	private short number;
	private String condition = "(hover)";
	public FFly(short number) {
		this.number = number;
	}
}
