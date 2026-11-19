package club.dnd5.portal.model.creature;

import club.dnd5.portal.model.AbilityType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Спасбросок существа — элемент jsonb-массива {@code creatures.saving_throws}.
 * В JSON пишутся только поля (вычисляемые геттеры вроде {@link #getText()} — нет).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class SavingThrow {
	private AbilityType ability;
	private byte bonus;
	private String additionalBonus;

	public SavingThrow(AbilityType ability, byte bonus) {
		this.ability = ability;
		this.bonus = bonus;
	}

	public String getText() {
		return String.format("%s %+d ", ability.getShortName(), bonus);
	}
}
