package club.dnd5.portal.model.classes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Выбор из вариантов умения для листа персонажа — jsonb {@code options_choice} (db/changelog 0048).
 * {@code null} у умения — варианты остаются справкой и лист их не спрашивает.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class FeatureOptionsChoice {
	/** Подпись выбора; пусто — название списка вариантов. */
	@Size(max = 255)
	private String label;

	/** Сколько вариантов выбрать на уровне самого умения. */
	@Min(0)
	@Max(50)
	private int count;

	/** Рост количества: сколько вариантов выбрано ВСЕГО к уровню. */
	@NotNull
	private List<@Valid Step> scaling = new ArrayList<>();

	/** Ступень роста количества. */
	@Getter
	@Setter
	@NoArgsConstructor
	@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
		isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
	public static class Step {
		@Min(1)
		@Max(20)
		private int level;

		@Min(0)
		@Max(50)
		private int count;
	}
}
