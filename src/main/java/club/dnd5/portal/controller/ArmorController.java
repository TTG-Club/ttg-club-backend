package club.dnd5.portal.controller;

import club.dnd5.portal.model.items.Armor;
import club.dnd5.portal.repository.datatable.ArmorDatatableRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Hidden
@Controller
public class ArmorController {
	private static final String BASE_URL = "https://ttg.club/armors";

	@Autowired
	private ArmorDatatableRepository repository;

	@GetMapping("/armors")
	public String getArmors(Model model) {
		model.addAttribute("metaTitle", "Доспехи (Armors) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Доспехи по D&D 5 редакции");
		model.addAttribute("menuTitle", "Доспехи");
		return "spa";
	}

	@GetMapping("/armors/{name}")
	public String getArmor(Model model, @PathVariable String name, HttpServletRequest request) {
		Armor armor = repository.findByEnglishName(name.replace('_', ' '));
		if (armor == null) {
			request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, "404");
			return "forward: /error";
		}
		model.addAttribute("metaTitle", String.format("%s (%s) | Доспехи D&D 5e", armor.getName(), armor.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, armor.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s (%s) - доспехи по D&D 5 редакции", armor.getName(), armor.getEnglishName()));
		model.addAttribute("menuTitle", "Доспехи");
		return "spa";
	}

	@GetMapping("/armors/fragment/{id:\\d+}")
	public String getArmorFragmentById(Model model, @PathVariable Integer id) throws InvalidAttributesException {
		model.addAttribute("armor", repository.findById(id).orElseThrow(InvalidAttributesException::new));
		return "fragments/armor :: view";
	}

	@GetMapping("/armors/fragment/{name:[A-Za-z_]+}")
	public String getMagicWeaponFragmentByName(Model model, @PathVariable String name) throws InvalidAttributesException {
		model.addAttribute("armor", repository.findByEnglishName(name.replace('_', ' ')));
		return "fragments/armor :: view";
	}
}
