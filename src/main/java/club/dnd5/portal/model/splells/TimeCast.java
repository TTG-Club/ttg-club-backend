package club.dnd5.portal.model.splells;

import club.dnd5.portal.model.TimeUnit;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Время сотворения заклинания — элемент jsonb-массива {@code spells.times}.
 * В JSON пишутся только поля (вычисляемый {@link #getName()} — нет).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class TimeCast {
	private int number;
	private TimeUnit unit;
	private String condition;

	public String getName() {
		if (unit == TimeUnit.ON_HIT) {
			return unit.getName();
		}
		if (condition == null) {
			return String.format("%d %s", number, unit.getName(number));
		}
		return String.format("%d %s, %s ", number, unit.getName(number), condition);
	}
}
