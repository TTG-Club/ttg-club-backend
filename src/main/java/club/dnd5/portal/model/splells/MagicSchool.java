package club.dnd5.portal.model.splells;

public enum MagicSchool {
	CONJURATION("вызов"),            // 0
	EVOCATION("воплощение"),         // 1
	ILLUSION("иллюзия"),             // 2
	NECROMANCY("некромантия"),       // 3
	ABJURATION("ограждение"),        // 4
	ENCHANTMENT("очарование"),       // 5
	TRANSMUTATION("преобразование"), // 6
	DIVINATION("прорицание");        // 7

	private String name;

	MagicSchool(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static MagicSchool getMagicSchool(String name) {
		for (MagicSchool school : values()) {
			if (school.name.equalsIgnoreCase(name)) {
				return school;
			}
		}
		return null;
	}
}
