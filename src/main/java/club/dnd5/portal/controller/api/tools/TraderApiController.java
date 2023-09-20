package club.dnd5.portal.controller.api.tools;

import club.dnd5.portal.dto.api.NameValueApi;
import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.dto.api.item.MagicItemApi;
import club.dnd5.portal.dto.api.spell.SpellApi;
import club.dnd5.portal.dto.api.tools.RequestTraderApi;
import club.dnd5.portal.dto.api.tools.TraderApi;
import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.MagicThingTable;
import club.dnd5.portal.model.items.Rarity;
import club.dnd5.portal.model.items.Weapon;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.ItemMagicTableRepository;
import club.dnd5.portal.repository.datatable.MagicItemRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.repository.datatable.WeaponRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Утилиты", description = "API по генерации товаров у торговца")
@RequiredArgsConstructor
@RestController
public class TraderApiController {
	private static final Random rnd = new Random();

	private final MagicItemRepository magicItemRepo;
	private final SpellRepository spellRepo;
	private final WeaponRepository weaponRepo;
	private final ItemMagicTableRepository mtRepo;

	@GetMapping("/api/v1/tools/trader")
	public TraderApi getTrader(){
		TraderApi traderApi = new TraderApi();
		List<NameValueApi> magicLevels = new ArrayList<>(3);
		magicLevels.add(new NameValueApi("Мало", 0));
		magicLevels.add(new NameValueApi("Норма", 1));
		magicLevels.add(new NameValueApi("Много", 2));
		traderApi.setMagicLevels(magicLevels);
		traderApi.setSources(Arrays.asList(
			new SourceApi("DMG", "Руководство мастера"),
			new SourceApi("XGE", "Руководство Занатара обо всем")
		));
		return traderApi;
	}

	@PostMapping("/api/v1/tools/trader")
	public List<MagicItemApi> getItems(@RequestBody RequestTraderApi traderApi){
		int coef = 0;
		if (traderApi.getMagicLevel() == 0) {
			coef = -10;
		} else if (traderApi.getMagicLevel() == 2) {
			coef = 10;
		}
		if (traderApi.getUnique() == null) {
			traderApi.setUnique(Boolean.FALSE);
		}
		List<MagicItemApi> list = new ArrayList<>();
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 1, 5, "А", 6, traderApi.getUnique()));
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 6, 10, "Б", 4, traderApi.getUnique()));
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 11, 15, "В", 4, traderApi.getUnique()));
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 16, 20, "Г", 4, traderApi.getUnique()));
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 21, 25, "Д", 4, traderApi.getUnique()));
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 26, 30, "Е", 4, traderApi.getUnique()));
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 31, 35, "Е1", 4, traderApi.getUnique()));
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 36, 40, "Ж", 4, traderApi.getUnique()));
		list.addAll(getMagicItems(traderApi.getPersuasion() + coef, 41, 1000, "З", 4, traderApi.getUnique()));
		return list
			.stream()
			.sorted(Comparator.comparing(MagicItemApi::getRarity))
			.collect(Collectors.toList());
	}

	private List<MagicItemApi> getMagicItems(
		Integer persuasion,
		final int start,
		final int end,
		final String tableName,
		int count,
		final boolean unique) {
		if (persuasion == null) {
			persuasion = 1;
		}
		Set<String> names = new HashSet<>();
		List<MagicItemApi> list = new ArrayList<>();
		if (persuasion >= start) {
			for (int i = 0; i < 1 + rnd.nextInt(count); i++) {
				int ri = Dice.roll(Dice.d100);
				MagicThingTable mt = mtRepo.findOne(ri, tableName);
				if (mt != null) {
					MagicItemApi itemApi = new MagicItemApi(mt.getMagicThing());
					itemApi.setPrice(Rarity.getRandomPrice(mt.getMagicThing()));
					if (tableName.equals("Б")) {
						if (ri == 91) {
							int zap = Dice.d4.roll(4);
							itemApi.updateName("(дополнительных заплаток " + zap + ")");
						}
					}
					if (tableName.equals("В")) {
						if (ri >= 82 && ri <= 84) {
							String effect;
							int er = Dice.roll(Dice.d100);
							if (er <= 15) {
								effect = "веер";
							} else if (er <= 40) {
								effect = "дерево";
							} else if (er <= 50) {
								effect = "кнут";
							} else if (er <= 65) {
								effect = "лодка-лебедь";
							} else if (er <= 80) {
								effect = "птица";
							} else {
								effect = "якорь";
							}
							itemApi.updateName("(Эффект: " + effect + ")");
						} else if (ri >= 85 && ri <= 87) {
							String effect;
							int er = Dice.roll(Dice.d100);
							if (er <= 10) {
								effect = "от аберраций";
							} else if (er <= 20) {
								effect = "от зверей";
							} else if (er <= 45) {
								effect = "от исчадий";
							} else if (er <= 55) {
								effect = "от небожителей";
							} else if (er <= 75) {
								effect = "от нежити";
							} else if (er <= 80) {
								effect = "от растений";
							} else if (er <= 90) {
								effect = "от фей";
							} else {
								effect = "от элементалей";
							}
							itemApi.updateName("(Вид существ: " + effect + ")");
						} else if (ri >= 88 && ri <= 89) {
							itemApi.updateName(String.format("(Бобов: %d)", Dice.d4.roll(2)));
						}
					}
					if (tableName.equals("E")) {
						switch (ri) {
						case 66:
							itemApi.updateName("(кольчуга)");
							itemApi.setPrice(Rarity.getRandomPrice(mt.getMagicThing(), 75));
							break;
						case 67:
							itemApi.updateName("(кольчужная рубаха)");
							itemApi.setPrice(Rarity.getRandomPrice(mt.getMagicThing(),  50));
							break;
						case 68:
							itemApi.updateName("(чещуйчатый доспех)");
							itemApi.setPrice(Rarity.getRandomPrice(mt.getMagicThing(), 50));
							break;
						}
					}
					if (tableName.equals("E1")) {
						switch (ri) {
						case 15:
							itemApi.updateName("(кираса)");
							itemApi.setPrice(Rarity.getRandomPrice(mt.getMagicThing(), + 400));
							break;
						case 16:
							itemApi.updateName("(наборной доспех)");
							itemApi.setPrice(Rarity.getRandomPrice(mt.getMagicThing(), 200));
							break;
						case 35:
							itemApi.updateName("(кожаный)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(10));
							break;
						case 36:
							itemApi.updateName("(кольчуга)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(75));
							break;
						case 37:
							itemApi.updateName("(кольчужная рубаха)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(50));
							break;
						case 38:
							itemApi.updateName("(чешуйчатый)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(50));
							break;
						case 60:
							itemApi.updateName(getResistanceType());
							break;
						}
					}
					if (tableName.equals("Ж")) {
						switch (ri) {
						case 55:
							itemApi.updateName("(латы)");
							itemApi.setPrice(Rarity.getRandomPrice(mt.getMagicThing(), 1500));
							break;
						case 56:
							itemApi.updateName("(полулаты)");
							itemApi.setPrice(Rarity.getRandomPrice(mt.getMagicThing(), 750));
							break;
						case 65:
							itemApi.updateName("(кираса)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(400));
							break;
						case 66:
							itemApi.updateName("(наборный)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(200));
							break;
						case 67:
							itemApi.updateName("(проклёпанная кожа)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(400));
							break;
						case 68:
							itemApi.updateName("(кожаный)");
							itemApi.setRarity(Rarity.RARE);
							itemApi.setPrice(Rarity.RARE.getRandomPrice(10));
							break;
						case 69:
							itemApi.updateName("(кольчуга)");
							itemApi.setRarity(Rarity.RARE);
							itemApi.setPrice(Rarity.RARE.getRandomPrice(75));
							break;
						case 70:
							itemApi.updateName("(кольчужная рубаха)");
							itemApi.setRarity(Rarity.RARE);
							itemApi.setPrice(Rarity.RARE.getRandomPrice(50));
							break;
						case 71:
							itemApi.updateName("(чешуйчатый)");
							itemApi.setRarity(Rarity.RARE);
							itemApi.setPrice(Rarity.RARE.getRandomPrice(50));
							break;
						case 89:
							String alignment = Alignment.values()[rnd.nextInt(Alignment.values().length)]
									.getCyrilicName();
							itemApi.updateName("(Мировоззрение: " + alignment + ")");
							break;
						case 91:
							int rg = Dice.roll(Dice.d20);
							String golemType = "";
							if (rg >= 1 && rg <= 5)
								golemType = "Глинянный";
							else if (rg == 6)
								golemType = "Железный";
							else if (rg >= 7 && rg <= 8)
								golemType = "Каменный";
							else if (rg >= 9 && rg <= 20)
								golemType = "Мясной (из плоти)";
							itemApi.updateName(String.format("(%s)", golemType));
							break;
						}
					}
					if (tableName.equals("З")) {
						switch (ri) {
						case 42:
						case 43:
							itemApi.updateName("(латы)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(1_500));
							break;
						case 44:
						case 45:
							itemApi.updateName("(полулаты)");
							itemApi.setRarity(Rarity.UNCOMMON);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(750));
							break;
						case 46:
						case 47:
							itemApi.updateName("(чешуйчатый)");
							itemApi.setRarity(Rarity.RARE);
							itemApi.setPrice(Rarity.UNCOMMON.getRandomPrice(50));
							break;
						case 48:
						case 49:
							itemApi.updateName("(кираса)");
							itemApi.setRarity(Rarity.RARE);
							itemApi.setPrice(Rarity.RARE.getRandomPrice(400));
							break;
						case 50:
						case 51:
							itemApi.updateName("(наборной)");
							itemApi.setRarity(Rarity.RARE);
							itemApi.setPrice(Rarity.RARE.getRandomPrice(200));
							break;
						case 52:
						case 53:
							itemApi.updateName("(проклёпанная кожа)");
							itemApi.setRarity(Rarity.RARE);
							itemApi.setPrice(Rarity.RARE.getRandomPrice(45));
							break;
						case 54:
						case 55:
							itemApi.updateName("(кожаный)");
							itemApi.setRarity(Rarity.VERY_RARE);
							itemApi.setPrice(Rarity.VERY_RARE.getRandomPrice(10));
							break;
						case 56:
						case 57:
							itemApi.updateName("(кольчуга)");
							itemApi.setRarity(Rarity.VERY_RARE);
							itemApi.setPrice(Rarity.VERY_RARE.getRandomPrice(75));
							break;
						case 58:
						case 59:
							itemApi.updateName("(кольчужная рубаха)");
							itemApi.setRarity(Rarity.VERY_RARE);
							itemApi.setPrice(Rarity.VERY_RARE.getRandomPrice(50));
							break;
						case 76:
							switch (Dice.d12.roll()) {
							case 1:
							case 2:
								itemApi.updateName("(полулаты)");
								itemApi.setRarity(Rarity.RARE);
								itemApi.setPrice(Rarity.RARE.getRandomPrice(750));
								break;
							case 3:
							case 4:
								itemApi.updateName("(латы)");
								itemApi.setRarity(Rarity.RARE);
								itemApi.setPrice(Rarity.RARE.getRandomPrice(1_500));
								break;
							case 5:
							case 6:
								itemApi.updateName("(проклёпанная кожа)");
								itemApi.setRarity(Rarity.VERY_RARE);
								itemApi.setPrice(Rarity.VERY_RARE.getRandomPrice(45));
								break;
							case 7:
							case 8:
								itemApi.updateName("(кираса)");
								itemApi.setRarity(Rarity.VERY_RARE);
								itemApi.setPrice(Rarity.VERY_RARE.getRandomPrice(400));
								break;
							case 9:
							case 10:
								itemApi.updateName("(набороной)");
								itemApi.setRarity(Rarity.VERY_RARE);
								itemApi.setPrice(Rarity.VERY_RARE.getRandomPrice(200));
								break;
							case 11:
								itemApi.updateName("(полулаты)");
								itemApi.setRarity(Rarity.VERY_RARE);
								itemApi.setPrice(Rarity.VERY_RARE.getRandomPrice(750));
								break;
							case 12:
								itemApi.updateName("(латы)");
								itemApi.setRarity(Rarity.VERY_RARE);
								itemApi.setPrice(Rarity.VERY_RARE.getRandomPrice(1_500));
								break;
							}
							break;
						}
					}
					if (itemApi.getName().getRus().startsWith("Боеприпасы")) {
						int rb = Dice.d12.roll();
						if (rb <= 6) {
							itemApi.updateName("(стрелы)");
						} else if (rb < 9) {
							itemApi.updateName("(болты)");
						} else if (rb < 10) {
							itemApi.updateName("(дротики)");
						} else if (rb < 11) {
							itemApi.updateName("(снаряды для пращи)");
						}
					} else if (itemApi.getName().getRus().contains("Свиток заклинания")) {
						if (itemApi.getName().getRus().contains("заговор")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 0, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("1")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 1, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("2")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 2, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("3")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 3, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("4")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 4, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("5")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 5, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("6")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 6, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("7")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 7, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("8")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 8, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
						if (itemApi.getName().getRus().contains("9")) {
							List<Spell> spells = spellRepo.findByLevelAndBook_type((byte) 9, TypeBook.OFFICAL);
							Spell spell = spells.get(rnd.nextInt(spells.size()));
							itemApi.setSpell(new SpellApi(spell));
							itemApi.updateName(String.format("(%s)", spell.getName().toLowerCase()));
						}
					} else if (itemApi.getName().getRus().contains("Оружие")) {
						List<Weapon> weapons = weaponRepo.findAll();
						Weapon weapon = weapons.get(rnd.nextInt(weapons.size()));
						itemApi.updateName(" (" + weapon.getName().toLowerCase() + ")");
					} else if (tableName.equals("Е1") && ri >= 12 && ri <= 14) {
						switch (Dice.roll(Dice.d8)) {
						case 1:
							itemApi.updateName("(Бронзовый грифон)");
							break;
						case 2:
							itemApi.updateName("(Эбеновая муха)");
							break;
						case 3:
							itemApi.updateName("(Золотые львы)");
							break;
						case 4:
							itemApi.updateName("(Костяные козлы)");
							break;
						case 5:
							itemApi.updateName("(Мраморный слон)");
							break;
						case 6:
						case 7:
							itemApi.updateName("(Ониксовая собака)");
							break;
						case 8:
							itemApi.updateName("(Серпентиновая сова)");
							break;
						}
					} else if (itemApi.getName().getRus().contains("Зелье сопротивления")) {
						String resistType = getResistanceType();
						switch (resistType) {
						case "(звуку)":
							itemApi = new MagicItemApi(magicItemRepo.findById(86).get());
							break;
						case "(излучению)":
							itemApi = new MagicItemApi(magicItemRepo.findById(87).get());
							break;
						case "(кислоте)":
							itemApi = new MagicItemApi(magicItemRepo.findById(421).get());
							break;
						case "(огню)":
							itemApi = new MagicItemApi(magicItemRepo.findById(420).get());
							break;
						case "(некротической энергии)":
							itemApi = new MagicItemApi(magicItemRepo.findById(422).get());
							break;
						case "(психической энергии)":
							itemApi = new MagicItemApi(magicItemRepo.findById(423).get());
							break;
						case "(силовому полю)":
							itemApi = new MagicItemApi(magicItemRepo.findById(424).get());
							break;
						case "(холоду)":
							itemApi = new MagicItemApi(magicItemRepo.findById(425).get());
							break;
						case "(электричеству)":
							itemApi = new MagicItemApi(magicItemRepo.findById(426).get());
							break;
						case "(яду)":
							itemApi = new MagicItemApi(magicItemRepo.findById(896).get());
							break;
						}
					} else if (itemApi.getName().getRus().contains("Доспех cопротивления")) {
						itemApi.updateName(getResistanceType());
					}
					else if (itemApi.getName().getRus().contains("Камень элементаля")) {
						switch(Dice.d4.roll()) {
						case 1:
							itemApi.updateName("Изумруд	(элементаль воды)");
							break;
						case 2:
							itemApi.updateName("Синий сапфир элементаль воздуха");
							break;
						case 3:
							itemApi.updateName("Жёлтый бриллиант (элементаль земли)");
							break;
						case 4:
							itemApi.updateName("Красный корунд (элементаль огня)");
							break;
						}

					} else if (itemApi.getName().getRus().contains("Свиток защиты")) {
						int roll = Dice.d100.roll();
						String effect;
						if (roll <= 10) {
							effect = "от аберраций";
						} else if (roll <= 20) {
							effect = "от зверей";
						} else if (roll <= 45) {
							effect = "от исчадий";
						} else if (roll <= 55) {
							effect = "от небожителей";
						} else if (roll <= 75) {
							effect = "от нежити";
						} else if (roll <= 80) {
							effect = "от растений";
						} else if (roll <= 90) {
							effect = "от фей";
						} else {
							effect = "от элементалей";
						}
						itemApi.updateName(effect);
					}
					if (unique) {
						if (names.contains(itemApi.getName().getRus())) {
							count++;
							continue;
						}
					}
					list.add(itemApi);
					names.add(itemApi.getName().getRus());
				}
			}
		}
		return list;
	}

	private String getResistanceType() {
		switch (Dice.d10.roll()) {
		case 1:
			return "(звуку)";
		case 2:
			return "(излучению)";
		case 3:
			return "(кислоте)";
		case 4:
			return "(некротической энергии)";
		case 5:
			return "(огню)";
		case 6:
			return "(психической энергии)";
		case 7:
			return "(силовому полю)";
		case 8:
			return "(холоду)";
		case 9:
			return "(электричеству)";
		case 10:
			return "(яду)";
		}
		return "";
	}
}
