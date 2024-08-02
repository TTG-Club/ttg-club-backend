package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.screen.Screen;
import club.dnd5.portal.repository.datatable.ScreenRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Hidden
@Controller
public class ScreenController {
	private static final String BASE_URL = "https://ttg.club/screens";

	private final ScreenRepository repository;

	@GetMapping("/screens")
	public String getScreens(Model model) {
		model.addAttribute("metaTitle", "Ширма Мастера (Screens) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Ширма Мастера Подземелий и Драконов по D&D 5 редакции");
		model.addAttribute("menuTitle", "Ширма Мастера");
		return "spa";
	}

	@GetMapping("/screens/{name}")
	public String getScreen(Model model, @PathVariable String name) {
		Screen screen = repository.findByEnglishName(name.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaImage", screen.getIcon());
		model.addAttribute("metaTitle", String.format("%s (%s) | Ширма Мастера (Screens) D&D 5e", screen.getName(), screen.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, screen.getUrl()));
		model.addAttribute("metaDescription", String.format("%s (%s) Ширма Мастера Подземелий и Драконов по D&D 5 редакции", screen.getName(), screen.getEnglishName()));
		model.addAttribute("menuTitle", "Ширма Мастера");
		return "spa";
	}

	@GetMapping("/screens/{name}/{subscreen}")
	public String getSubscreenList(Model model, @PathVariable String name, @PathVariable String subscreen) {
		model.addAttribute("screens", repository.findAllByParentIsNullOrderByOrdering());
		model.addAttribute("selectedScreen", name);
		model.addAttribute("selectedSubscreen", subscreen);
		return "spa";
	}
}
