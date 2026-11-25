package club.dnd5.portal.model.creature;

import club.dnd5.portal.model.AbilityType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.Setter;

/**
 * Заклинательство существа — элемент jsonb-массива {@code creatures.spellcasters}.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Spellcater {
	private Byte level;
	private Byte dc;
	private Byte attack;
	private AbilityType spellAbility;
	private Byte slot1;
	private Byte slot2;
	private Byte slot3;
	private Byte slot4;
	private Byte slot5;
	private Byte slot6;
	private Byte slot7;
	private Byte slot8;
	private Byte slot9;
}
