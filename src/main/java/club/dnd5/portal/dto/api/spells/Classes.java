package club.dnd5.portal.dto.api.spells;

import club.dnd5.portal.model.classes.HeroClass;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Classes {
	private List<FromClassList> fromClassList;

	public Classes(List<HeroClass> heroClass) {
		this.fromClassList = heroClass.stream().map(FromClassList::new).collect(Collectors.toList());
	}
}