package club.dnd5.portal.model.tavern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import club.dnd5.portal.model.creature.HabitatType;
import lombok.Getter;
import lombok.Setter;

/**
 * Вес (шанс) появления расы за столиком таверны в зависимости от местности.
 * Ключ — англ. слаг расы ({@code Race.englishName}) либо его часть: правило применяется
 * ко всем расам, в чьём {@code englishName} встречается это значение (напр. «dwarf» ловит
 * «mountain-dwarf»). Из нескольких подходящих правил берётся самое специфичное (длинный ключ).
 * Строка с {@code habitat = NULL} задаёт базовый вес расы во всех прочих местностях.
 */
@Entity
@Table(name = "taverna_race_habitates")
@Getter
@Setter
public class RaceHabitatChance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "english_name", nullable = false)
	private String englishName;

	@Enumerated(EnumType.STRING)
	private HabitatType habitat;

	private int chance;
}
