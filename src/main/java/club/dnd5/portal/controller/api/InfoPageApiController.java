package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.InfoPageApi;
import club.dnd5.portal.model.InfoPage;
import club.dnd5.portal.repository.InfoPagesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Tag(name = "Info page", description = "The info page API")
@RestController
@RequestMapping("/api/v1/info")
public class InfoPageApiController {
	@Autowired
	private InfoPagesRepository infoPagesRepository;

	@Operation(summary = "Gets info page result", tags = "info page")
	@GetMapping("/{url}")
	public ResponseEntity<InfoPageApi> getPage(@PathVariable String url) {
		Optional<InfoPage> page = infoPagesRepository.findOneByUrl(url);

		if (page.isPresent()) {
			InfoPage data = page.get();

			return ResponseEntity.ok(new InfoPageApi(data.getTitle(), data.getSubtitle(), data.getDescription()));
		}

		return ResponseEntity.notFound().build();
	}
}
