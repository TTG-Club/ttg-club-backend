package club.dnd5.portal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Rest {
	SHORT("короткий отдых"),
	LONG("продолжительный отдых"),
	FULL("полный отдых"),;
	private final String cyrillicName;
}