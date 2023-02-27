package club.dnd5.portal.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Hidden
@Controller
public class UserController {
	@GetMapping("/reset/password")
	public String getResetPasswordForm(Model model, @RequestParam String token) {
		return "spa";
	}
}
