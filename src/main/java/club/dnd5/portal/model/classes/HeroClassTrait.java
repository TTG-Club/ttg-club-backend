package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.mechanics.ClassMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;
import club.dnd5.portal.model.book.Book;
import lombok.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "hero_class_traits")
@Data
public class HeroClassTrait {
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

	@ManyToOne(targetEntity = HeroClass.class)
	private HeroClass heroClass;

	private boolean architype;
	private String child;
	private int optional;

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

	/** jsonb-колонка NOT NULL: пустое описание хранится как [], а не NULL. */
	@jakarta.persistence.PrePersist
	@jakarta.persistence.PreUpdate
	private void nullDescriptionToEmpty() {
		description = description == null ? new ArrayList<>() : description;
	}
}
