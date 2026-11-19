package club.dnd5.portal.model.creature;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Data;

/**
 * Умение существа — элемент jsonb-массива {@code creatures.feats}.
 */
@Data
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class CreatureFeat {
	private String name;
	private String englishName;
	private String description;
	/**
	 * Если умение содержит markdown
	 */
	private boolean markdown;
}
