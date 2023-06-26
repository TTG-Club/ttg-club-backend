package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.rule.Rule;
import club.dnd5.portal.repository.datatable.RuleRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.http.HttpServletRequest;

@Hidden
@Controller
public class RuleController {
	private static final String BASE_URL = "https://ttg.club/rules";

	@Autowired
	private RuleRepository ruleRepository;

	@GetMapping("/rules")
	public String getRules(Model model) {
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaTitle", "Правила и термины [Rules] D&D 5e");
		model.addAttribute("metaDescription", "Правила и термины [Rules] D&D 5e");
		model.addAttribute("menuTitle", "Правила и термины");
		return "spa";
	}

	@GetMapping("/rules/{name}")
	public String getRule(Model model, @PathVariable String name, HttpServletRequest request) {
		Rule rule = ruleRepository.findByEnglishName(name.replace('_', ' ')).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s | %s | Правила и термины [Rules] D&D 5e", rule.getName(), rule.getType()));
		model.addAttribute("metaDescription", String.format("%s (%s) Правила и термины по D&D 5 редакции", rule.getName(), rule.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, rule.getUrlName()));
		model.addAttribute("menuTitle", "Правила и термины");
		return "spa";
	}

	@GetMapping("/rules/fragment/{id}")
	public String getMagicRuleFragmentById(Model model, @PathVariable Integer id) throws InvalidAttributesException {
		model.addAttribute("rule", ruleRepository.findById(id).orElseThrow(InvalidAttributesException::new));
		return "fragments/rule :: view";
	}
}
