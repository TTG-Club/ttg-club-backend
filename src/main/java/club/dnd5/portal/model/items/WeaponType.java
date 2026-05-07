package club.dnd5.portal.model.items;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WeaponType {
	SIMPLE_MELE("Простое рукопашное"),
	SIMPLE_RANGED("Простое дальнобойное"),
	WAR_MELE("Воинское рукопашное"),
	WAR_RANGED("Воинское дальнобойное"),
	EXOTIC_MELE("Экзотическое рукопашное оружие"), 
	EXOTIC_RANGED("Экзотическое дальнобойное оружие");
	
	private final String name;
}