package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.classes.ArchetypeSpellRepository;
import club.dnd5.portal.repository.datatable.SpellRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Hidden
@Controller
public class SpellController {
	private static final String BASE_URL = "https://ttg.club/spells";

	@Autowired
	private SpellRepository repository;
	@Autowired
	private ArchetypeSpellRepository archetypeSpellRepository;

	@GetMapping("/spells")
	public String getSpells(Model model) {
		model.addAttribute("metaTitle", "Заклинания (Spells) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Заклинания по D&D 5 редакции");
		model.addAttribute("menuTitle", "Заклинания");
		return "spa";
	}

	@GetMapping("/spells/{name}")
	public String getSpell(Model model, @PathVariable String name, HttpServletRequest request) {
		Spell spell = repository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s)", spell.getName(), spell.getEnglishName()) + " | Заклинания D&D 5e");
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, spell.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s %s, %s", (spell.getLevel() == 0 ? "Заговор" : spell.getLevel() + " уровень"), spell.getName(), spell.getSchool().getName()));
		model.addAttribute("metaImage", String.format("https://img.ttg.club/magic/%s.png", StringUtils.capitalize(spell.getSchool().name().toLowerCase())));
		model.addAttribute("menuTitle", "Заклинания");
		return "spa";
	}

	@GetMapping("/spells/fragment/{id}")
	public String getSpellFragmentById(Model model, @PathVariable Integer id) throws InvalidAttributesException {
		model.addAttribute("archetypes", archetypeSpellRepository.findAllBySpell(id));
		model.addAttribute("races", Collections.emptyList());
		model.addAttribute("spell", repository.findById(id).orElseThrow(InvalidAttributesException::new));
		return "fragments/spell :: view";
	}

	@GetMapping("/spells/id")
	public String getSpell(Model model, Integer id) throws InvalidAttributesException {
		model.addAttribute("archetypes", archetypeSpellRepository.findAllBySpell(id));
		model.addAttribute("races", Collections.emptyList());
		model.addAttribute("spell", repository.findById(id).orElseThrow(InvalidAttributesException::new));
		return "fragments/spell :: view";
	}
}
