package club.dnd5.portal.model.classes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Позиция варианта стартового снаряжения класса (jsonb {@code classes.starting_equipment},
 * db/changelog 0054): предмет справочника, свой предмет или выбор из категории.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class StartingEquipmentItem {
	/** Предмет: запись справочника ({@code url} заполнен) или свой предмет ({@code url} пуст). */
	public static final String KIND_ITEM = "ITEM";
	/** Любой предмет из категории на выбор игрока ({@code category}). */
	public static final String KIND_CHOICE = "CHOICE";

	/** {@link #KIND_ITEM} или {@link #KIND_CHOICE}. */
	private String kind;

	/** Раздел справочника у ITEM со ссылкой: WEAPON, ARMOR или ITEM; {@code null} у своего предмета. */
	private String section;

	/** Адрес записи справочника, как на сайте: «/weapons/longbow»; {@code null} у своего предмета. */
	private String url;

	/** Название на момент сохранения. */
	private String name;

	/** Количество, от 1. */
	private Integer quantity = 1;

	/** Уточнение, необязательно. */
	private String description;

	/** Категория у CHOICE: SIMPLE_WEAPON, ARMOR_LIGHT, INSTRUMENT и т.п. */
	private String category;
}
