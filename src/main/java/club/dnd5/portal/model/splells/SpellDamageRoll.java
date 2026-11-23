package club.dnd5.portal.model.splells;

import club.dnd5.portal.model.DamageType;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Один бросок урона (или лечения) заклинания — элемент {@link SpellDamage#getRolls()}.
 * Взаимоисключающие броски (кость зависит от состояния цели) — отдельные элементы
 * со своим {@link #condition}.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class SpellDamageRoll {
	/** Кости броска: «8d6»; пусто — бросок без костей (только бонус). */
	@Pattern(regexp = "^(\\d{1,2}d\\d{1,3})?$")
	private String dice;

	/** Плоский бонус к броску. */
	@Min(-99)
	@Max(99)
	private int bonus;

	/** К броску прибавляется модификатор заклинательной характеристики. */
	private boolean abilityModifier;

	/** Типы урона; несколько — на выбор заклинателя («кислота или холод»). */
	@NotNull
	private List<DamageType> types = new ArrayList<>();

	/** Бросок лечит, а не ранит. */
	private boolean healing;

	/** Условие броска («Цель с неполными хитами»); пусто — бросок всегда. */
	@Size(max = 120)
	private String condition;
}
