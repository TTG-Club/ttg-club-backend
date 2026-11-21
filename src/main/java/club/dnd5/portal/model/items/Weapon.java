package club.dnd5.portal.model.items;

import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
	private Dice damageDice;

	@Enumerated(EnumType.ORDINAL)
	private Dice twoHandDamageDice;

	private Byte numberDice;

	@Enumerated(EnumType.ORDINAL)
	private DamageType damageType;

	@Enumerated(EnumType.ORDINAL)
	private WeaponType type;

	private Short minDistance;

	private Short maxDistance;

	@ManyToMany
	@OrderColumn(name = "sort_order") // порядок как в MySQL, см. db/pg-migration/load
	List<WeaponProperty> properties;

	private Byte ammo;

	/** Описание. Разметка core-api: массив строк-блоков (jsonb, см. db/changelog 0027/0028); NULL — нет. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> description;

	/** Особое свойство. Разметка core-api: массив строк-блоков (jsonb, см. db/changelog 0027/0028); NULL — нет. */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private List<String> special;

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
		if (damageDice == null) {
			return String.format("%s", numberDice);
		}
		return String.format("%d%s", numberDice, damageDice.getName());
	}

	public String getUrlName() {
		return StringUtil.getUrl(englishName);
	}
}
