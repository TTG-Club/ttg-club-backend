package club.dnd5.portal.controller;

import club.dnd5.portal.repository.datatable.TreasureRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Hidden
@Controller
public class TreasureController {
	private static final String BASE_URL = "https://ttg.club/treasures";

	@Autowired
	private TreasureRepository repository;

	@GetMapping("/treasures")
	public String getItems(Model model) {
		model.addAttribute("metaTitle", "Драгоценности и безделушки (Treasures) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Драгоценности и безделушки по D&D 5 редакции");
		model.addAttribute("menuTitle", "Драгоценности и безделушки");
		return "spa";
	}

	@GetMapping("/treasures/{name}")
	public String getItem(Model model, @PathVariable String name) {
		return "spa";
	}
}
