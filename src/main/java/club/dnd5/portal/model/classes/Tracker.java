package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.Rest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.Setter;

/**
 * Счётчик ресурса на уровне класса — элемент вложенного массива {@code trackers}
 * в {@link SpellLevelDefinition}.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Tracker {
	private String name;
	private String value;
	private Type type;
	private Rest rest;

	enum Type {
		Number,
		Counter,
		Roll
	}
}
