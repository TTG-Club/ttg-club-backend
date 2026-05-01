package club.dnd5.portal.model.god;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum Domain {
	ARCANA("Магия"),
	DEATH("Смерть"),
	FORGE("Кузня"),
	GRAVE("Могила"),
	KNOWLEDGE("Знание"),
	LIFE("Жизнь"),
	LIGHT("Свет"),
	NATURE("Природа"),
	NONE("Нет"),
	ORDER("Порядок"),
	TEMPEST("Буря"),
	TRICKERY("Хитрость"),
	WAR("Война"),

	STORM("Шторм"),
	DECEPTION("Обман"),
	REPOSE("Упокоение"),
	UNDEFINE("Нет жрецов"); 

	private final String cyrilicName;

	public static Domain parse(String value) {
		return Arrays.stream(values())
				.filter(d -> d.getCyrilicName().equals(value))
				.findFirst()
				.orElseThrow(IllegalArgumentException::new);
	}
}