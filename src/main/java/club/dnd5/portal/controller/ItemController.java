package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.items.Equipment;
import club.dnd5.portal.repository.datatable.ItemDatatableRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

@Hidden
@Controller
public class ItemController {
	private static final String BASE_URL = "https://ttg.club/items";

	@Autowired
	private ItemDatatableRepository repository;

	@GetMapping("/items")
	public String getItems(Model model) {
		model.addAttribute("metaTitle", "Снаряжение (Items) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Снаряжение и инструменты по D&D 5 редакции");
		model.addAttribute("menuTitle", "Снаряжение");
		return "spa";
	}

	@GetMapping("/items/{name}")
	public String getItem(Model model, @PathVariable String name, HttpServletRequest request) {
		Equipment item = repository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", item.getName() + " | Снаряжение D&D 5e");
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, item.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s (%s) снаряжение по D&D 5 редакции", item.getName(), item.getEnglishName()));
		model.addAttribute("menuTitle", "Снаряжение");
		return "spa";
	}

	@GetMapping("/items/fragment/{id:\\d+}")
	public String getMagicItemFragmentById(Model model, @PathVariable Integer id) throws InvalidAttributesException {
		model.addAttribute("item", repository.findById(id).orElseThrow(InvalidAttributesException::new));
		return "fragments/item :: view";
	}

	@GetMapping("/items/fragment/{name:[A-Za-z_,']+}")
	public String getMagicWeaponFragmentByName(Model model, @PathVariable String name) {
		model.addAttribute("item", repository.findByEnglishName(name.replace('_', ' ')));
		return "fragments/item :: view";
	}
}
