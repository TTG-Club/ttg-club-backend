package club.dnd5.portal.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public enum CreatureType {
	ABERRATION("аберрация"), // 0
	BEAST("зверь"), // 1
	CELESTIAL("небожитель"), // 2
	CONSTRUCT("конструкт"), // 3
	DRAGON("дракон"), // 4
	ELEMENTAL("элементаль"), // 5
	FEY("фея", "фей"), // 6
	FIEND("исчадие"), // 7
	GIANT("великан", "гигант"), // 8
	HUMANOID("гуманоид"), // 9
	MONSTROSITY("монстр", "чудовище"), // 10
	OOZE("тина"), // 11
	OUTSIDER("потустаронний"), // 12
	PLANT("растение"), // 13
	DEVIL("демон", "изверг"), // 14
	UNDEAD("нежить"), // 15
	VERMIN("паразит"), // 16
	SLIME("слизь"), // 17
	SMALL_BEAST("стая крошечных зверей", "крохотных зверей", "рой крошечных зверей"),
	SWARM("рой", "стая");

	private final String displayCyrillicName;
	private final Set<String> cyrillicNames;

	CreatureType(String... cyrillicNames) {
		this.displayCyrillicName = cyrillicNames[0];
		this.cyrillicNames = new HashSet<>(Arrays.asList(cyrillicNames));
	}

	public String getCyrillicName() {
		return this.displayCyrillicName;
	}

	public static CreatureType parse(final String type) {
		return Arrays.stream(values()).filter(t -> t.cyrillicNames.contains(type)).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(type));
	}

	public static Set<CreatureType> getFilterTypes() {
		return EnumSet.of(ABERRATION, BEAST, CELESTIAL, CONSTRUCT, DRAGON, ELEMENTAL, FEY, FIEND, GIANT, HUMANOID,
				MONSTROSITY, PLANT, UNDEAD, SLIME, SMALL_BEAST);
	}
}
