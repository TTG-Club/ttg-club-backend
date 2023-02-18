package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.InfoPage;
import club.dnd5.portal.repository.InfoPageRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/info")
public class InfoPageController {
	private static final String BASE_URL = "https://ttg.club/info";

	@Autowired
	private InfoPageRepositories infoPageRepositories;

	@GetMapping("/{url}")
	public String getPage(Model model, @PathVariable String url) {
		InfoPage page = infoPageRepositories.findById(url)
			.orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", page.getTitle());
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", page.getTitle());
		model.addAttribute("menuTitle", page.getTitle());
		model.addAttribute("page", page);
		return "info_page";
	}
}
