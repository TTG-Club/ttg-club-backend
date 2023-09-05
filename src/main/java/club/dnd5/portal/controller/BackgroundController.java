package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.repository.datatable.BackgroundRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Hidden
@Controller
public class BackgroundController {
	private static final String BASE_URL = "https://ttg.club/backgrounds";

	private final BackgroundRepository backgroundRepository;

	@GetMapping("/backgrounds")
	public String getBackgrounds(Model model) {
		model.addAttribute("metaTitle", "Предыстории персонажей (Backgrounds) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Предыстории персонажей по D&D 5 редакции");
		model.addAttribute("menuTitle", "Предыстории");
		return "spa";
	}

	@GetMapping("/backgrounds/{name}")
	public String getBackGround(Model model, @PathVariable String name) {
		Background background = backgroundRepository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", background.getName() + " | Предыстории персонажей D&D 5e");
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL,  background.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s (%s) - предыстория персонажа по D&D 5 редакции", background.getName(), background.getEnglishName()));
		model.addAttribute("menuTitle", "Предыстории");
		return "spa";
	}
}
