package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.god.God;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.GodRepository;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Hidden
@Controller
public class GodController {
	private static final String BASE_URL = "https://ttg.club/gods";

	@Autowired
	private GodRepository repository;

	@Autowired
	private ImageRepository imageRepo;

	@GetMapping("/gods")
	public String getGods(Model model) {
		model.addAttribute("metaTitle", "Боги (Gods) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Боги, полубоги и философии D&D 5 редакции");
		model.addAttribute("menuTitle", "Боги");
		return "spa";
	}

	@GetMapping("/gods/{name}")
	public String getGod(Model model, @PathVariable String name, HttpServletRequest request) {
		God god = repository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s) | Боги D&D 5e", god.getName(), god.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, god.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s (%s) - %s %s, %s", god.getName(), god.getEnglishName(), god.getAligment().getCyrilicName(), god.getSex().getCyrilicName(), god.getCommitment()));
		Collection<String> images = imageRepo.findAllByTypeAndRefId(ImageType.GOD, god.getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Боги");
		return "spa";
	}
}
