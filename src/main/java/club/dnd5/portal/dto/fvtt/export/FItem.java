package club.dnd5.portal.dto.fvtt.export;

import club.dnd5.portal.model.ArmorType;
import club.dnd5.portal.model.creature.Action;
import club.dnd5.portal.model.creature.CreatureFeat;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class FItem {
	private static Set<String> weapons = new HashSet<>();
	static {
		Collections.addAll(weapons, "меч", "алебарда", "безоружная атака", "бодание", "боевая кирка", "боевой молот",
			"боевой посох", "боевой топор", "булава", "вилы", "глефа", "гарпун", "двуручный меч", "длинный лук",
			"длинный меч", "дротик", "дубина", "дубинка", "жало", "камень", "кинжал", "кнут", "клешни", "клешня",
			"клюв", "касание", "укус", "коготь", "коготи", "когти", "копыта", "хвост", "атака хвостом",
			"атака крыльями", "копье", "короткий лук", "короткий меч", "кувалда", "кулак", "ложноножка",
			"лёгкий арбалет", "метательное копьё", "копьё", "копье", "молот", "моргенштерн", "мушкет", "палица",
			"посох", "праща", "размашистый удар", "щупальце", "щупальца", "трезубец");
	}

	// private String _id;
	private String name;
	private String type;
	private String img;
	private FItemSystem system;
	public List<FEffect> effects = new ArrayList<>();

	public FItem(CreatureFeat feat) {
		name = feat.getName();
		type = "feat";
		system = new FItemSystem(feat);
	}

	public FItem(Action action) {
		name = action.getName();
		if (weapons.contains(action.getName().toLowerCase())) {
			type = "weapon";
		} else {
			type = "feat";
		}
		system = new FItemSystem(action);
	}

	public FItem(ArmorType armor) {
		name = armor.getCyrillicName();
		system = new FItemSystem(armor);
		type = "equipment";
		img = armor.getFvttIcon();
	}
}
