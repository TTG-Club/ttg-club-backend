package club.dnd5.portal.dto.api.item;

import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Weapon;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class WeaponApi {
	private NameApi name;
	protected String url;
	private Boolean homebrew;
	private TypeApi type;
	private DamageApi damage;
	private String price;
	private SourceApi source;

	public WeaponApi(Weapon weapon) {
		name = new NameApi(weapon.getName(), weapon.getEnglishName());
		url = String.format("/weapons/%s", StringUtil.getUrl(weapon.getEnglishName()));
		if (weapon.getBook().getType() == TypeBook.CUSTOM) {
			homebrew = Boolean.TRUE;
		}
		type = new TypeApi(weapon.getType().getName(), weapon.getType().ordinal());
		damage = new DamageApi(weapon.getDamage(), weapon.getDamageType().getCyrillicName());
		if (weapon.getCost() != null) {
			price = String.format("%d %s.", weapon.getCost(), weapon.getCurrency().getName());
		}
		source = new SourceApi(weapon.getBook());
	}
}
