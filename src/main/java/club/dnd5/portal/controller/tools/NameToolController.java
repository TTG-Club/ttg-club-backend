package club.dnd5.portal.controller.tools;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import club.dnd5.portal.repository.datatable.NameRepository;
//import club.dnd5.portal.repository.datatable.NicknameRepository;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Controller
public class NameToolController {
	public static final Random rnd = new Random();
	
	@Autowired
	private NameRepository nameRepo;
	//@Autowired
	//private NicknameRepository nicknameRepo;

	
	@GetMapping("/tools/names")
	public String getTreasuryTool(Model model) {
		model.addAttribute("raceNames", nameRepo.findAllRaces());
		model.addAttribute("metaTitle", "Генератор имен");
		model.addAttribute("metaUrl", "https://ttg.club/tools/names");
		model.addAttribute("metaDescription", "Генерация имен, фамилий, прозвищ");
		return "tools/names";
	}
	
	@GetMapping("/tools/names/random")
	@ResponseBody
	public String getWildMagicRandomText(String type) {
		return "Не удалось сгенерировать имя";
	}
}