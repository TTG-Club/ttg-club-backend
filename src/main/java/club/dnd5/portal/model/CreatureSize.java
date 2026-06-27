package club.dnd5.portal.model;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum CreatureSize {
	TINY(Dice.d4, 2.5f, "Крошечный","Крошечная", "Крошечное"), // 0
	SMALL(Dice.d6, 3.5f, "Маленький", "Маленькая", "Маленькое"), // 1
	MEDIUM(Dice.d8, 4.5f, "Средний", "Средняя", "Среднее"), // 2
	LARGE(Dice.d10, 5.5f, "Большой", "Большая", "Большое"), // 3
	HUGE(Dice.d12, 6.5f, "Огромный", "Огромная", "Огромное"), // 4
	GARGANTUAN(Dice.d20, 10.5f, "Громадный", "Громадная", "Громадное"), //5
	SMALL_MEDIUM(Dice.d8, 4.5f, "Средний или Маленький", "Средняя или Маленькая", "Среднее или Маленькое");

	private final String [] names;
	private final Dice hitDice;
	private final float hitAverage;

	CreatureSize(Dice hitDice, float hitAverage, String... names){
		this.hitDice = hitDice;
		this.hitAverage = hitAverage;
		this.names = names;
	}

	public static CreatureSize parse(String size) {
		for (CreatureSize creatureSize : values()) {
			for (String sizeName : creatureSize.names) {
				if (sizeName.equalsIgnoreCase(size)) {
					return creatureSize;
				}
			}
		}
		return null;
	}
	public static Set<CreatureSize> getFilterSizes(){
		return EnumSet.of(TINY, SMALL, MEDIUM, LARGE, HUGE, GARGANTUAN);
	}

	public String getSizeName(CreatureType type) {
		switch (type) {
		case ABERRATION:
		case FEY:
		case OOZE:
		case UNDEAD:
		case SLIME:
		case SMALL_BEAST:
			return names[1];
		case FIEND:
		case PLANT:
			return names[2];
		default:
			return names[0];
		}
	}

	public String getCyrillicName() {
		return names[0];
	}

	public String getCell() {
		switch (this) {
		case TINY: return "1/4 клетки";
		case SMALL: return "1 клетка";
		case MEDIUM: return "1 клетка";
		case LARGE: return "2x2 клетки";
		case HUGE: return "3x3 клетки";
		case GARGANTUAN: return "4х4 клетки или больше";
		default: return "-";
		}
	}
}
