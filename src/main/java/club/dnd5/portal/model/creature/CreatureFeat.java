package club.dnd5.portal.model.creature;

import java.util.List;
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
	/** Разметка core-api, массив строк-блоков (см. db/changelog 0035). */
	private List<String> description;
}
