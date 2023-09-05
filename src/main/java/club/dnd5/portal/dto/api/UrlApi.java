package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.ArmorType;
import lombok.Getter;

@Getter
public class UrlApi {
	private String name;
	private String type;
	private String url;
	public UrlApi(ArmorType armorType) {
		name = armorType.getCyrillicName();
		type = "armor";
		if (armorType.getEnglishName() != null) {
			url = String.format("/armors/%s", armorType.getEnglishName());
		}
	}
}
