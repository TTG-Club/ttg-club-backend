package club.dnd5.portal.model.tracker;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TrackerStatus {
	PREPARING("Подготовка"),
	ACTIVE("Бой");

	private final String name;
}
