package club.dnd5.portal.model.classes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Ячейки заклинаний класса или архетипа на уровне — элемент jsonb-массива
 * {@code classes.level_definitions} / {@code archetypes.level_definitions}.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class SpellLevelDefinition {
	private Byte level;

	private Byte slot1;
	private Byte slot2;
	private Byte slot3;
	private Byte slot4;
	private Byte slot5;
	private Byte slot6;
	private Byte slot7;
	private Byte slot8;
	private Byte slot9;

	private List<Tracker> trackers = new ArrayList<>();
}
