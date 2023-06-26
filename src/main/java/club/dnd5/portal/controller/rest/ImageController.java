package club.dnd5.portal.controller.rest;

import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.repository.ImageRepository;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestController
public class ImageController {
	@Autowired
	private ImageRepository repository;

	@Autowired
	private HttpSession session;

	@GetMapping("/image/{type}/{id}")
	public String getImage(@PathVariable ImageType type, @PathVariable Integer id) {
		return repository.findAllByTypeAndRefId(type, id).stream().findFirst().orElse(getDefault(type));
	}

	@GetMapping("/images/{type}/{id}")
	public Collection<String> getImages(@PathVariable ImageType type, @PathVariable Integer id) {
		Collection<String> images = repository.findAllByTypeAndRefId(type, id);
		if (images.isEmpty()) {
			return Collections.singleton(getDefault(type));
		}
		return repository.findAllByTypeAndRefId(type, id);
	}

	private String getDefault(ImageType type) {
		Object themeObject = session.getAttribute("theme");
		String theme;
		if (themeObject == null) {
			theme = "light";
		} else {
			theme = themeObject.toString();
		}
		return "/style/" + theme + "/no_img_best.png";
	}
}
