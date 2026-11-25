package club.dnd5.portal.model.races;

import club.dnd5.portal.model.mechanics.RaceMechanics;
import club.dnd5.portal.model.effect.ActiveEffect;
import club.dnd5.portal.model.*;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.races.RaceNickname.NicknameType;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter

@Entity
@Table(name = "races")
public class Race implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String altName;

	@Column(unique = true)
	private String englishName;
	private Integer minAge;
	private Integer maxAge;

	@OneToMany
	@JoinColumn(name = "race_id", insertable = false, updatable = false)
	@OrderBy("id") // порядок вставки, как в MySQL
	List<Feature> features;

	/** Разметка core-api, массив строк-блоков (см. db/changelog 0031). */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<String> description = new ArrayList<>();

	@OneToMany
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	List<Language> languages;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "parent_id")
	private Race parent;

	@OneToMany(mappedBy = "parent", orphanRemoval = true)
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<Race> subRaces;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "varchar(255) default 'MEDIUM'")
	private CreatureSize size;

	@Enumerated(EnumType.STRING)
	private CreatureType type;

	private Integer darkvision;

	@Column(columnDefinition = "int default 30")
	private int speed;
	private Integer fly;
	private Integer climb;
	private Integer swim;

	@OneToMany(mappedBy = "race")
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<RaceName> names;

	@OneToMany(mappedBy = "race", fetch = FetchType.LAZY)
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<RaceNickname> nicknames;
	private boolean view = true;

	/**
	 * Происхождение
	 */
	private Boolean origin = true;

	@OneToMany
	@JoinColumn(name = "race_id", insertable = false, updatable = false)
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<AbilityBonus> bonuses;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "race_spells", joinColumns = @JoinColumn(name = "race_id"))
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<Spell> spells;

	private String icon;

	/** Механика для листа персонажа (модель core-api); {@code null} — не заполнена. См. db/changelog 0044. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private RaceMechanics mechanics;

	/** Активные эффекты (модель Active Effects VTTG, как в core-api); {@code null} — нет. См. db/changelog 0044. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<ActiveEffect> activeEffects;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public String getFullName() {
		if (parent != null) {
			String parentName = parent.getName();
			return name.contains(parentName) ? name : name + " " + parentName;
		}
		return name;
	}

	public String getCapitalizeName() {
		return StringUtil.capitalize(name);
	}

	public String getAbilityBonuses() {
		if (bonuses.size() == 6) {
			return "+1 к каждой характеристике";
		}
		if (bonuses.isEmpty()) {
			return "—";
		}
		return bonuses.stream()
				.map(b -> {
					if (b.getAbility() == AbilityType.CHOICE_UNIQUE || b.getAbility() == AbilityType.CHOICE || b.getAbility() == AbilityType.ONE) {
						return String.format("%s %+d", b.getAbility().getCyrilicName(), b.getBonus());
					}
					else if (b.getAbility() == AbilityType.CHOICE_DOUBLE) {
						return String.format("%s", b.getAbility().getCyrilicName());
					} else {
						return String.format("%s %+d", b.getAbility().getShortName(), b.getBonus());
					}
				})
				.collect(Collectors.joining(", "));
	}

	public String getFullSpeed() {
		if (fly != null) {
			return String.format("%d фт., летая %d", speed, fly);
		}
		if (swim != null) {
			return String.format("%d фт., плавая %d", speed, swim);
		}
		if (climb != null) {
			return String.format("%d фт., лазая %d", speed, climb);
		}
		return String.format("%d фт.", speed);
	}

	public List<AbilityBonus> getAbilityValueBonuses() {
		if (bonuses == null) {
			return Collections.emptyList();
		}
		return bonuses;
	}

	public String getFullNameAbilityBonuses() {
		if (bonuses.size() == 6) {
			return "+1 к каждой характеристике";
		}
		return bonuses.stream()
				.map(b -> String.format("%s %+d", b.getAbility().getCyrilicName(), b.getBonus()))
				.collect(Collectors.joining(", "));
	}

	public String getCapName() {
		return StringUtil.capitalize(name.toLowerCase());
	}

	public boolean isDarkVision() {
		return features.parallelStream().map(Feature::getName).map(String::toLowerCase)
				.anyMatch(f -> f.contains("тёмное зрение") || f.contains("темное зрение"));
	}

	public boolean isNatureArmor() {
		return features.parallelStream().map(Feature::getName).map(String::toLowerCase)
				.anyMatch(f -> f.contains("природная броня") || f.contains("природный доспех"));
	}

	public Map<Sex, Set<String>> getNames() {
		if (names == null) {
			return Collections.emptyMap();
		}
		return names.stream().collect(Collectors.groupingBy(RaceName::getSex,
				Collectors.mapping(RaceName::getName, Collectors.toCollection(TreeSet::new))));
	}

	public List<RaceNickname> getAllNicknames() {
		Stream<RaceNickname> current = nicknames == null ? Stream.empty() : nicknames.stream();
		Stream<RaceNickname> parentNicknames = parent == null || parent.getNicknames() == null ? Stream.empty() : parent.getNicknames().stream();
		return Stream.concat(current, parentNicknames).collect(Collectors.toList());
	}

	public Map<Sex, Set<String>> getAllNames() {
		Stream<RaceName> current = names == null ? Stream.empty() : names.stream();
		Stream<RaceName> parentNames = parent == null || parent.names == null ? Stream.empty() : parent.names.stream();
		return Stream.concat(current, parentNames)
				.collect(Collectors.groupingBy(RaceName::getSex,
						Collectors.mapping(RaceName::getName, Collectors.toCollection(TreeSet::new))));
	}

	public Map<NicknameType, Set<String>> getNicknamesGroup() {
		if (nicknames == null) {
			return Collections.emptyMap();
		}
		return nicknames.stream()
				.collect(Collectors.groupingBy(RaceNickname::getType,
						Collectors.mapping(RaceNickname::getName, Collectors.toCollection(TreeSet::new))));
	}

	public String getCapitalazeName() {
		return StringUtil.capitalize(name.toLowerCase());
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}

	/** jsonb-колонка NOT NULL: пустое описание хранится как [], а не NULL. */
	@jakarta.persistence.PrePersist
	@jakarta.persistence.PreUpdate
	private void nullDescriptionToEmpty() {
		description = description == null ? new ArrayList<>() : description;
	}
}
