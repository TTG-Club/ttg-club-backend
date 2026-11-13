package club.dnd5.portal.model.classes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Тип списка особенностей класса (боевые стили, воззвания, приёмы и т.п.).
 * Справочник хранится в БД, поэтому новый класс со своими особенностями добавляется без изменения кода.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "code")
@Entity
@Table(name = "option_type_definitions")
public class OptionTypeDefinition {
	/** Код типа; он же хранится в options.option_type, option_types.option_type, classes.option_type, archetypes.option_type. */
	@Id
	@Column(length = 64)
	private String code;

	/** Полное название, например «Боевые стили: Паладин». */
	@Column(nullable = false)
	private String name;

	/** Английское название класса, к которому относится список. */
	@Column(name = "class_name", nullable = false)
	private String className;

	/** Фрагмент URL архетипа, если список относится к архетипу. */
	@Column(name = "archetype_name")
	private String archetypeName;

	@Column(nullable = false)
	private boolean homebrew;

	/** Корень названия умения класса, к которому прикрепляется список при экспорте в VTTG. */
	@Column(name = "feature_stem")
	private String featureStem;

	/** Порядок в фильтрах и списках. */
	private Integer ordering;

	public OptionTypeDefinition(String code, String name, String className, String archetypeName, boolean homebrew) {
		this.code = code;
		this.name = name;
		this.className = className;
		this.archetypeName = archetypeName;
		this.homebrew = homebrew;
	}

	/** Название без уточнения класса: «Боевые стили: Паладин» → «Боевые стили». */
	public String getDisplayName() {
		return name.contains(":") ? name.substring(0, name.indexOf(":")) : name;
	}
}
