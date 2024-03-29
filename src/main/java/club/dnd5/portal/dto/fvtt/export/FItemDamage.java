package club.dnd5.portal.dto.fvtt.export;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FItemDamage {
	private List<List<String>> parts =new ArrayList<>();
	private String versatile = "";

	public void addDamage(String formula, String damageType) {
		List<String> damage = new ArrayList<>(2);
		damage.add(formula);
		damage.add(damageType);
		parts.add(damage);
	}
}
