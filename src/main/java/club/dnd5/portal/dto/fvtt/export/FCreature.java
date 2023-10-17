package club.dnd5.portal.dto.fvtt.export;

import club.dnd5.portal.dto.fvtt.export.system.FSystem;
import club.dnd5.portal.dto.fvtt.export.token.FToken;
import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.creature.Creature;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FCreature {
	private String name;
	private String type;
	private String img;
	private FSystem system;
	private FToken token;
	private List<FItem> items = new ArrayList<>();
	private List<FEffect> effects = new ArrayList<>();

	public FCreature(Creature creature) {
		name = creature.getName();
		type = "npc";
		system = new FSystem(creature);
		img = String.format("https://5e.tools/img/%s/%s.png",
				creature.getBook().getSource(), StringUtils.capitalizeWords(creature.getEnglishName()));
		token = new FToken(creature);
		creature.getFeats().stream()
			.map(FItem::new)
			.forEach(i -> items.add(i));
		creature.getActions().stream()
			.map(FItem::new)
			.forEach(i -> items.add(i));
		creature.getArmorTypes().stream()
			.filter(t -> t != ArmorType.NATURAL)
			.forEach(a -> items.add(new FItem(a)));
	}
}
