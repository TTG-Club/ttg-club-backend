package club.dnd5.portal.model.splells;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Урон заклинания для листа персонажа — jsonb-колонка {@code spells.damage} (db/changelog 0046).
 * Хранит броски и правила их роста: заговор умножает кости на 5, 11 и 17 уровнях персонажа,
 * заклинание круга добавляет {@link #upcastDice} за каждый круг ячейки выше своего.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class SpellDamage {
	@NotNull
	@Size(max = 10)
	private List<@Valid @NotNull SpellDamageRoll> rolls = new ArrayList<>();

	/** Заговор: кости бросков ×2 / ×3 / ×4 на 5 / 11 / 17 уровне персонажа. */
	private boolean cantripScaling;

	/** Кости, добавляемые первому броску за каждый круг ячейки выше круга заклинания. */
	@Pattern(regexp = "^(\\d{1,2}d\\d{1,3})?$")
	private String upcastDice;

	/**
	 * Приводит урон из запроса мастерской к хранимому виду: пустые броски отбрасываются,
	 * строки обрезаются; без бросков урона нет вовсе ({@code null}).
	 *
	 * @param damage урон из запроса; может быть {@code null}
	 * @return урон для сохранения или {@code null}
	 */
	public static SpellDamage normalize(SpellDamage damage) {
		if (damage == null || damage.getRolls() == null) {
			return null;
		}
		List<SpellDamageRoll> rolls = new ArrayList<>();
		for (SpellDamageRoll roll : damage.getRolls()) {
			if (roll == null) {
				continue;
			}
			String dice = StringUtils.hasText(roll.getDice()) ? roll.getDice().trim() : null;
			if (dice == null && roll.getBonus() == 0 && !roll.isAbilityModifier()) {
				continue;
			}
			SpellDamageRoll normalized = new SpellDamageRoll();
			normalized.setDice(dice);
			normalized.setBonus(roll.getBonus());
			normalized.setAbilityModifier(roll.isAbilityModifier());
			normalized.setTypes(roll.getTypes() == null
				? new ArrayList<>()
				: new ArrayList<>(roll.getTypes().stream().filter(Objects::nonNull).distinct().toList()));
			normalized.setHealing(roll.isHealing());
			normalized.setCondition(StringUtils.hasText(roll.getCondition()) ? roll.getCondition().trim() : null);
			rolls.add(normalized);
		}
		if (rolls.isEmpty()) {
			return null;
		}
		SpellDamage result = new SpellDamage();
		result.setRolls(rolls);
		result.setCantripScaling(damage.isCantripScaling());
		result.setUpcastDice(StringUtils.hasText(damage.getUpcastDice()) ? damage.getUpcastDice().trim() : null);
		return result;
	}
}
