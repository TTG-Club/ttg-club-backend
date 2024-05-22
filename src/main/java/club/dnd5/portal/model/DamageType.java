package club.dnd5.portal.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum DamageType {
	FAIR("огонь", "огонь"), //0
	COLD("холод", "холод"), //1
	LIGHTNING("электричество","электричество"), //2
	POISON("яд","яд"),//3
	ACID("кислота","кислота"), //4
	SOUND("звук","звук"),//5
	NECTOTIC("некротическая энергия","некротическая энергия"),//6
	PSYCHIC("психическая энергия", "психическая энергия"),//7

	BLUDGEONING("дробящий","дробящий"), //8
	PIERCING ("колющий","колющий"),//9
	SLASHING ("рубящий","рубящий"),//10
	PHYSICAL("дробящий, колющий и рубящий урон от немагических атак", "физический"),//11

	NO_NOSILVER("дробящий, колющий и рубящий урон от немагических атак, а также от немагического оружия, которое при этом не посеребрено", "физический и не посеребрённое"), //12
	NO_DAMAGE("без урона","без урона"), //13
	RADIANT("излучение","излучение"), //14
	NO_ADMANTIT("дробящий, колющий и рубящий урон от немагических атак, а также от немагического оружия, которое при этом не изготовлено из адамантина", "физический и не адамантиновое"), //15
	PHYSICAL_MAGIC("дробящий, колющий и рубящий урон от магического оружия", "физический магический"), //16
	PIERCING_GOOD("колющий от магического оружия, используемого добрыми существами", "колющий магический (добро)"), //17
	MAGIC("урон от заклинаний", "урон от заклинаний"), //18
	DARK("дробящий, колющий и рубящий, пока находится в области тусклого света или тьмы", "физический в тусклом свете или тьме"), //19
	FORCE("силовое поле","силовое поле"), //20
	METAL_WEAPON("дробящий, колющий и рубящий урон от оружия из металла", "физический только металл"), //21
	VORPAL_SWORD("рубящий удар мечом головорубом", "рубящий головоруб"), //22

	NECROTIC("некротическая энергия","некротическая энергия"),
	THUNDER("звук","звук");

	private final String cyrillicName;
	private final String shortName;

	public static DamageType parse(String damageTypeString) {
		for (DamageType damageType : values()) {
			if (damageType.cyrillicName.equals(damageTypeString)) {
				return damageType;
			}
		}
		return null;
	}

	public static Set<DamageType> getVulnerability()
	{
		return EnumSet.of(
				BLUDGEONING,
				PIERCING,
				SLASHING,
				FAIR,
				COLD,
				LIGHTNING,
				POISON,
				ACID,
				SOUND,
				NECTOTIC,
				PSYCHIC,
				RADIANT,
				FORCE,
				PIERCING_GOOD);
	}

	public static Set<DamageType> getResistance()
	{
		return EnumSet.of(
				BLUDGEONING,
				PIERCING,
				SLASHING,
				FAIR,
				COLD,
				LIGHTNING,
				POISON,
				ACID,
				SOUND,
				NECTOTIC,
				RADIANT,
				PHYSICAL,
				NO_ADMANTIT,
				PHYSICAL_MAGIC,
				NO_NOSILVER,
				PSYCHIC,
				MAGIC,
				DARK,
				FORCE);
	}

	public static Set<DamageType> getImmunity()
	{
		return EnumSet.of(
				BLUDGEONING,
				PIERCING,
				SLASHING,
				FAIR,
				COLD,
				LIGHTNING,
				POISON,
				ACID,
				SOUND,
				NECTOTIC,
				RADIANT,
				PHYSICAL,
				NO_ADMANTIT,
				FORCE, PSYCHIC);
	}

	public static List<DamageType> getSpellDamage(){
		return Arrays.asList(
				NO_DAMAGE,
				BLUDGEONING,
				PIERCING,
				SLASHING,
				FAIR,
				COLD,
				LIGHTNING,
				POISON,
				ACID,
				SOUND,
				NECTOTIC,
				RADIANT,
				FORCE,
				PSYCHIC);
	}

	public static Set<DamageType> getWeaponDamage() {
		return EnumSet.of(
				BLUDGEONING,
				PIERCING,
				SLASHING,
				NO_DAMAGE);
	}
}
