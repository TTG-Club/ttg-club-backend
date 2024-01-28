package club.dnd5.portal.controller;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.items.MagicItem;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.datatable.MagicItemRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;

@RequiredArgsConstructor
@Hidden
@Controller
public class MagicItemController {
	private static final String BASE_URL = "https://ttg.club/items/magic";

	private final MagicItemRepository repository;
	private final ImageRepository imageRepo;

	@GetMapping({"/items/magic", "/magic-items"})
	public String getMagicItems(Model model) {
		model.addAttribute("metaTitle", "Магические предметы (Magic items) D&D 5e");
		model.addAttribute("metaUrl", BASE_URL);
		model.addAttribute("metaDescription", "Магические предметы и артефакты по D&D 5 редакции");
		model.addAttribute("menuTitle", "Магические предметы");
		return "spa";
	}

	@GetMapping({"/items/magic/{name}", "/magic-items/{name}"})
	public String getMagicItem(Model model, @PathVariable String name) {
		MagicItem item = repository.findByEnglishName(name.replace("_", " ")).orElseThrow(PageNotFoundException::new);
		model.addAttribute("metaTitle", String.format("%s (%s) | Магические предметы D&D 5e", item.getName(), item.getEnglishName()));
		model.addAttribute("metaUrl", String.format("%s/%s", BASE_URL, item.getUrlName()));
		model.addAttribute("metaDescription", String.format("%s (%s) - %s %s", item.getName(), item.getEnglishName(), item.getRarity().getCyrilicName(), item.getType().getCyrilicName()));
		Collection<String> images = imageRepo.findAllByTypeAndRefId(ImageType.MAGIC_ITEM, item.getId());
		if (!images.isEmpty()) {
			model.addAttribute("metaImage", images.iterator().next());
		}
		model.addAttribute("menuTitle", "Магические предметы");
		return "spa";
	}
}
