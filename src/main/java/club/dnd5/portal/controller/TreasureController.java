package club.dnd5.portal.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Hidden
@Controller
public class TreasureController {
	private static final String BASE_URL = "https://ttg.club/treasures";

	@GetMapping("/treasures")
	public String getItems(Model model) {
		model.addAttribute("metaTitle", "Драгоценности и безделушки (Treasures) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Драгоценности и безделушки по D&D 5 редакции");
		model.addAttribute("menuTitle", "Драгоценности и безделушки");
		return "spa";
	}
}
