package club.dnd5.portal.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Hidden
@Controller
public class ConditionController {
	@GetMapping("/conditions")
	public String getConditions(Model model) {
		return "redirect:/screens/Conditions_and_disease";
	}

	@GetMapping("/conditions/{name}")
	public String getCondition(Model model, @PathVariable String name) {
		return "redirect:/screens/" + name;
	}
}
