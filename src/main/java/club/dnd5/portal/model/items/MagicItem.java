package club.dnd5.portal.model.items;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.thymeleaf.util.StringUtils;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.classes.HeroClass;
import lombok.Getter;
import lombok.Setter;

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

	@Column
	private String custSpecial;

	@Column
	private String special;

	@Column(columnDefinition = "TEXT")
	private String description;
	private boolean consumed;
	private Integer charge;

	@Column
	private Integer cost;
	private Byte bonus;

	@OneToMany
	@JoinTable(name = "artifactes_cust_classes")
	private List<HeroClass> custClasses;

	@OneToMany
	@JoinTable(name = "artifactes_weapons")
	private List<Weapon> weapons;

	@OneToMany
	@JoinTable(name = "artifactes_armors")
	private List<Armor> armors;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "magic_thing_id")
	private List<MagicThingTable> tables;

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

	public int getBaseCost() {
		return cost;
	}

	public String getCapitalazeName() {
		return StringUtils.capitalize(name.toLowerCase());
	}

	@Override
	public String toString() {
		return name.toLowerCase();
	}

	public String getRangeCostDMG() {
		switch (rarity) {
		case COMMON:
			if (isConsumed()) {
				return "???? 25 ???? 50 ????.";
			} else {
				return "???? 50 ???? 100 ????.";
			}
		case UNCOMMON:
			if (isConsumed()) {
				return "???? 51 ???? 250 ????.";
			} else {
				return "???? 101 ???? 500 ????.";
			}
		case RARE:
			if (isConsumed()) {
				return "???? 251 ???? 2 500 ????";
			} else {
				return "???? 501 ???? 5 000 ????";
			}
		case VERY_RARE:
			if (isConsumed()) {
				return "???? 2 501 ???? 25 000 ????.";
			} else {
				return "???? 5 001 ???? 50 000 ????.";
			}
		case LEGENDARY:
			if (isConsumed()) {
				return "???? 25 001 ???? 125 000 ????.";
			} else {
				return "???? 50 001 ???? 250 000 ????.";
			}
		case ARTIFACT:
			return "???? 250 001 ????. ???? ???????????????????? ????????????";
		default:
			return Integer.toString(getCost());
		}
	}

	public String getRangeCostXGE() {
		switch (rarity) {
		case COMMON:
			return "(1??6 + 1) * 10";
		case UNCOMMON:
			return "(1??6 + 1) * 100";
		case RARE:
			return "2??10 * 1000";
		case VERY_RARE:
			return "(1??4 + 1) * 10000";
		case LEGENDARY:
			return "2??6 * 25000";
		case ARTIFACT:
			return "???????????????????? ????????????";
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
		return englishName.toLowerCase().replace(' ', '_');
	}
}
