package club.dnd5.portal.model.creature;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.Setter;

/**
 * Действие существа (обычное, бонусное, реакция, легендарное, мистическое) — элемент
 * jsonb-массива {@code creatures.actions}. У каждого существа своя копия: раньше одно действие
 * могли разделять несколько существ, но мастерская при сохранении всё равно создавала новое.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Action {
	private String name;
	private String englishName;
	private String description;
	private ActionType actionType;
	/**
	 * Если действие содержит markdown
	 */
	private boolean markdown;
}
