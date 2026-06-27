package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.items.Weapon;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class WeaponDetailApi extends WeaponApi {
	private Integer id;
	private Integer cost;
	private String currency;
	private Float weight;
	private String typeRaw;
	private String damageDice;
	private String twoHandDamageDice;
	private Byte numberDice;
	private String damageType;
	private Short minDistance;
	private Short maxDistance;
	private Byte ammo;
	private String description;
	private String special;
	private List<PropertyApi> properties;
	private List<Integer> propertyIds;
	public WeaponDetailApi(Weapon weapon) {
		super(weapon);
		id = weapon.getId();
		url = null;
		cost = weapon.getCost();
		currency = weapon.getCurrency() == null ? null : weapon.getCurrency().name();
		weight = weapon.getWeight();
		typeRaw = weapon.getType().name();
		damageDice = weapon.getDamageDice() == null ? null : weapon.getDamageDice().name();
		twoHandDamageDice = weapon.getTwoHandDamageDice() == null ? null : weapon.getTwoHandDamageDice().name();
		numberDice = weapon.getNumberDice();
		damageType = weapon.getDamageType().name();
		minDistance = weapon.getMinDistance();
		maxDistance = weapon.getMaxDistance();
		ammo = weapon.getAmmo();
		description = weapon.getDescription();
		special = weapon.getSpecial();
		properties = weapon.getProperties()
				.stream()
				.map(property-> new PropertyApi(weapon, property))
				.collect(Collectors.toList());
		propertyIds = weapon.getProperties().stream().map(property -> property.getId()).collect(Collectors.toList());
	}
}
