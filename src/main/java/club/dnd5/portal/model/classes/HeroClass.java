package club.dnd5.portal.model.classes;

import club.dnd5.portal.model.*;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;
import club.dnd5.portal.model.splells.Spell;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter

@Entity
@Table(name = "classes")
public class HeroClass extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

	@Column(columnDefinition = "TEXT")
	private String description;

	@OneToMany()
	@JoinColumn(name = "hero_class_id")
	private List<SpellLevelDefinition> levelDefenitions;

	@OneToMany()
	@JoinColumn(name = "hero_class_id")
	private List<FeatureLevelDefinition> featureLevelDefenitions;

	private byte diceHp;

	private String armor;
	private String weapon;
	private String tools;
	private String savingThrows;
	private String archetypeName;

	@Column(columnDefinition = "TEXT")
	private String equipment;

	@ManyToMany(fetch = FetchType.LAZY)
	@OrderBy("level")
	private List<Spell> spells;

	@Enumerated(EnumType.STRING)
	private AbilityType spellAbility;

	@Enumerated(EnumType.STRING)
	private SpellcasterType spellcasterType = SpellcasterType.NONE;

	@ElementCollection(targetClass = AbilityType.class)
	@JoinTable(name = "class_primary_abilities", joinColumns = @JoinColumn(name = "class_id"))
	@Column(name = "ability", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<AbilityType> primaryAbilities;

	@OneToMany()
	@JoinColumn(name = "hero_class_id")
	private List<ClassFeature> traits;

	private int enabledArhitypeLevel;

	@OneToMany()
	@JoinColumn(name = "class_id")
	private List<Archetype> archetypes;

	private short skillAvailableCount;

	@ElementCollection(targetClass = SkillType.class)
	@JoinTable(name = "class_available_skills", joinColumns = @JoinColumn(name = "class_id"))
	@Column(name = "skill", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<SkillType> availableSkills;

	@Enumerated(EnumType.STRING)
	private Option.OptionType optionType;

	@Enumerated(EnumType.STRING)
	private Rest slotsReset;
	private boolean sidekick;
	private String icon;
	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public String getAblativeName() {
		switch (getName()) {
		case "Чародей":
			return "чародеем";
		case "Изобретатель":
			return "изобретателем";
		default:
			return getName() + "ом";
		}
	}

	public String getGenitiveName() {
		switch (getName()) {
		case "Чародей":
			return "чародея";
		case "Изобретатель":
			return "изобретателя";
		case "Напарник боец":
			return "Напарника бойца";
		default:
			return getName().toLowerCase() + "а";
		}
	}

	public List<ClassFeature> getTraits(int level) {
		return traits.stream()
				.filter(t -> t.getLevel() == level)
				.collect(Collectors.toList());
	}

	public List<ClassFeature> getTraitsClear(int level) {
		return traits.stream()
				.filter(t -> t.getLevel() == level)
				.filter(t -> !t.isArchitype())
				.collect(Collectors.toList());
	}

	public List<ClassFeature> getTraits() {
		return traits.stream()
				.sorted(Comparator.comparingInt(ClassFeature::getLevel))
				.collect(Collectors.toList());
	}

	public List<ArchetypeTrait> getArhitypeTraitNames(int level){
		List<ArchetypeTrait> levelArhitypeFeats = archetypes
				.stream()
				.flatMap(a -> a.getFeats().stream())
				.filter(t-> t.getLevel() == level)
				.collect(Collectors.toList());
		return levelArhitypeFeats;
	}

	public List<ArchetypeTrait> getArhitypeTraitNames(int archetypeId, int level){
		List<ArchetypeTrait> levelArhitypeFeats = archetypes
				.stream()
				.filter(a -> a.getId() == archetypeId)
				.flatMap(a -> a.getFeats().stream())
				.filter(t-> t.getLevel() == level)
				.collect(Collectors.toList());
		return levelArhitypeFeats;
	}
}
