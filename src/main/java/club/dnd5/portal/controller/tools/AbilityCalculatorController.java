package club.dnd5.portal.controller.tools;

import club.dnd5.portal.model.creature.HabitatType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AbilityCalculatorController {
	@GetMapping("/tools/ability-calc")
	public String getAbilityCalculator(Model model) {
		model.addAttribute("metaTitle", "Калькулятор характеристик");
		model.addAttribute("metaUrl", "https://ttg.club/tools/ability-calc");
		model.addAttribute("metaDescription", "Калькулятор характеристик персонажа. Roll, Point bay, Arrays");
		model.addAttribute("types", HabitatType.types());
		model.addAttribute("menuTitle", "Калькулятор характеристик");
		return "spa";
	}
}
