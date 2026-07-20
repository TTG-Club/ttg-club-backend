package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.model.DamageType;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.items.Currency;
import club.dnd5.portal.model.items.WeaponType;
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
}
