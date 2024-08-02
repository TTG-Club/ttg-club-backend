package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.Option.OptionType;
import club.dnd5.portal.repository.datatable.OptionRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Hidden
@Controller
public class OptionController {
	private static final String BASE_URL = "https://ttg.club/options";

	private final OptionRepository repository;

	private final Map<String, String> classIcons = new HashMap<>();

	@GetMapping("/options")
	public String getOptions(Model model) {
		model.addAttribute("metaTitle", "Особенности классов (Options) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Список особенности классов и подкласов по D&D 5 редакции");
		model.addAttribute("menuTitle", "Особенности классов");
		return "spa";
	}

	@GetMapping("/options/{name}")
	public String getOption(Model model, @PathVariable String name) {
		Option option = repository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s)", option.getName(), option.getEnglishName()) + " | Особенности классов D&D 5e");
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, option.getUrl()));
		model.addAttribute("metaDescription",
				String.format("Описание особенности %s - %s",
						option.getOptionTypes().stream().map(OptionType::getDisplayName).collect(Collectors.joining()),
						option.getName()));
		model.addAttribute("menuTitle", "Особенности классов");
		return "spa";
	}
}
