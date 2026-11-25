package club.dnd5.portal.model.classes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Рост умения по уровням — элемент jsonb-списка {@code scaling} у умений класса
 * ({@code hero_class_traits}) и архетипа ({@code hero_class_feats}), см. db/changelog 0047.
 * Уровень, на котором умение повторяется или усиливается: «Увеличение характеристик» на 8, 12,
 * 16 и 19 уровнях, вторая «Дополнительная атака». В таблице класса каждая строка — отдельная
 * запись со ссылкой на умение.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class FeatureScaling {
	@Min(1)
	@Max(20)
	private int level;

	/** Своё название на этом уровне; пусто — как у умения. */
	@Size(max = 255)
	private String name;

	/** Короткая подпись в таблице после названия: «(2)», «(3 применения)». */
	@Size(max = 255)
	private String additional;

	/** Что меняется на этом уровне — разметка core-api; пусто — описание у самого умения. */
	@NotNull
	private List<String> description = new ArrayList<>();

	/** Строка не показывается в таблице подкласса (только у умений класса). */
	private boolean hideInSubclasses;

	/**
	 * Название строки в таблице: своё или умения.
	 *
	 * @param traitName название умения
	 * @return название уровня роста
	 */
	public String displayName(String traitName) {
		return StringUtils.hasText(name) ? name.trim() : Objects.toString(traitName, "");
	}

	/**
	 * Приводит рост из запроса мастерской к хранимому виду: строки без уровня отбрасываются,
	 * пустые строки становятся {@code null}, список сортируется по уровню.
	 *
	 * @param scaling рост из запроса; может быть {@code null}
	 * @param allowHide можно ли скрывать строку в подклассе (только у умений класса)
	 * @return рост для сохранения; пустой список, если строк нет
	 */
	public static List<FeatureScaling> normalize(List<FeatureScaling> scaling, boolean allowHide) {
		if (scaling == null) {
			return new ArrayList<>();
		}
		List<FeatureScaling> result = new ArrayList<>();
		for (FeatureScaling row : scaling) {
			if (row == null || row.getLevel() < 1 || row.getLevel() > 20) {
				continue;
			}
			FeatureScaling normalized = new FeatureScaling();
			normalized.setLevel(row.getLevel());
			normalized.setName(trimToNull(row.getName()));
			normalized.setAdditional(trimToNull(row.getAdditional()));
			normalized.setDescription(row.getDescription() == null
				? new ArrayList<>()
				: new ArrayList<>(row.getDescription().stream()
					.filter(StringUtils::hasText)
					.map(String::trim)
					.toList()));
			normalized.setHideInSubclasses(allowHide && row.isHideInSubclasses());
			result.add(normalized);
		}
		result.sort(Comparator.comparingInt(FeatureScaling::getLevel));
		return result;
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}
}
