package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.trait.Trait;
import club.dnd5.portal.repository.datatable.FeatRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RequiredArgsConstructor
@Hidden
@Controller
public class FeatController {
	private static final String BASE_URL = "https://ttg.club/feats";

	private final FeatRepository repository;

	@GetMapping({"/traits", "/feats"})
	public String getTraits(Model model) {
		model.addAttribute("metaTitle", "Черты (Traits) D&D 5e");
		model.addAttribute("menuTitle", "Черты");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Список черт персонажей по D&D 5 редакции");
		return "spa";
	}

	@GetMapping({"/traits/{name}", "/feats/{name}"})
	public String getTrait(Model model, @PathVariable String name) {
		Trait trait = repository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s)", trait.getName(), trait.getEnglishName()) + " | Черты D&D 5e");
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, trait.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s (%s) - черта персонажа по D&D 5-редакции", trait.getName(), trait.getEnglishName()));
		model.addAttribute("menuTitle", "Черты");
		return "spa";
	}
}
