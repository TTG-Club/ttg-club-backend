package club.dnd5.portal.model.tracker;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParticipantType {
	PLAYER("Игрок"),
	CREATURE("Существо");

	private final String name;
}
