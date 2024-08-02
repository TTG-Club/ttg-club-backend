package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.items.Weapon;
import club.dnd5.portal.repository.datatable.WeaponPropertyDatatableRepository;
import club.dnd5.portal.repository.datatable.WeaponRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Hidden
@Controller
public class WeaponController {
	private static final String BASE_URL = "https://ttg.club/weapons";

	private final WeaponRepository repository;
	private final WeaponPropertyDatatableRepository propertyRepository;

	@GetMapping("/weapons")
	public String getWeapons(Model model) {
		model.addAttribute("metaTitle", "Оружие (Weapons) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Оружие по D&D 5 редакции");
		model.addAttribute("menuTitle", "Оружие");
		return "spa";
	}

	@GetMapping("/weapons/{name}")
	public String getWeapon(Model model, @PathVariable String name) {
		Weapon weapon = repository.findByEnglishName(name.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s) | Оружие D&D 5e", weapon.getName(), weapon.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, weapon.getUrl()));
		model.addAttribute("metaDescription", String.format("%s (%s) - %s D&D 5 редакции", weapon.getName(), weapon.getEnglishName(), weapon.getType().getName()));
		model.addAttribute("menuTitle", "Оружие");
		return "spa";
	}
}
