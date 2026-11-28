package club.dnd5.portal.model.classes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Вариант строки стартового снаряжения класса: набор предметов и, возможно, монеты.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class StartingEquipmentOption {
	/** Предметы варианта. */
	private List<StartingEquipmentItem> items = new ArrayList<>();

	/** Золотые монеты; {@code null} — монет нет. */
	private Integer coins;
}
