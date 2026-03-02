package club.dnd5.portal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.thymeleaf.util.StringUtils;

import java.util.EnumSet;
import java.util.Set;

/**
 * Характеристики 
 */
@Getter
@AllArgsConstructor
public enum AbilityType {
	STRENGTH("Сила"),             // 0 
	DEXTERITY("Ловкость"),        // 1
	CONSTITUTION("Телосложение"), // 2
	INTELLIGENCE("Интеллект"),    // 3
	WISDOM("Мудрость"),           // 4
	CHARISMA("Харизма"),          // 5

	CHOICE("к другой"),
	ONE("к одной"),
	CHOICE_UNIQUE("к 2 другим"),
	CHOICE_DOUBLE("+2 и +1 / +1 к трем");

	private final String cyrilicName;

	public String getShortName() {
		switch (this) {
			case STRENGTH:
			case DEXTERITY:
			case CONSTITUTION:
			case INTELLIGENCE:
			case CHARISMA:
				return cyrilicName.substring(0,3);
			case WISDOM:
				return "Мдр";
			case CHOICE:
			case CHOICE_UNIQUE:
			case CHOICE_DOUBLE:
				return cyrilicName;
			default:
				return "";
		}
	}

	public String getCapitalizeName() {
		return StringUtils.capitalize(name().toLowerCase());
	}

	public static AbilityType parse(String name) {
		for (AbilityType abilityType : values()) {
			if (abilityType.getCyrilicName().equalsIgnoreCase(name)) {
				return abilityType;
			}
		}
		return null;
	}

	public static Set<AbilityType> getBaseAbility(){
		return EnumSet.of(STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA);
	}
}