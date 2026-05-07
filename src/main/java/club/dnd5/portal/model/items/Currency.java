package club.dnd5.portal.model.items;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Currency {
	MM("мм", 1f),
	SM("см", 10f),
	EM("эм", 50f),
	GM("зм", 100f),
	PM("пм", 1000f);
	
	private final String name;
	private final float coef;
}