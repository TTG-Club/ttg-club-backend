package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.model.items.Weapon;
import club.dnd5.portal.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UrlApi {
	private final String name;
	private String type;
	private String url;
	public UrlApi(ArmorType armorType) {
		name = armorType.getCyrillicName();
		type = "armor";
		if (armorType.getEnglishName() != null) {
			url = String.format("/armors/%s", StringUtil.getUrl(armorType.getEnglishName()));
		}
	}
	public UrlApi(Armor armor) {
		name = armor.getName().toLowerCase();
		type = "armor";
		url = String.format("/armors/%s", StringUtil.getUrl(armor.getEnglishName()));
	}

	public UrlApi(Weapon weapon) {
		name = weapon.getName().toLowerCase();
		type = "weapon";
		url = String.format("/weapons/%s", StringUtil.getUrl(weapon.getEnglishName()));
	}
}
