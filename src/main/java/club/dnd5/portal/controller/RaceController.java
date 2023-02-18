package club.dnd5.portal.controller;

import club.dnd5.portal.model.AbilityType;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.races.Feature;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.classes.RaceRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Hidden
@Controller
public class RaceController {
	private static final String BASE_URL = "https://ttg.club/races";

	@Autowired
	private RaceRepository raceRepository;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private ImageRepository imageRepo;

	@GetMapping("/races")
	public String getRaces(Model model) {
		model.addAttribute("metaTitle", "Расы и происхождения (Races) D&D 5e");
		model.addAttribute("menuTitle", "Расы и происхождения");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Расы и происхождения персонажей по D&D 5 редакции");
		return "spa";
	}

	@GetMapping("/races/{name}")
	public String getRace(Model model, @PathVariable String name, HttpServletRequest request) {
		Optional<Race> race = raceRepository.findByEnglishName(name.replace('_', ' '));
		if (!race.isPresent()) {
			request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "404");
			return "forward: /error";
		}
		model.addAttribute("metaTitle", race.get().getName() + " | Расы и происхождения D&D 5e");
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, race.get().getUrlName()));
		model.addAttribute("metaDescription", String.format("%s - раса персонажа по D&D 5 редакции", race.get().getCapitalazeName()));
		Collection<String> images = imageRepo.findAllByTypeAndRefId(ImageType.RACE, race.get().getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Расы и происхождения");
		return "spa";
	}

	@GetMapping("/races/{raceEnglishName}/{subraceEnglishName}")
	public String getSubraceList(Model model, @PathVariable String raceEnglishName, @PathVariable String subraceEnglishName, HttpServletRequest request) {
		Optional<Race> race = raceRepository.findByEnglishName(raceEnglishName.replace('_', ' '));
		if (!race.isPresent()) {
			request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "404");
			return "forward: /error";
		}
		Optional<Race> subRace = race.get().getSubRaces()
			.stream()
			.filter(r -> r.getEnglishName().equalsIgnoreCase(subraceEnglishName.replace('_', ' ')))
			.findFirst();
		if (!subRace.isPresent()){
			request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "404");
			return "forward: /error";
		}
		model.addAttribute("metaTitle", String.format("%s | Расы и происхождения | Разновидности D&D 5e", subRace.get().getCapitalazeName()));
		model.addAttribute("metaUrl", String.format("%s/%s/%s", BASE_URL, race.get().getUrlName(), subRace.get().getUrlName()));
		model.addAttribute("metaDescription", String.format("%s - разновидность расы персонажа по D&D 5 редакции", subRace.get().getName()));
		model.addAttribute("menuTitle", "Расы и происхождения");
		Collection<String> images = imageRepo.findAllByTypeAndRefId(ImageType.RACE, subRace.get().getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Расы и происхождения");
		return "spa";
	}

	@GetMapping("/races/fragment/{id}")
	public String getFragmentRace(Model model, @PathVariable Integer id) {
		Race race = raceRepository.findById(id).get();
		List<Feature> features =  race.getFeatures().stream().filter(Feature::isFeature).collect(Collectors.toList());
		model.addAttribute("features", features);
		List<Feature> notFeatures =  race.getFeatures().stream().filter(Feature::isNotFeature).collect(Collectors.toList());
		model.addAttribute("notFeatures", notFeatures);
		model.addAttribute("race", race);
		model.addAttribute("selectedRaceName", "--- Выбор подрасы ---");
		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, race.getId());
		model.addAttribute("images", images);
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		return "fragments/race :: view";
	}

	@GetMapping("/races/{raceName}/subrace/{subraceName}")
	public String getFragmentSubraces(Model model, @PathVariable String raceName, @PathVariable String subraceName) {
		model.addAttribute("abilities", AbilityType.values());
		Race subRace = raceRepository.findBySubrace(raceName.replace("_", " "), subraceName.replace("_", " ")).orElseThrow(IllegalArgumentException::new);
		final Set<Integer> replaceFeatureIds = subRace.getFeatures().stream().map(Feature::getReplaceFeatureId).filter(Objects::nonNull).collect(Collectors.toSet());
		model.addAttribute("features",
				subRace.getParent().getFeatures()
				.stream()
				.filter(feature -> !replaceFeatureIds.contains(feature.getId()))
				.filter(feature -> feature.isFeature())
				.collect(Collectors.toList()));
		model.addAttribute("subFeatures", subRace.getFeatures().stream()
				.filter(f -> f.isFeature())
				.collect(Collectors.toList()));
		List<Feature> notFeatures =  subRace.getParent().getFeatures().stream().filter(Feature::isNotFeature).collect(Collectors.toList());
		model.addAttribute("notFeatures", notFeatures);
		model.addAttribute("race", subRace);
		model.addAttribute("selectedSubrace", subRace.getEnglishName());
		model.addAttribute("selectedRaceName", subRace.getName());

		Collection<String> images = imageRepository.findAllByTypeAndRefId(ImageType.RACE, subRace.getId());
		model.addAttribute("images", images);
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		return "fragments/race :: view";
	}

	@GetMapping("/races/{englishName}/subraces/list")
	public String getArchitypeList(Model model,@PathVariable String englishName) {
		Race race = raceRepository.findByEnglishName(englishName.replace("_", " ")).orElseThrow(IllegalArgumentException::new);
		model.addAttribute("images", imageRepository.findAllByTypeAndRefId(ImageType.RACE, race.getId()));
		model.addAttribute("race", race);
		model.addAttribute("subraces", race.getSubRaces());
		return "fragments/subraces_list :: sub_menu";
	}
}
