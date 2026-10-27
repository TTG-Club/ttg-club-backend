package club.dnd5.portal.controller.api.bestiary;

import club.dnd5.portal.dto.api.bestiary.BeastDetailApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.service.BestiaryService;
import club.dnd5.portal.service.WorkshopPreview;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Мастерская бестиария. Существа сохраняются через {@link Bestiary2ApiController},
 * а предпросмотр лежит под /api/v1/workshop, как у остальных разделов мастерской.
 */
@RequiredArgsConstructor
@Tag(name = "Бестиарий v2", description = "API для существ из бестиария")
@RestController
public class BestiaryWorkshopApiController {
	private final BestiaryService bestiaryService;
	private final WorkshopPreview workshopPreview;

	@Operation(summary = "Предпросмотр существа без сохранения")
	@SecurityRequirement(name = "Bearer Authentication")
	@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
	@PostMapping(value = "/api/v1/workshop/bestiary/preview", produces = MediaType.APPLICATION_JSON_VALUE)
	public BeastDetailApi previewBeast(@Valid @RequestBody BeastDetailRequest request) {
		return workshopPreview.render(
			() -> {
				if (request.getId() == null) {
					bestiaryService.create(request);
				} else {
					bestiaryService.update(request);
				}
			},
			() -> bestiaryService.findOne(request.getName().getEng().trim()));
	}
}
