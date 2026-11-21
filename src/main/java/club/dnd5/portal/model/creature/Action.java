package club.dnd5.portal.model.creature;

import java.util.List;
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
	/** Разметка core-api, массив строк-блоков (см. db/changelog 0035). */
	private List<String> description;
	private ActionType actionType;
}
