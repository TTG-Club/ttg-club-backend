package club.dnd5.portal.controller.tools;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Controller
public class ItemMagicBuyingController {
	@GetMapping("/tools/items/magic")
	public String getTreasuryTool(Model model) {
		model.addAttribute("metaTitle", "Боги");
		model.addAttribute("metaUrl", "https://ttg.club/tools/items/magic");
		model.addAttribute("metaDescription", "Генерация покупки магических предметов");
		return "tools/buying_magic_items";
	}
}