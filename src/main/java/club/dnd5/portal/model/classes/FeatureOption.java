package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.effect.ActiveEffect;
import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Вариант умения класса (воззвание, манёвр, метамагия, боевой стиль) — элемент jsonb-списка
 * {@code options} у умений класса и архетипа, см. db/changelog 0048. Умение — единственное место,
 * где вариант правится; раздел «Особенности классов» ({@link Option}) — его копия, которую
 * обновляет {@code FeatureOptionProjection} при сохранении класса.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class FeatureOption {
	@NotBlank
	@Size(max = 255)
	private String name;

	/** Английское название: по нему строится адрес /options/&lt;slug&gt; и находится запись раздела. */
	@NotBlank
	@Size(max = 255)
	private String englishName;

	@Size(max = 255)
	private String altName;

	/** С какого уровня класса доступен; {@code null} — с уровня умения. */
	@Min(1)
	@Max(20)
	private Integer level;

	/** Требования текстом: «Заклинание «Мистический заряд»»; пусто — нет. */
	@Size(max = 255)
	private String prerequisite;

	/** Короткая подпись в списке вариантов. */
	@Size(max = 255)
	private String additional;

	/** Разметка core-api. */
	@NotNull
	private List<String> description = new ArrayList<>();

	/** Вариант можно взять повторно. */
	private boolean repeatable;

	/** Не показывать вариант на странице подкласса (только у умений класса). */
	private boolean hideInSubclasses;

	/** Механика для листа персонажа; {@code null} — не заполнена. */
	@Valid
	private ClassMechanics mechanics;

	/** Активные эффекты (Active Effects VTTG); {@code null} — нет. */
	@Valid
	private List<ActiveEffect> activeEffects;

	/** Аббревиатура книги-источника; пусто — книга класса. */
	@Size(max = 255)
	private String source;

	private Short page;

	/**
	 * Адрес варианта в разделе «Особенности классов».
	 *
	 * @return /options/&lt;slug&gt;
	 */
	public String url() {
		return "/options/" + StringUtil.getUrl(englishName);
	}
}
