package club.dnd5.portal.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static club.dnd5.portal.model.AbilityType.*;

/**
 * Навыки 
 */
public enum SkillType {
	ATHLETICS (STRENGTH, "Атлетика"), // 0
	ACROBATICS (DEXTERITY, "Акробатика"), // 1
	SLEIGHT_OF_HAND (DEXTERITY, "Лoвкость рук", "Ловкость Рук"), // 2
	STEALTH (DEXTERITY, "Скрытность"), // 3
	ARCANA (INTELLIGENCE, "Магия"), // 4
	HISTORY (INTELLIGENCE, "История"), // 5
	INVESTIGATION (INTELLIGENCE, "Анализ", "Расследование"), //6 
	NATURE (INTELLIGENCE, "Природа", "Естествознание"), //7 
	RELIGION (INTELLIGENCE, "Религия"), //8 
	ANIMAL_HANDLING (WISDOM, "Уход за животными", "Обращение с животными"), // 9
	INSIGHT (WISDOM,"Проницательность"), //10
	MEDICINE (WISDOM, "Медицина"), //11
	PERCEPTION (WISDOM, "Внимательность", "Восприятие"), //12
	SURVIVAL (WISDOM, "Выживание"), //13
	DECEPTION (CHARISMA, "Обман"), //14
	INTIMIDATION (CHARISMA, "Запугивание", "Устрашение"), //15
	PERFORMANCE (CHARISMA, "Выступление"), //16
	PERSUASION (CHARISMA, "Убеждение"); //17

	@Getter()
	private final AbilityType ability;
	@Getter()
	private final String cyrilicName;

	private final Set<String> cyrilicNames;

	
	SkillType(AbilityType ability, String ... cyrilicNames){
		this.ability = ability;
		this.cyrilicName = cyrilicNames[0];
		this.cyrilicNames = Arrays.stream(cyrilicNames).collect(Collectors.toSet());
	}

	public static SkillType parse(String cyrilicName) {
		for (SkillType type : values()) {
			if (type.cyrilicNames.contains(cyrilicName)) {
				return type;
			}
		}
		return null;
	}
}