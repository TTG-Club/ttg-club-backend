package club.dnd5.portal.controller.tools;

import club.dnd5.portal.model.creature.HabitatType;
import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.model.tavern.Atmosphere;
import club.dnd5.portal.model.tavern.TavernaName;
import club.dnd5.portal.model.tavern.TavernaPrefixName;
import club.dnd5.portal.model.tavern.TavernaType;
import club.dnd5.portal.repository.classes.ClassRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import club.dnd5.portal.repository.tavern.*;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Hidden
@Controller
public class TavernToolController {
	private static final Random rnd = new Random();
	private static final Set<HabitatType> habitats = EnumSet.of(HabitatType.SWAMP, HabitatType.CITY,
			HabitatType.MOUNTAIN, HabitatType.VILLAGE, HabitatType.UNDERGROUND, HabitatType.ARCTIC, HabitatType.WATERS,
			HabitatType.DESERT, HabitatType.GRASSLAND, HabitatType.FOREST, HabitatType.TROPICS);

	private final TavernaNameRepository nameRepo;
	private final TavernaPrefixNameRepository prefixRepo;
	private final RaceRepository raceRepo;
	private final AtmosphereRepoditory atmosphereRepo;
	private final TopicDiscussedRepository topicRepo;
	private final RandomEventRepository eventRepo;
	private final VisitorRepository visitorRepo;
	private final ClassRepository classRepo;
	private final TavernaDishRepository dishRepo;
	private final TavernaDrinkRepository drinkRepo;
	private final DrinkEffectsRepository drinkEffectRepo;

	private Set<String> generatedNames = new HashSet<>();

	@GetMapping("/tools/tavern")
	public String getForm(Model model) {
		model.addAttribute("metaTitle", "Генератор таверны");
		model.addAttribute("metaUrl", "https://ttg.club/tools/tavern");
		model.addAttribute("metaDescription", "Генерация таверны - название, атмосфера, список блюд и выпивки");
		return "tools/tavern";
	}

	@GetMapping("/tools/tavern/name")
	@ResponseBody
	public String getTreasuryTool(String tavernaType) {
		List<TavernaName> tavernaNames = nameRepo.findAll();
		List<TavernaPrefixName> prefixes = prefixRepo.findAll();
		TavernaType type;
		if (tavernaType == null) {
			type = TavernaType.values()[rnd.nextInt(TavernaType.values().length)];
		} else {
			type = TavernaType.valueOf(tavernaType);
		}
		String tavernName = null;
		do {
			int index = rnd.nextInt(tavernaNames.size());
			TavernaName tavernaName = tavernaNames.get(index);
			index = rnd.nextInt(prefixes.size());
			TavernaPrefixName prefix = prefixes.get(index);
			if (prefix.getObjectType() != null) {
				tavernaNames = tavernaNames.stream()
						.filter(n -> n.getObjectType() == prefix.getObjectType())
						.collect(Collectors.toList());
				index = rnd.nextInt(tavernaNames.size());
				tavernaName = tavernaNames.get(index);
			}
			int nameType = rnd.nextInt(100);
			if (nameType > 85) {
				tavernaNames = tavernaNames.stream().filter(n -> n.getNames() != null).collect(Collectors.toList());
				index = rnd.nextInt(tavernaNames.size());
				TavernaName name = tavernaNames.get(index);
				tavernName = type.getName() + " \"";
				switch (rnd.nextInt(5)) {
				case 0:
					tavernName += "Три ";
					break;
				case 1:
					tavernName += "Четыре ";
					break;
				default:
					tavernName += name.getSex() == Sex.FEMALE ? "Две " : "Два ";
					break;
				}
				tavernName += tavernaNames.get(index).getNames() + "\"";
			} else if (nameType > 70) {
				index = rnd.nextInt(tavernaNames.size());
				TavernaName tavernaName2 = tavernaNames.get(index);
				tavernName = type.getName() + " \"" + tavernaName.getName() + " и " + tavernaName2.getName() + "\"";
			} else if (nameType > 60) {
				index = rnd.nextInt(tavernaNames.size());
				TavernaName tavernaName2 = tavernaNames.get(index);
				tavernName = type.getName() + " \"" + prefix.getName(tavernaName.getSex()) + " " + tavernaName.getName()
						+ " и " + tavernaName2.getName() + "\"";
			} else {
				tavernName = type.getName() + " \"" + prefix.getName(tavernaName.getSex()) + " " + tavernaName.getName()
						+ "\"";
			}
		} while (generatedNames.contains(tavernName));
		if (generatedNames.size() < 500) {
			generatedNames.add(tavernName);
		} else {
			generatedNames.clear();
		}
		return tavernName;
	}

	@GetMapping("/tools/tavern/habitates/")
	public String getHabitats(Model model) {
		//model.addAttribute("selected",  habitats.get(rnd.nextInt(habitats.size())));
		model.addAttribute("habitates", habitats);
		return "tools/tavern :: habitates";
	}

	@GetMapping("/tools/tavern/atmosphere/")
	@ResponseBody
	public String getAtmosphere() {
		List<Atmosphere> atmospheres = atmosphereRepo.findAll();
		Atmosphere atmosphere = atmospheres.get(rnd.nextInt(atmospheres.size()));
		return "<h5>"+atmosphere.getName() + "</h5> <br>" + atmosphere.getDescription();
	}
}
