package club.dnd5.portal.model.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString

public enum TypeBook {
	OFFICAL("Базовые"),
	MODULE("Приключения"),
	SETTING("Сеттинги"),
	TEST("Unearthed Arcana"),
	THIRD_PARTY("3rd party"),
	CUSTOM("Homebrew");

	private final String name;
}