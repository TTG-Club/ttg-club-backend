package club.dnd5.portal.model.creature;

import club.dnd5.portal.model.*;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.ChallengeRating;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Существо
 */
@Getter
@Setter
@Entity
@Table(name = "creatures")
public class Creature implements FoundryCommon {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false)
	private String name;
	private String altName;
	private String englishName;

	@Enumerated(EnumType.ORDINAL)
	private CreatureSize size;

	@Enumerated(EnumType.ORDINAL)
	private CreatureType type;

	private Integer raceId;
	private String raceName;

	@Enumerated(EnumType.ORDINAL)
	private Alignment alignment;
	private String alignmentSuffix;

	@Column(nullable = false)
	private byte AC;
	private String bonusAC;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	private List<ArmorType> armorTypes;

	private short averageHp;
	private Short countDiceHp;

	@Enumerated(EnumType.ORDINAL)
	private Dice diceHp;

	private Short bonusHP;

	private String suffixHP;

	private byte speed = 30;

	private Short flySpeed;

	private Short hover;

	private Short swimmingSpped;

	private Short climbingSpeed;

	private Short diggingSpeed;

	private String speedText;

	// Абилки
	@Column(nullable = false)
	private byte strength = 10;
	@Column(nullable = false)
	private byte dexterity = 10;
	@Column(nullable = false)
	private byte constitution = 10;
	@Column(nullable = false)
	private byte intellect = 10;
	@Column(nullable = false)
	private byte wizdom = 10;
	@Column(nullable = false)
	private byte charisma = 10;

	@ElementCollection
    @CollectionTable(name = "creature_immunity_states", joinColumns = @JoinColumn(name = "creature_id"))
	@Enumerated(EnumType.ORDINAL)
	private List<Condition> immunityStates;

	@ElementCollection
	@Enumerated(EnumType.ORDINAL)
	private List<DamageType> immunityDamages;

	@ElementCollection
	@Enumerated(EnumType.ORDINAL)
	private List<DamageType> resistanceDamages;

	@ElementCollection
	@Enumerated(EnumType.ORDINAL)
	private List<DamageType> vulnerabilityDamages;

	private Integer darkvision;
	private Integer trysight;
	private Integer vibration;
	private Integer blindsight;
	private Integer blindsightRadius;
	private byte passivePerception;
	private String passivePerceptionBonus;

	// опыт
	private int exp;
	// уровень опасности
	private String challengeRating;

	private String proficiencyBonus;

	// спаброски
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "creature_id")
	private List<SavingThrow> savingThrows;

	// навыки
	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "creature_id")
	private List<Skill> skills;

	@ManyToMany
	private List<Language> languages;

	@OneToMany(cascade = CascadeType.ALL)
	private List<CreatureFeat> feats;

	@ManyToMany(cascade = CascadeType.ALL)
	private List<Action> actions;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "creature_id")
	private List<CreatureSpell> spells;


	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TEXT")
	private String legendary;

	@Column(columnDefinition = "TEXT")
	private String reaction;

	@ManyToMany
	private List<CreatureRace> races;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "creature_id")
	private List<Spellcater> spellcasters;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	private List<HabitatType> habitates;

	@OneToOne
	@JoinColumn(name = "lair_id")
	private Lair lair;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	private Short page;

	public String getSizeName() {
		return size.getSizeName(type);
	}

	public String getAligment() {
		if (alignmentSuffix != null) {
			return String.format("%s %s", alignmentSuffix, alignment.getName(type));
		}
		return alignment.getName(type);
	}

	public Alignment getAligmentRaw(){
		return alignment;
	}

	public String getHpFormula() {
		if (bonusHP == null && diceHp == null && suffixHP == null) {
			return String.format("%d", averageHp);
		}
		if (bonusHP == null && diceHp == null && suffixHP != null) {
			return String.format("%d %s", averageHp, suffixHP);
		}
		if (bonusHP == null) {
			return String.format("%d%s", countDiceHp, diceHp.name());
		}
		return String.format("%d%s%s%d", countDiceHp, diceHp.name(), bonusHP >= 0 ? "+" : "-", Math.abs(bonusHP));
	}

	public String getSense() {
		List<String> sense = new ArrayList<>(5);
		if (blindsight != null) {
			String blind = String.format("слепое зрение %d фт.", blindsight);
			if (blindsightRadius != null) {
				blind += " (слеп за пределами этого радиуса)";
			}
			sense.add(blind);
		}
		if (darkvision != null) {
			String darkvis = String.format("тёмное зрение %d фт.", darkvision);
			if (blindsightRadius != null) {
				darkvis += " (слеп за пределами этого радиуса)";
			}
			sense.add(darkvis);
		}
		if (trysight != null) {
			sense.add(String.format("истинное зрение %d фт.", trysight));
		}
		if (vibration != null) {
			sense.add(String.format("чувство вибрации %d фт.", vibration));
		}
		return String.join(", ", sense);
	}

	public List<Action> getActions(ActionType type){
		return actions.stream()
			.filter(a -> a.getActionType() == type)
			.collect(Collectors.toList());
	}

	public List<Action> getActions(){
 		return actions;
	}

	public Byte getSavingThrow(AbilityType abilityType) {
		return savingThrows.stream()
			.filter(st-> st.getAbility() == abilityType)
			.map(SavingThrow::getBonus)
			.findFirst().orElse(null);
	}

	public Byte getSkillBonus(SkillType skillType) {
		return skills.stream()
			.filter(st-> st.getType() == skillType)
			.map(Skill::getBonus)
			.findFirst()
			.orElse(null);
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}

	public String getProficiencyBonus() {
		if (Objects.isNull(proficiencyBonus)) {
			return ChallengeRating.getProficiencyBonus(challengeRating);
		}
		return proficiencyBonus;
	}
}
