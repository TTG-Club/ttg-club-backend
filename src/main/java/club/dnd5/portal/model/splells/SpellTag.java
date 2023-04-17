package club.dnd5.portal.model.splells;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SpellTag {
	BANISHMENT("Изгнание"),
	BUFF("Положительный эффект"),
	CHARMED("Очарование"),
	COMBAT("Боевое"),
	COMMUNICATION("Общение"),
	COMPULSION("Принуждение"),
	CONTROL("Котроль"),
	CREATION("Создание"),
	DAMAGE("Урон"),
	DEBUFF("Отрицательный эффект"),
	DECEPTION("Обман"),
	DUNAMANCY("Дюнамантия"),
	ENVIRONMENT("Окружение"),
	EXPLORATION("Исследование"),
	FOREKNOWLEDGE("Предвидение"),
	HEALING("Лечение"),
	MOVEMENT("Перемещение"),
	NEGATION("Контрдействие"),
	PSIONIC("Пси"),
	SCRYING("Наблюдение"),
	SHAPECHANGING("Изменение формы"),
	SOCIAL("Социальное"),
	SPECIAL("Специальное"),
	SUMMONING("Призыв"),
	TELEPORTATION("Телепортация"),
	UTILITY("Утилита"),
	WARDING("Защита");
	private String name;
}
