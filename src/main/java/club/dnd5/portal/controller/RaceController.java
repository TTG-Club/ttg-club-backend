package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.RaceRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;

@RequiredArgsConstructor
@Hidden
@Controller
public class RaceController {
	private static final String BASE_URL = "https://ttg.club/races";

	private final RaceRepository raceRepository;
	private final ImageRepository imageRepository;


	@GetMapping("/races")
	public String getRaces(Model model) {
		model.addAttribute("metaTitle", "Расы и происхождения (Races) D&D 5e");
		model.addAttribute("menuTitle", "Расы и происхождения");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Расы и происхождения персонажей по D&D 5 редакции");
		return "spa";
	}

	@GetMapping("/races/{name}")
	public String getRace(Model model, @PathVariable String name) {
		Race race = raceRepository.findByEnglishName(name.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", race.getName() + " | Расы и происхождения D&D 5e");
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, race.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s - раса персонажа по D&D 5 редакции", race.getCapitalazeName()));
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, race.getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Расы и происхождения");
		return "spa";
	}

	@GetMapping("/races/{raceEnglishName}/{subraceEnglishName}")
	public String getSubraceList(Model model, @PathVariable String raceEnglishName, @PathVariable String subraceEnglishName) {
		Race race = raceRepository.findByEnglishName(raceEnglishName.replace('_', ' '))
			.orElseThrow(PageNotFoundException::new);
		Race subRace = race.getSubRaces()
			.stream()
			.filter(r -> r.getEnglishName().equalsIgnoreCase(subraceEnglishName.replace('_', ' ')))
			.findFirst().orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s | Расы и происхождения | Разновидности D&D 5e", subRace.getCapitalazeName()));
		model.addAttribute("metaUrl", String.format("%s/%s/%s", BASE_URL, race.getUrlName(), subRace.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s - разновидность расы персонажа по D&D 5 редакции", subRace.getName()));
		model.addAttribute("menuTitle", "Расы и происхождения");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, subRace.getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Расы и происхождения");
		return "spa";
	}
}
