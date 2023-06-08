package club.dnd5.portal.model.items;

import java.util.Arrays;
import java.util.Random;

import club.dnd5.portal.dto.api.item.PriceApi;
import club.dnd5.portal.model.Dice;
import lombok.Getter;

@Getter
public enum Rarity {
	COMMON(100, "обычный", "обычная", "обычное"),
	UNCOMMON(400, "необычный", "необычная", "необычное"),
	RARE(4000, "редкий", "редкая", "редкое"),
	VERY_RARE(40_000, "очень редкий", "очень редкая", "очень редкое" ),
	LEGENDARY(200_000, "легендарный", "легендарная", "легендарное" ),
	ARTIFACT(1_500_000, "артефакт", "артефакт", "артефакт"),
	UNKNOWN(0, "редкость не определена", "редкость не определена", "редкость не определена"),
	VARIES(0, "редкость варьируется", "редкость варьируется", "редкость варьируется");

	private static final Random RND = new Random();
	private final String[] names;
	Rarity(int cost, String... names){
		baseCost = cost;
		this.names = names;
	}
	// базовая цена в золотых монетах
	private final int baseCost;

	public static Rarity parse(String value) {
		return Arrays.stream(values()).filter(f -> f.getCyrilicName().equals(value)).findFirst().orElseThrow(IllegalArgumentException::new);
	}

	public String getCyrilicName() {
		return names[0];
	}

	public String getFemaleName() {
		return names[1];
	}

	public String getMiddleName() {
		return names[2];
	}

	public  PriceApi getRandomPrice(int bonus) {
		return getRandomPrice(this, false, bonus);
	}

	public static PriceApi getRandomPrice(final MagicItem item) {
		return getRandomPrice(item, 0);
	}

	public static PriceApi getRandomPrice(final MagicItem item, int bonus) {
		return getRandomPrice(item.getRarity(), item.isConsumed() , bonus);
	}

	public static PriceApi getRandomPrice(final Rarity rarity, boolean consumed, int bonus) {
		PriceApi price = new PriceApi();
		float consumable = consumed ? 2 : 1;
		switch (rarity) {
			case COMMON:
				price.setDmg(String.format("%.0f", RND.nextInt(101) / consumable + bonus));
				break;
			case UNCOMMON:
				price.setDmg(String.format("%.0f", (101 + RND.nextInt(501)) / consumable + bonus));
				break;
			case RARE:
				price.setDmg(String.format("%.0f", (501 + RND.nextInt(5001)) / consumable + bonus));
				break;
			case VERY_RARE:
				price.setDmg(String.format("%.0f", (5001 + RND.nextInt(50_000)) / consumable + bonus));
				break;
			case LEGENDARY:
				price.setDmg(String.format("%.0f", (50_000 + RND.nextInt(200_000)) / consumable + bonus));
				break;
			default:
				break;
		}
		switch (rarity) {
			case COMMON:
				price.setXge(String.format("%.0f", (Dice.d6.roll() + 1) * 10 / consumable + bonus));
				break;
			case UNCOMMON:
				price.setXge(String.format("%.0f", (Dice.d6.roll() + 1) * 100 / consumable + bonus));
				break;
			case RARE:
				price.setXge(String.format("%.0f", Dice.d10.roll(2) * 1_000 / consumable + bonus));
				break;
			case VERY_RARE:
				price.setXge(String.format("%.0f",(Dice.d4.roll() + 1) * 10_000 /consumable + bonus));
				break;
			case LEGENDARY:
				price.setXge(String.format("%.0f", Dice.d6.roll(2) * 25_000 / consumable + bonus));
				break;
			default:
				break;
		}
		return price;
	}

	public String getShort() {
		switch (this) {
		case COMMON:
			return "O";
		case UNCOMMON:
			return "Н";
		case RARE:
			return "Р";
		case VERY_RARE:
			return "OР";
		case LEGENDARY:
			return "Л";
		case ARTIFACT:
			return "А";
		default:
			return "~";
		}
	}
}
