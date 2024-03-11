package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.spells.SpellLSS;
import club.dnd5.portal.service.LssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("api/v1/lss")
@RequiredArgsConstructor
public class LssApiController {
	private final LssService lssService;

	@GetMapping("spell/{name}")
	public SpellLSS findByName(@PathVariable String name) {
		return lssService.findByName(name);
	}

	@GetMapping("spell/all")
	public List<SpellLSS> getAllSpellForLSS() {
		return lssService.getAllSpellForLSS();
	}
}
