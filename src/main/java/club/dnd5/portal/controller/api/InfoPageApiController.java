package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.InfoPageApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.InfoPage;
import club.dnd5.portal.repository.InfoPagesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "Info page", description = "The info page API")
@RestController
@RequestMapping("/api/v1/info")
public class InfoPageApiController {
	@Autowired
	private InfoPagesRepository infoPagesRepository;

	@Operation(summary = "Check is info page exist", tags = "info page exist")
	@PostMapping("/{url}")
	public ResponseEntity<?> getPageExist(@PathVariable String url) {
		InfoPage page = infoPagesRepository.findOneByUrl(url).orElseThrow(PageNotFoundException::new);
		return ResponseEntity.ok(page);
	}

	@Operation(summary = "Gets info page result", tags = "info page")
	@GetMapping("/{url}")
	public  ResponseEntity<InfoPageApi> getPage(@PathVariable String url) {
		InfoPage infoPage = infoPagesRepository.findOneByUrl(url).orElseThrow(PageNotFoundException::new);
		return  ResponseEntity.ok(new InfoPageApi(infoPage.getTitle(), infoPage.getSubtitle(), infoPage.getDescription()));
	}
}
