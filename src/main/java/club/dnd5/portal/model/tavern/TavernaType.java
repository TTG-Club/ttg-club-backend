package club.dnd5.portal.model.tavern;

import java.util.Random;

public enum TavernaType {
	BEER("Пивная", "Кабак", "Бар", "Паб", "Дом эля", "Пивоварня"),
	INN("Постоялый двор", "Трактир", "Таверна", "Корчма", "Логово"),
	HOTEL("Гостиница", "Отель", "Дом", "Гостиный двор"),
	CAFE("Кафе", "Кофейня", "Чайная", "Чайный дом"),
	RESTAURANT("Ресторан", "Трапезная", "Харчевня", "Кухмистерская"),
	GAMBLING_DEN("Игорный дом", "Казино", "Притон");
	
	private final Random rnd = new Random();

	private final String[] names;

	TavernaType(String... names){
		this.names = names;
	}
	public String getName() {
		return names[rnd.nextInt(names.length)];
	}
	public String getNames() {
		return String.join(", ", names);
	}
}