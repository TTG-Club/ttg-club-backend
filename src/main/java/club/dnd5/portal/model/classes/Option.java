package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "options")
public class Option {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String altName;
	private String englishName;

	/** Типы списков, в которые входит особенность. Справочник типов — {@link OptionTypeDefinition}. */
	@ManyToMany
	@JoinTable(name = "option_types",
		joinColumns = @JoinColumn(name = "option_id"),
		inverseJoinColumns = @JoinColumn(name = "option_type"),
		foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
		inverseForeignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<OptionTypeDefinition> optionTypes;

	private String prerequisite;
	private Integer level;

	/** Разметка core-api: массив строк-блоков (jsonb, см. db/changelog 0023/0024). */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<String> description = new ArrayList<>();

	/** Механика для листа персонажа (модель core-api); {@code null} — не заполнена. См. db/changelog 0044. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private ClassMechanics mechanics;

	/** Активные эффекты (модель Active Effects VTTG, как в core-api); {@code null} — нет. См. db/changelog 0044. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<ActiveEffect> activeEffects;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	/** jsonb-колонка NOT NULL: «нет описания» хранится как [], а не NULL. */
	@PrePersist
	@PreUpdate
	private void nullDescriptionToEmpty() {
		description = description == null ? new ArrayList<>() : description;
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
