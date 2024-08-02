package club.dnd5.portal.model.items;

import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.Name;
import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
@Getter
@Setter

@Entity
@Table(name = "weapons")
public class Weapon extends Name {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false, unique = true)
	private String url;

	private Integer cost;

	@Enumerated(EnumType.STRING)
	private Currency currency;

	private float weight;

	@Enumerated(EnumType.STRING)
	private Dice damageDice;

	@Enumerated(EnumType.STRING)
	private Dice twoHandDamageDice;

	private Byte numberDice;

	@Enumerated(EnumType.STRING)
	private DamageType damageType;

	@Enumerated(EnumType.STRING)
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
}
