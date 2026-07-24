package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.items.Currency;
import club.dnd5.portal.model.items.WeaponType;
import club.dnd5.portal.model.items.Weapon;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class WeaponSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	private Integer cost;
	private Currency currency;
	private Float weight;
	private Dice damageDice;
	private Dice twoHandDamageDice;
	private Byte numberDice;
	@NotNull
	private DamageType damageType;
	@NotNull
	private WeaponType type;
	private Short minDistance;
	private Short maxDistance;
	private List<Integer> properties;
	private Byte ammo;
	private String description;
	private String special;

	/** Аббревиатура книги-источника, например MM. Пусто — самодельный контент. */
	private String source;

	public WeaponSaveApi(Weapon weapon) {
		name = weapon.getName();
		englishName = weapon.getEnglishName();
		altName = weapon.getAltName();
		cost = weapon.getCost();
		currency = weapon.getCurrency();
		weight = weapon.getWeight();
		damageDice = weapon.getDamageDice();
		twoHandDamageDice = weapon.getTwoHandDamageDice();
		numberDice = weapon.getNumberDice();
		damageType = weapon.getDamageType();
		type = weapon.getType();
		minDistance = weapon.getMinDistance();
		maxDistance = weapon.getMaxDistance();
		properties = weapon.getProperties() == null ? java.util.Collections.emptyList()
			: weapon.getProperties().stream().map(property -> property.getId()).collect(java.util.stream.Collectors.toList());
		ammo = weapon.getAmmo();
		description = weapon.getDescription();
		special = weapon.getSpecial();
		source = weapon.getBook() == null ? null : weapon.getBook().getSource();
	}
}
