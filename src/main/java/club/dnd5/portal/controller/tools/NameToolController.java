package club.dnd5.portal.controller.tools;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;

@Hidden
@Controller
public class NameToolController {
	public static final Random rnd = new Random();

	@GetMapping("/tools/names/random")
	@ResponseBody
	public String getWildMagicRandomText(String type) {
		return "Не удалось сгенерировать имя";
	}
}
