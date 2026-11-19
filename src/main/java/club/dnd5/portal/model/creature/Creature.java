package club.dnd5.portal.model.creature;

import club.dnd5.portal.model.*;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.ChallengeRating;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив имён, см. db/changelog 0007
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<ArmorType> armorTypes = new ArrayList<>();

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

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив имён, см. db/changelog 0007
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<Condition> immunityStates = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив имён, см. db/changelog 0007
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<DamageType> immunityDamages = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив имён, см. db/changelog 0007
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<DamageType> resistanceDamages = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив имён, см. db/changelog 0007
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<DamageType> vulnerabilityDamages = new ArrayList<>();

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
	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, см. db/changelog 0009
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<SavingThrow> savingThrows = new ArrayList<>();

	// навыки
	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, см. db/changelog 0009
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<Skill> skills = new ArrayList<>();

	@ManyToMany
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<Language> languages;
	@Column(columnDefinition = "TEXT")
	private String language;

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, см. db/changelog 0009
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<CreatureFeat> feats = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, у каждого существа своя копия, см. db/changelog 0011
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<Action> actions = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "creature_id")
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<CreatureSpell> spells;


	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TEXT")
	private String legendary;

	@Column(columnDefinition = "TEXT")
	private String reaction;

	@ManyToMany
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<CreatureRace> races;

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив, см. db/changelog 0009
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<Spellcater> spellcasters = new ArrayList<>();

	@JdbcTypeCode(SqlTypes.JSON) // jsonb-массив имён, см. db/changelog 0007
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<HabitatType> habitates = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "lair_id")
	private Lair lair;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	private Short page;

	/**
	 * Опыт существа всегда определяется уровнем опасности,
	 * поэтому пересчитываем его при любом сохранении, каким бы путём оно ни шло.
	 */
	@PrePersist
	@PreUpdate
	private void syncExpWithChallengeRating() {
		this.exp = ChallengeRating.getExp(challengeRating);
		// jsonb-колонки NOT NULL: «нет значений» хранится как [], а не NULL
		armorTypes = armorTypes == null ? new ArrayList<>() : armorTypes;
		immunityStates = immunityStates == null ? new ArrayList<>() : immunityStates;
		immunityDamages = immunityDamages == null ? new ArrayList<>() : immunityDamages;
		resistanceDamages = resistanceDamages == null ? new ArrayList<>() : resistanceDamages;
		vulnerabilityDamages = vulnerabilityDamages == null ? new ArrayList<>() : vulnerabilityDamages;
		habitates = habitates == null ? new ArrayList<>() : habitates;
		savingThrows = savingThrows == null ? new ArrayList<>() : savingThrows;
		skills = skills == null ? new ArrayList<>() : skills;
		feats = feats == null ? new ArrayList<>() : feats;
		spellcasters = spellcasters == null ? new ArrayList<>() : spellcasters;
		actions = actions == null ? new ArrayList<>() : actions;
	}

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
		if (countDiceHp == null || getDiceHp() == null) {
			if (suffixHP != null) {
				return String.format("%d %s", averageHp, suffixHP);
			}
			return String.format("%d", averageHp);
		}
		short constitutionBonus = getBonusHP();
		if (constitutionBonus == 0) {
			return String.format("%d%s", countDiceHp, getDiceHp().name());
		}
		return String.format("%d%s%s%d", countDiceHp, getDiceHp().name(), constitutionBonus >= 0 ? "+" : "-", Math.abs(constitutionBonus));
	}

	public short getAverageHp() {
		if (countDiceHp == null || size == null || size.getHitDice() == null) {
			return averageHp;
		}
		return (short) Math.floor(countDiceHp * size.getHitAverage() + getBonusHP());
	}

	public Dice getDiceHp() {
		if (size == null || size.getHitDice() == null) {
			return diceHp;
		}
		return size.getHitDice();
	}

	public Short getBonusHP() {
		if (countDiceHp == null) {
			return bonusHP;
		}
		return (short) (getConstitutionModifier() * countDiceHp);
	}

	private int getConstitutionModifier() {
		return (int) Math.floor((constitution - 10) / 2.0);
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

	@Override
	public String getDescriptionHtml() {
		return description;
	}
}
