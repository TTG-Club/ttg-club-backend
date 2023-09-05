package club.dnd5.portal.controller.api.tools;

import club.dnd5.portal.dto.api.item.ItemApi;
import club.dnd5.portal.dto.api.item.MagicItemApi;
import club.dnd5.portal.dto.api.spell.SpellApi;
import club.dnd5.portal.dto.api.tools.CoinsApi;
import club.dnd5.portal.dto.api.tools.RequestTreasuryApi;
import club.dnd5.portal.dto.api.tools.TreasuryApi;
import club.dnd5.portal.model.Alignment;
import club.dnd5.portal.model.Dice;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.*;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RequiredArgsConstructor
@Tag(name = "Tools", description = "The tools API")
@RestController
public class TreasuryApiController {
	public static final Random rnd = new Random();
	private final MagicItemRepository magicItemRepo;
	private final SpellRepository spellRepo;
	private final WeaponRepository weaponRepo;
	private final ItemMagicTableRepository mtRepo;
	private final TreasureRepository treasureRepo;

	@PostMapping("/api/v1/tools/treasury")
	public TreasuryApi getItems(@RequestBody RequestTreasuryApi request) {
		TreasuryApi treasuryApi = new TreasuryApi();
		if (request.getCr() == null) {
			request.setCr(Dice.d4.roll());
		}
		if (request.getUnique() == null) {
			request.setUnique(Boolean.FALSE);
		}
		if (request.getCoins() != null && request.getCoins()) {
			CoinsApi coins = new CoinsApi();
			treasuryApi.setCoins(coins);
			switch (request.getCr()) {
			case 1:
				coins.setCopper(Dice.d6.roll(6) * 100);
				coins.setSilver(Dice.d6.roll(3) * 100);
				coins.setGold(Dice.d6.roll(2) * 10);
				break;
			case 2:
				coins.setCopper(Dice.d6.roll(2) * 100);
				coins.setSilver(Dice.d6.roll(2) * 1000);
				coins.setGold(Dice.d6.roll(6) * 100);
				coins.setPlatinum(Dice.d6.roll(3) * 10);
				break;
			case 3:
				coins.setGold(Dice.d6.roll(4) * 1000);
				coins.setPlatinum(Dice.d6.roll(5) * 100);
				break;
			case 4:
				coins.setGold(Dice.d6.roll(12) * 1000);
				coins.setPlatinum(Dice.d6.roll(8) * 1000);
				break;
			default:
				break;
			}
		}
		if (request.getMagicItem() != null && request.getMagicItem()) {
			List<MagicItemApi> magicItems = new ArrayList<>();
			int ri = Dice.d100.roll();
			switch (request.getCr()) {
			case 1:
				if (ri >= 37 && ri <= 60) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "А", 6, request));
				} else if (ri >= 61 && ri <= 75) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Б", 4, request));
				} else if (ri >= 76 && ri <= 85) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "В", 4, request));
				} else if (ri >= 86 && ri <= 97) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Е", 4, request));
				} else if (ri >= 98 && ri <= 100) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Е1", 1, request));
				}
				break;
			case 2:
				if (ri >= 29 && ri <= 44) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "А", 6, request));
				} else if (ri >= 45 && ri <= 63) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Б", 4, request));
				} else if (ri >= 64 && ri <= 74) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "В", 4, request));
				} else if (ri >= 75 && ri <= 80) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Г", 1, request));
				} else if (ri >= 81 && ri <= 94) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Е", 4, request));
				} else if (ri >= 95 && ri <= 98) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Е1", 4, request));
				} else if (ri >= 96 && ri <= 100) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Ж", 1, request));
				}
				break;
			case 3:
				if (ri >= 16 && ri <= 29) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "А", 4, request));
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Б", 6, request));
				} else if (ri >= 30 && ri <= 50) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "В", 6, request));
				} else if (ri >= 51 && ri <= 66) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Г", 4, request));
				} else if (ri >= 67 && ri <= 74) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Д", 1, request));
				} else if (ri >= 75 && ri <= 82) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Е", 1, request));
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Е1", 4, request));
				} else if (ri >= 83 && ri <= 92) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Ж", 4, request));
				} else if (ri >= 93 && ri <= 100) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "З", 1, request));
				}
				break;
			case 4:
				if (ri >= 3 && ri <= 14) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "В", 8, request));
				} else if (ri >= 15 && ri <= 46) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Г", 6, request));
				} else if (ri >= 47 && ri <= 68) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Д", 6, request));
				} else if (ri >= 69 && ri <= 72) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Е1", 6, request));
				} else if (ri >= 73 && ri <= 80) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "Ж", 4, request));
				} else if (ri >= 81 && ri <= 100) {
					magicItems.addAll(getMagicItems(ri, 1, 1000, "З", 4, request));
				}
				break;
			default:
				break;
			}
			treasuryApi.setMagicItems(magicItems);
		}
		List<ItemApi> arts = new ArrayList<>();
		List<ItemApi> gems = new ArrayList<>();
		int ri = Dice.d100.roll();
		if (request.getCr() == 1) {
			if (ri >= 7 && ri <= 16) {
				gems.addAll(getTreasures(10, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 17 && ri <= 26) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 27 && ri <= 36) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 37 && ri <= 44) {
				gems.addAll(getTreasures(10, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 45 && ri <= 52) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 53 && ri <= 60) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 61 && ri <= 65) {
				gems.addAll(getTreasures(10, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 66 && ri <= 70) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 71 && ri <= 75) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 76 && ri <= 78) {
				gems.addAll(getTreasures(10, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 79 && ri <= 80) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 81 && ri <= 85) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 86 && ri <= 92) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 93 && ri <= 97) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 2, Dice.d6));
			} else if (ri >= 98 && ri <= 99) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri == 100) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 2, Dice.d6));
			}
		} else if (request.getCr() == 2) {
			if (ri >= 5 && ri <= 10) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 11 && ri <= 16) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 17 && ri <= 22) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 23 && ri <= 28) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 29 && ri <= 32) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 33 && ri <= 36) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 37 && ri <= 40) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 41 && ri <= 44) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 45 && ri <= 49) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 50 && ri <= 54) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 55 && ri <= 59) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 60 && ri <= 63) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 64 && ri <= 66) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 67 && ri <= 69) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 70 && ri <= 72) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 73 && ri <= 74) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 75 && ri <= 76) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 77 && ri <= 78) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 3, Dice.d6));
			} else if (ri == 79) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri == 80) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 81 && ri <= 84) {
				arts.addAll(getTreasures(25, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 85 && ri <= 88) {
				gems.addAll(getTreasures(50, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 89 && ri <= 91) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 92 && ri <= 94) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 95 && ri <= 96) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 97 && ri <= 98) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri == 99) {
				gems.addAll(getTreasures(100, TreasureType.GEM, 3, Dice.d6));
			} else if (ri == 100) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d6));
			}
		} else if (request.getCr() == 3) {
			if (ri >= 4 && ri <= 6) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 7 && ri <= 9) {
				arts.addAll(getTreasures(750, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 10 && ri <= 12) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 13 && ri <= 15) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 16 && ri <= 19) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 20 && ri <= 23) {
				arts.addAll(getTreasures(750, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 24 && ri <= 26) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 27 && ri <= 29) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 30 && ri <= 35) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 36 && ri <= 40) {
				arts.addAll(getTreasures(750, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 41 && ri <= 45) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 46 && ri <= 50) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 51 && ri <= 54) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 55 && ri <= 58) {
				arts.addAll(getTreasures(750, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 59 && ri <= 62) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 63 && ri <= 66) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 67 && ri <= 68) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 69 && ri <= 70) {
				arts.addAll(getTreasures(750, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 71 && ri <= 72) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 73 && ri <= 74) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 75 && ri <= 76) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 77 && ri <= 78) {
				arts.addAll(getTreasures(750, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 79 && ri <= 80) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 81 && ri <= 82) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 83 && ri <= 85) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 86 && ri <= 88) {
				arts.addAll(getTreasures(750, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 89 && ri <= 90) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 91 && ri <= 92) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 93 && ri <= 94) {
				arts.addAll(getTreasures(250, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 95 && ri <= 96) {
				arts.addAll(getTreasures(750, TreasureType.WORKS_OF_ART, 2, Dice.d4));
			} else if (ri >= 97 && ri <= 98) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 99 && ri <= 100) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			}
		} else if (request.getCr() == 4) {
			if (ri >= 3 && ri <= 5) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 6 && ri <= 8) {
				arts.addAll(getTreasures(2500, TreasureType.WORKS_OF_ART, 1, Dice.d10));
			} else if (ri >= 9 && ri <= 11) {
				arts.addAll(getTreasures(7500, TreasureType.WORKS_OF_ART, 1, Dice.d4));
			} else if (ri >= 12 && ri <= 14) {
				gems.addAll(getTreasures(5000, TreasureType.GEM, 1, Dice.d8));
			} else if (ri >= 15 && ri <= 22) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 23 && ri <= 30) {
				arts.addAll(getTreasures(2500, TreasureType.WORKS_OF_ART, 1, Dice.d10));
			} else if (ri >= 31 && ri <= 38) {
				arts.addAll(getTreasures(7500, TreasureType.WORKS_OF_ART, 1, Dice.d4));
			} else if (ri >= 39 && ri <= 46) {
				gems.addAll(getTreasures(5000, TreasureType.GEM, 1, Dice.d8));
			} else if (ri >= 47 && ri <= 52) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 53 && ri <= 58) {
				arts.addAll(getTreasures(2500, TreasureType.WORKS_OF_ART, 1, Dice.d10));
			} else if (ri >= 59 && ri <= 63) {
				arts.addAll(getTreasures(7500, TreasureType.WORKS_OF_ART, 1, Dice.d4));
			} else if (ri >= 64 && ri <= 68) {
				gems.addAll(getTreasures(5000, TreasureType.GEM, 1, Dice.d8));
			} else if (ri == 69) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri == 70) {
				arts.addAll(getTreasures(2500, TreasureType.WORKS_OF_ART, 1, Dice.d10));
			} else if (ri == 71) {
				arts.addAll(getTreasures(7500, TreasureType.WORKS_OF_ART, 1, Dice.d4));
			} else if (ri == 72) {
				gems.addAll(getTreasures(5000, TreasureType.GEM, 1, Dice.d8));
			} else if (ri >= 73 && ri <= 74) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 75 && ri <= 76) {
				arts.addAll(getTreasures(2500, TreasureType.WORKS_OF_ART, 1, Dice.d10));
			} else if (ri >= 77 && ri <= 78) {
				arts.addAll(getTreasures(7500, TreasureType.WORKS_OF_ART, 1, Dice.d4));
			} else if (ri >= 79 && ri <= 80) {
				gems.addAll(getTreasures(500, TreasureType.GEM, 1, Dice.d8));
			} else if (ri >= 81 && ri <= 85) {
				gems.addAll(getTreasures(1000, TreasureType.GEM, 3, Dice.d6));
			} else if (ri >= 86 && ri <= 90) {
				arts.addAll(getTreasures(2500, TreasureType.WORKS_OF_ART, 1, Dice.d10));
			} else if (ri >= 91 && ri <= 95) {
				arts.addAll(getTreasures(7500, TreasureType.WORKS_OF_ART, 1, Dice.d4));
			} else if (ri >= 96 && ri <= 100) {
				gems.addAll(getTreasures(5000, TreasureType.GEM, 1, Dice.d8));
			}
		}
		if (request.getArt() != null && request.getArt()) {
			treasuryApi.setArts(arts);
		}
		if (request.getGem() != null && request.getGem()) {
			treasuryApi.setGems(gems);
		}
		if (request.getTrinket() != null && request.getTrinket()) {
			List<Treasure> items = treasureRepo.findAllByTypeIn(EnumSet.range(TreasureType.BAUBLE, TreasureType.IDR_TRINKET));
			List<ItemApi> trinkets = new ArrayList<>();
			for (int i = 0; i < 1 + Dice.d12.roll(); i++) {
				trinkets.add(new ItemApi(items.get(rnd.nextInt(items.size()))));
			}
			treasuryApi.setTrinkets(trinkets);
		}
		return treasuryApi;
	}

	private List<MagicItemApi> getMagicItems(Integer result, int start, int end, String tableName, int count,
											 RequestTreasuryApi request) {
		Set<String> names = new HashSet<>();
		List<MagicItemApi> list = new ArrayList<>();
		if (result >= start) {
			for (int i = 0; i < 1 + rnd.nextInt(count); i++) {
				int ri = Dice.roll(Dice.d100);
				// System.out.println("table= " + tableName + " in " + ri);
				MagicThingTable mt = mtRepo.findOne(ri, tableName);
				if (mt != null) {
					MagicItemApi itemApi = new MagicItemApi(mt.getMagicThing());
					if (tableName.equals("Б")) {
						if (ri == 91) {
							int zap = Dice.roll(4, Dice.d4);
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
								effect = "Аберрации";
							} else if (er <= 20) {
								effect = "Звери";
							} else if (er <= 45) {
								effect = "Исчадия";
							} else if (er <= 55) {
								effect = "Небожителиь";
							} else if (er <= 75) {
								effect = "Нежить";
							} else if (er <= 80) {
								effect = "Растения";
							} else if (er <= 90) {
								effect = "Феи";
							} else if (er <= 100) {
								effect = "Элементали";
							} else {
								effect = "якорь";
							}
							itemApi.updateName("(Вид существ: " + effect + ")");
						} else if (ri >= 88 && ri <= 89) {
							itemApi.updateName(String.format("(Бобов: %d)", Dice.roll(2, Dice.d4)));
						}
					}
					if (tableName.equals("E")) {
						switch (ri) {
						case 66:
							itemApi.updateName("(кольчуга)");
							break;
						case 67:
							itemApi.updateName("(кольчужная рубаха)");
							break;
						case 68:
							itemApi.updateName("(чещуйчатый доспех)");
							break;
						}
					}
					if (tableName.equals("E1")) {
						switch (ri) {
						case 15:
							itemApi.updateName("(кираса)");
							break;
						case 16:
							itemApi.updateName("(наборной доспех)");
							break;
						case 35:
							itemApi.updateName("(кожаный)");
							itemApi.setRarity(Rarity.UNCOMMON);
							break;
						case 36:
							itemApi.updateName("(кольчуга)");
							itemApi.setRarity(Rarity.UNCOMMON);
							break;
						case 37:
							itemApi.updateName("(кольчужная рубаха)");
							itemApi.setRarity(Rarity.UNCOMMON);
							break;
						case 38:
							itemApi.updateName("(чешуйчатый)");
							itemApi.setRarity(Rarity.UNCOMMON);
							break;
						case 60:
							itemApi.updateName(getResistenceType());
							break;
						}
					}
					if (tableName.equals("Ж")) {
						switch (ri) {
						case 55:
							itemApi.updateName("(латы)");
							break;
						case 56:
							itemApi.updateName("(полулаты)");
							break;
						case 65:
						case 66:
							itemApi.updateName("(кираса)");
							itemApi.setRarity(Rarity.UNCOMMON);
							break;
						case 67:
							itemApi.updateName("(проклёпанная кожа)");
							itemApi.setRarity(Rarity.UNCOMMON);
							break;
						case 68:
							itemApi.updateName("(кожаный)");
							itemApi.setRarity(Rarity.RARE);
							break;
						case 69:
							itemApi.updateName("(кольчуга)");
							itemApi.setRarity(Rarity.RARE);
							break;
						case 70:
							itemApi.updateName("(кольчужная рубаха)");
							itemApi.setRarity(Rarity.RARE);
							break;
						case 71:
							itemApi.updateName("(чешуйчатый)");
							itemApi.setRarity(Rarity.RARE);
							break;
						case 89:
							String aligment = Alignment.values()[rnd.nextInt(Alignment.values().length)]
									.getCyrilicName();
							itemApi.updateName("(Мировоззрение: " + aligment + ")");
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
							break;
						case 44:
						case 45:
							itemApi.updateName("(полулаты)");
							itemApi.setRarity(Rarity.UNCOMMON);
							break;
						case 46:
						case 47:
							itemApi.updateName("(чешуйчатый)");
							itemApi.setRarity(Rarity.RARE);
							break;
						case 48:
						case 49:
							itemApi.updateName("(кираса)");
							itemApi.setRarity(Rarity.RARE);
							break;
						case 50:
						case 51:
							itemApi.updateName("(наборной)");
							itemApi.setRarity(Rarity.RARE);
							break;
						case 52:
						case 53:
							itemApi.updateName("(проклёпанная кожа)");
							itemApi.setRarity(Rarity.RARE);
							break;
						case 54:
						case 55:
							itemApi.updateName("(кожаный)");
							itemApi.setRarity(Rarity.VERY_RARE);
							break;
						case 56:
						case 57:
							itemApi.updateName("(кольчуга)");
							itemApi.setRarity(Rarity.VERY_RARE);
							break;
						case 58:
						case 59:
							itemApi.updateName("(кольчужная рубаха)");
							itemApi.setRarity(Rarity.VERY_RARE);
							break;
						case 76:
							switch (Dice.roll(Dice.d12)) {
							case 1:
							case 2:
								itemApi.updateName("(полулаты)");
								itemApi.setRarity(Rarity.RARE);
								break;
							case 3:
							case 4:
								itemApi.updateName("(латы)");
								itemApi.setRarity(Rarity.RARE);
								break;
							case 5:
							case 6:
								itemApi.updateName("(проклёпанная кожа)");
								itemApi.setRarity(Rarity.VERY_RARE);
								break;
							case 7:
							case 8:
								itemApi.updateName("(кираса)");
								itemApi.setRarity(Rarity.VERY_RARE);
								break;
							case 9:
							case 10:
								itemApi.updateName("(набороной)");
								itemApi.setRarity(Rarity.VERY_RARE);
								break;
							case 11:
								itemApi.updateName("(полулаты)");
								itemApi.setRarity(Rarity.VERY_RARE);
								break;
							case 12:
								itemApi.updateName("(латы)");
								itemApi.setRarity(Rarity.VERY_RARE);
								break;
							}
							break;
						}
					}
					if (itemApi.getName().getRus().startsWith("Боеприпасы")) {
						int rb = Dice.roll(Dice.d12);
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
						 if (!request.getScroll()) {
							 count++;
							 continue;
						 }
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
						String resistType = getResistenceType();
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
						itemApi.updateName(getResistenceType());
					} else if (itemApi.getName().getRus().contains("Камень элементаля")) {
						switch (Dice.roll(Dice.d4)) {
						case 1:
							itemApi.updateName("Изумруд (элементаль воды)");
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
					} else if (itemApi.getName().getRus().contains("Свиток защиты") && request.getScroll()) {
						int roll = Dice.d100.roll();
						if (roll <= 10) {
							itemApi.updateName("Свиток защиты (Аберрации)");
						} else if (roll >= 11 && roll <= 20) {
							itemApi.updateName("Свиток защиты (Звери)");
						} else if (roll >= 21 && roll <= 45) {
							itemApi.updateName("Свиток защиты (Исчадия)");
						} else if (roll >= 46 && roll <= 55) {
							itemApi.updateName("Свиток защиты (Небожители)");
						} else if (roll >= 56 && roll <= 75) {
							itemApi.updateName("Свиток защиты (Нежить)");
						} else if (roll >= 76 && roll <= 80) {
							itemApi.updateName("Свиток защиты (Растения)");
						} else if (roll >= 81 && roll <= 90) {
							itemApi.updateName("Свиток защиты (Феи)");
						} else {
							itemApi.updateName("Свиток защиты (Элементали)");
						}
					}
					if (request.getUnique()) {
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

	private String getResistenceType() {
		switch (Dice.roll(Dice.d10)) {
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

	private List<ItemApi> getTreasures(int cost, TreasureType type, int count, Dice dice) {
		List<ItemApi> treasures = new ArrayList<>();
		int ri = dice.roll(count);
		for (int i = 0; i < ri; i++) {
			List<Treasure> treasuresFind = treasureRepo.findAllByCostAndType(cost, type);
			treasures.add(new ItemApi(treasuresFind.get(rnd.nextInt(treasuresFind.size()))));
		}
		return treasures;
	}
}
