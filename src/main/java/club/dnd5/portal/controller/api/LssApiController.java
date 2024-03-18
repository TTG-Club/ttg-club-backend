package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.spells.SpellLSS;
import club.dnd5.portal.service.LssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("api/v1/lss")
@RequiredArgsConstructor
public class LssApiController {
	private final LssService lssService;

	//@ResponseBody чтобы возвращало не html, a json
	@GetMapping("spell/{englishName}")
	public @ResponseBody String findByName(@PathVariable String englishName) {
		return lssService.findByName(englishName);
	}

	@GetMapping("spell/all")
	public List<SpellLSS> getAllSpellForLSS() {
		return lssService.getAllSpellForLSS();
	}
}
