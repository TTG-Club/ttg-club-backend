package club.dnd5.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Controller
public class MainController {
	@GetMapping("/")
	public String getHome(Model model) {
		return "spa";
	}

	@GetMapping("/search")
	public String getSearch(Model model) {
		return "spa";
	}
}
