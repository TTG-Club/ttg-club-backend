package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.Rest;
import club.dnd5.portal.model.SkillType;
import club.dnd5.portal.model.SpellcasterType;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter

@Entity
@Table(name = "classes")
public class HeroClass {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String englishName;
	@Column(name = "name_accusative")
	private String accusativeName;
	@Column(columnDefinition = "TEXT")
	private String description;

	@JdbcTypeCode(SqlTypes.JSON) // jsonb, см. db/changelog 0015
	@Column(name = "level_definitions", columnDefinition = "jsonb", nullable = false)
	private List<SpellLevelDefinition> levelDefenitions = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "hero_class_id")
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<FeatureLevelDefinition> featureLevelDefenitions;

	private byte diceHp;

	private String armor;
	private String weapon;
	private String tools;
	private String savingThrows;
	private String archetypeName;

	@Column(columnDefinition = "TEXT")
	private String equipment;

	/**
	 * Обратная сторона Spell.heroClass (таблица spells_hero_class). Раньше связь была
	 * независимой и жила в отдельной classes_spells, куда ничего не записывалось.
	 */
	@ManyToMany(mappedBy = "heroClass", fetch = FetchType.LAZY)
	@OrderBy("level")
	private List<Spell> spells;

	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private AbilityType spellAbility;

	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private SpellcasterType spellcasterType = SpellcasterType.NONE;

	@JdbcTypeCode(SqlTypes.JSON) // jsonb, см. db/changelog 0015
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<AbilityType> primaryAbilities = new ArrayList<>();

	@OneToMany()
	@JoinColumn(name = "hero_class_id")
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<HeroClassTrait> traits;

	private int enabledArhitypeLevel;

	@OneToMany()
	@JoinColumn(name = "class_id")
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<Archetype> archetypes;

	private short skillAvailableCount;

	@JdbcTypeCode(SqlTypes.JSON) // jsonb, см. db/changelog 0015
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<SkillType> availableSkills = new ArrayList<>();

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "option_type", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
	private OptionTypeDefinition optionType;

	@Enumerated(EnumType.STRING)
	private Rest slotsReset;
	private boolean sidekick;
	private String icon;
	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public String getAblativeName() {
		switch (name) {
		case "Чародей":
			return "чародеем";
		case "Изобретатель":
			return "изобретателем";
		default:
			return name + "ом";
		}
	}

	public String getGenitiveName() {
		// Названия классов обозначают одушевлённых персонажей мужского рода,
		// поэтому сохранённая винительная форма совпадает с родительной.
		if (accusativeName != null && !accusativeName.trim().isEmpty()) {
			return accusativeName.trim().toLowerCase();
		}
		switch (name) {
		case "Чародей":
			return "чародея";
		case "Изобретатель":
			return "изобретателя";
		case "Напарник боец":
			return "Напарника бойца";
		default:
			return name.toLowerCase() + "а";
		}
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}

	public String getCapitalazeName() {
		return StringUtil.capitalize(name.toLowerCase());
	}

	public List<HeroClassTrait> getTraits(int level) {
		if (traits == null) {
			return Collections.emptyList();
		}
		return traits.stream()
				.filter(t -> t.getLevel() == level)
				.collect(Collectors.toList());
	}

	public List<HeroClassTrait> getTraitsClear(int level) {
		if (traits == null) {
			return Collections.emptyList();
		}
		return traits.stream()
				.filter(t -> t.getLevel() == level)
				.filter(t -> !t.isArchitype())
				.collect(Collectors.toList());
	}

	public List<HeroClassTrait> getTraits() {
		if (traits == null) {
			return Collections.emptyList();
		}
		return traits.stream()
				.sorted(Comparator.comparingInt(HeroClassTrait::getLevel))
				.collect(Collectors.toList());
	}

	public List<ArchetypeTrait> getArhitypeTraitNames(int level){
		if (archetypes == null) {
			return Collections.emptyList();
		}
        return archetypes
                .stream()
                .flatMap(a -> a.getFeats().stream())
                .filter(t-> t.getLevel() == level)
                .collect(Collectors.toList());
	}

	public List<ArchetypeTrait> getArhitypeTraitNames(int archetypeId, int level){
		if (archetypes == null) {
			return Collections.emptyList();
		}
        return archetypes
                .stream()
                .filter(a -> a.getId() == archetypeId)
                .flatMap(a -> a.getFeats().stream())
                .filter(t-> t.getLevel() == level)
                .collect(Collectors.toList());
	}

	/** jsonb-колонки NOT NULL: «нет значений» хранится как [], а не NULL. */
	@PrePersist
	@PreUpdate
	private void nullJsonListsToEmpty() {
		levelDefenitions = levelDefenitions == null ? new ArrayList<>() : levelDefenitions;
		primaryAbilities = primaryAbilities == null ? new ArrayList<>() : primaryAbilities;
		availableSkills = availableSkills == null ? new ArrayList<>() : availableSkills;
	}
}
