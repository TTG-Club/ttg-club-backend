package club.dnd5.portal.model.background;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LifeStyle {
	ANYTHING("Никудышное"),
	BEGGARLY("Нищенское"),
	POOR("Бедное"),
	MODEST("Скромное"),
	COMFORTABLE("Комфортное"),
	RICH("Богатое"),
	ARISTOCRATIC("Аристократическое");
	
	private final String cyrilicName;
}