package club.dnd5.portal.model.splells;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.FoundryCommon;
import club.dnd5.portal.model.HealType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.HeroClass;
import club.dnd5.portal.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "spells")
public class Spell implements FoundryCommon {
	public static final int MAX_LEVEL = 9;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private byte level;
	private Boolean ritual;

	private String name;
	private String altName;

	@Column(unique = true)
	private String englishName;

	@Enumerated(EnumType.ORDINAL)
	private MagicSchool school;
	private String additionalType;

	private int timeCast;

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, см. db/changelog 0013
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<TimeCast> times = new ArrayList<>();

	private String distance;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TEXT")
	private String upperLevel;

	private boolean verbalComponent;
	private boolean somaticComponent;

	@Column(columnDefinition = "boolean default false")
	private Boolean consumable;

	@Column(columnDefinition = "TEXT")
	private String additionalMaterialComponent;
	private String duration;
	private Boolean concentration;

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, см. db/changelog 0013
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<DamageType> damageType = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, см. db/changelog 0013
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<HealType> healType = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, см. db/changelog 0013
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<AbilityType> savingthrows = new ArrayList<>();

	// Имена колонок заданы явно: у двусторонней связи (HeroClass.spells — mappedBy)
	// Hibernate по умолчанию назвал бы колонку spells_id вместо существующей spell_id.
	@ManyToMany
	@JoinTable(name = "spells_hero_class",
		joinColumns = @JoinColumn(name = "spell_id"),
		inverseJoinColumns = @JoinColumn(name = "hero_class_id"))
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<HeroClass> heroClass;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	private Short page;
	private Boolean srd;

	/** jsonb-колонки NOT NULL: «нет значений» хранится как [], а не NULL. */
	@PrePersist
	@PreUpdate
	private void nullCollectionsToEmpty() {
		times = times == null ? new ArrayList<>() : times;
		damageType = damageType == null ? new ArrayList<>() : damageType;
		healType = healType == null ? new ArrayList<>() : healType;
		savingthrows = savingthrows == null ? new ArrayList<>() : savingthrows;
	}

	public String getComponents() {
		return (verbalComponent ? "В" : "") + (somaticComponent ? "C" : "") + (additionalMaterialComponent != null ? "М" : "");
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
