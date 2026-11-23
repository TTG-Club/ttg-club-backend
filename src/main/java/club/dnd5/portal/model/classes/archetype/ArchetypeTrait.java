package club.dnd5.portal.model.classes.archetype;

import club.dnd5.portal.model.classes.FeatureScaling;

import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import java.util.ArrayList;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.splells.Spell;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "hero_class_feats")
public class ArchetypeTrait {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	@Column(columnDefinition = "varchar(25) default ''")
	private String suffix;
	private byte level;
	/** Разметка core-api, массив строк-блоков (см. db/changelog 0033). */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<String> description = new ArrayList<>();
	private String child;

	@ManyToOne
	private Archetype archetype;

	@OneToMany(fetch = FetchType.LAZY)
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<Spell> spells;

	/** Механика для листа персонажа (модель core-api); {@code null} — не заполнена. См. db/changelog 0044. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private ClassMechanics mechanics;

	/** Активные эффекты (модель Active Effects VTTG, как в core-api); {@code null} — нет. См. db/changelog 0044. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<ActiveEffect> activeEffects;

	/** Рост по уровням (см. db/changelog 0047); пусто — умение не повторяется. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<FeatureScaling> scaling = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	/** jsonb-колонка NOT NULL: пустое описание хранится как [], а не NULL. */
	@jakarta.persistence.PrePersist
	@jakarta.persistence.PreUpdate
	private void nullDescriptionToEmpty() {
		description = description == null ? new ArrayList<>() : description;
		scaling = scaling == null ? new ArrayList<>() : scaling;
	}
}
