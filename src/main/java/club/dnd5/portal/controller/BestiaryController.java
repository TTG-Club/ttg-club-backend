package club.dnd5.portal.controller;

import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.BestiaryDatatableRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Hidden
@Controller
public class BestiaryController {
	private static final String BASE_URL = "https://ttg.club/bestiary";

	@Autowired
	private BestiaryDatatableRepository repository;

	@Autowired
	private ImageRepository imageRepo;

	@GetMapping("/bestiary")
	public String getCreatures(Model model) {
		model.addAttribute("metaTitle", "Бестиарий (Monster Manual) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Бестиарий - существа для D&D 5 редакции");
		model.addAttribute("menuTitle", "Бестиарий");
		return "spa";
	}

	@GetMapping("/bestiary/{name}")
	public String getCreature(Model model, @PathVariable String name, HttpServletRequest request) {
		Creature beast = repository.findByEnglishName(name.replace("_", " "));
		if (beast == null) {
			request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "404");
			return "forward: /error";
		}
		model.addAttribute("metaTitle", String.format("%s (%s) | Бестиарий D&D 5e", beast.getName(), beast.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, beast.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s (%s) - %s %s, %s с уровнем опасности %s", beast.getName(), beast.getEnglishName(), beast.getSizeName(), beast.getType().getCyrilicName(), beast.getAligment(), beast.getChallengeRating()));
		Collection<String> images = imageRepo.findAllByTypeAndRefId(ImageType.CREATURE, beast.getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Бестиарий");
		return "spa";
	}

	@GetMapping("/bestiary/fragment/{id:\\d+}")
	public String getCreatureFragmentById(Model model, @PathVariable Integer id) throws InvalidAttributesException {
		Creature creature = repository.findById(id).orElseThrow(InvalidAttributesException::new);
		model.addAttribute("creature", creature);
		Collection<String> images = imageRepo.findAllByTypeAndRefId(ImageType.CREATURE, creature.getId());
		model.addAttribute("images", images);
		return "fragments/creature :: view";
	}

	@GetMapping("/bestiary/description/{id:\\d+}")
	public String getCreatureDescription(Model model, @PathVariable Integer id) throws InvalidAttributesException {
		model.addAttribute("creature", repository.findById(id).orElseThrow(InvalidAttributesException::new));
		return "fragments/creature :: description";
	}
}
