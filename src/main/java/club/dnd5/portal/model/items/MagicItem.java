package club.dnd5.portal.model.items;

import club.dnd5.portal.model.mechanics.MagicItemMechanics;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.HeroClass;
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
@Table(name = "artifactes")
public class MagicItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private String englishName;
	private String altName;

	@Enumerated(EnumType.ORDINAL)
	private Rarity rarity;

	@Enumerated(EnumType.ORDINAL)
	private MagicThingType type;

	private Boolean customization;

	/** Условие настройки («заклинателем»), разметка core-api (см. db/changelog 0041); {@code null} — нет. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> custSpecial;

	/** Уточнение типа («пара сапог»), разметка core-api (см. db/changelog 0041); {@code null} — нет. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> special;

	/** Разметка core-api: массив строк-блоков (jsonb, см. db/changelog 0027/0028). */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private List<String> description = new ArrayList<>();
	private boolean consumed;
	private Integer charge;

	private Boolean curse;

	@Column
	private Integer cost;
	private Byte bonus;

	@OneToMany
	@JoinTable(name = "artifactes_cust_classes")
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<HeroClass> custClasses;

	@OneToMany
	@JoinTable(name = "artifactes_weapons")
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<Weapon> weapons;

	@OneToMany
	@JoinTable(name = "artifactes_armors")
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	private List<Armor> armors;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "magic_thing_id")
	@OrderBy("id") // порядок вставки, как в MySQL
	private List<MagicThingTable> tables;

	/** Механика для листа персонажа (модель core-api); {@code null} — не заполнена. См. db/changelog 0044. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private MagicItemMechanics mechanics;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;
	private Short page;

	public int getCost() {
		if (cost != null) {
			return cost;
		}
		return consumed ? rarity.getBaseCost() / 2 : rarity.getBaseCost();
	}

	public Integer getRawCost() {
		return cost;
	}

	public String getCapitalazeName() {
		return StringUtil.capitalize(name.toLowerCase());
	}

	@Override
	public String toString() {
		return name.toLowerCase();
	}

	public String getRangeCostDMG() {
		switch (rarity) {
		case COMMON:
			if (isConsumed()) {
				return "от 25 до 50 зм.";
			} else {
				return "от 50 до 100 зм.";
			}
		case UNCOMMON:
			if (isConsumed()) {
				return "от 51 до 250 зм.";
			} else {
				return "от 101 до 500 зм.";
			}
		case RARE:
			if (isConsumed()) {
				return "от 251 до 2 500 зм";
			} else {
				return "от 501 до 5 000 зм";
			}
		case VERY_RARE:
			if (isConsumed()) {
				return "от 2 501 до 25 000 зм.";
			} else {
				return "от 5 001 до 50 000 зм.";
			}
		case LEGENDARY:
			if (isConsumed()) {
				return "от 25 001 до 125 000 зм.";
			} else {
				return "от 50 001 до 250 000 зм.";
			}
		case ARTIFACT:
			return "от 250 001 зм. до невозможно купить";
		default:
			return Integer.toString(getCost());
		}
	}

	public String getRangeCostXGE() {
		switch (rarity) {
		case COMMON:
			return "(1к6 + 1) * 10";
		case UNCOMMON:
			return "(1к6) * 100";
		case RARE:
			return "2к10 * 1000";
		case VERY_RARE:
			return "(1к4 + 1) * 10000";
		case LEGENDARY:
			return "2к6 * 25000";
		case ARTIFACT:
			return "невозможно купить";
		default:
			return Integer.toString(getCost());
		}
	}

	public String getTextRarity() {
		switch (type) {
		case AMMUNITION:
		case WAND:
			return rarity.getFemaleName();
		case POTION:
		case RING:
		case WEAPON:
		case MELE_WEAPON:
		case RANGED_WEAPON:
			return rarity.getMiddleName();
		default:
			return rarity.getCyrilicName();
		}
	}

	public String getUrlName(){
		return StringUtil.getUrl(englishName);
	}

	/** jsonb-колонка NOT NULL: «нет описания» хранится как [], а не NULL. */
	@PrePersist
	@PreUpdate
	private void nullDescriptionToEmpty() {
		description = description == null ? new ArrayList<>() : description;
	}
}
