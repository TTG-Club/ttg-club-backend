package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.repository.datatable.ArmorRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Hidden
@Controller
public class ArmorController {
	private static final String BASE_URL = "https://ttg.club/armors";

	private final ArmorRepository armorRepository;

	@GetMapping("/armors")
	public String getArmors(Model model) {
		model.addAttribute("metaTitle", "Доспехи (Armors) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Доспехи по D&D 5 редакции");
		model.addAttribute("menuTitle", "Доспехи");
		return "spa";
	}

	@GetMapping("/armors/{url}")
	public String getArmor(Model model, @PathVariable String url) {
		Armor armor = armorRepository.findByUrl(url).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s) | Доспехи D&D 5e", armor.getName(), armor.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, armor.getUrl()));
		model.addAttribute("metaDescription", String.format("%s (%s) - доспехи по D&D 5 редакции", armor.getName(), armor.getEnglishName()));
		model.addAttribute("menuTitle", "Доспехи");
		return "spa";
	}
}
