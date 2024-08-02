package club.dnd5.portal.model.splells;

import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.FoundryCommon;
import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.HeroClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "spells")
public class Spell extends Name implements FoundryCommon {
	public static final int MAX_LEVEL = 9;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

	private byte level;
	private Boolean ritual;

	@Enumerated(EnumType.ORDINAL)
	private MagicSchool school;
	private String additionalType;

	private int timeCast;

	@OneToMany
	@JoinColumn(name = "spell_id")
	private List<TimeCast> times;

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

	@ElementCollection(targetClass = DamageType.class)
	@JoinTable(name = "spell_damage_type", joinColumns = @JoinColumn(name = "spell_id"))
	@Column(name = "damage_type", nullable = false)
	@Enumerated(javax.persistence.EnumType.STRING)
	private List<DamageType> damageType;

	@ManyToMany
	private List<HeroClass> heroClass;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	private Short page;
	private Boolean srd;

	public String getComponents() {
		return (verbalComponent ? "В" : "") + (somaticComponent ? "C" : "") + (additionalMaterialComponent != null ? "М" : "");
	}
}
