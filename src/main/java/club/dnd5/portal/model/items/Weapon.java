package club.dnd5.portal.model.items;

import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
@Getter
@Setter

@Entity
@Table(name = "weapons")
public class Weapon {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private String englishName;
	private String altName;
	private Integer cost;

	@Enumerated(EnumType.ORDINAL)
	private Currency currency;

	private float weight;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = true)
	private Dice damageDice;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = true)
	private Dice twoHandDamageDice;

	@Column(nullable = true)
	private Byte numberDice;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = true)
	private DamageType damageType;

	@Enumerated(EnumType.ORDINAL)
	private WeaponType type;

	@Column(nullable = true)
	private Short minDistance;

	@Column(nullable = true)
	private Short maxDistance;

	@ManyToMany
	List<WeaponProperty> properties;

	private Byte ammo;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TEXT")
	private String special;

	@ManyToOne
	@JoinColumn(name = "source")
	private Book book;

	public String getDamage() {
		if (damageDice == null && numberDice == null) {
			return null;
		}
		if (damageDice != null && numberDice == null) {
			return String.format("%s", damageDice.getName());

		}
		if (damageDice == null && numberDice != null) {
			return String.format("%s", numberDice);
		}
		return String.format("%d%s", numberDice, damageDice.getName());
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
