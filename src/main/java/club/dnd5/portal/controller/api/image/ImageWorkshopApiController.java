package club.dnd5.portal.controller.api.image;

import club.dnd5.portal.dto.api.image.ImageCoverApi;
import club.dnd5.portal.dto.api.image.ImageGalleryApi;
import club.dnd5.portal.dto.api.image.ImageUploadApi;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.service.image.ImageGalleryService;
import club.dnd5.portal.service.image.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Картинки мастерской: общая загрузка в S3 (как {@code /api/v2/image} на core-api),
 * галереи записей и обложки классов и рас.
 * <p>
 * {@code type} — раздел записи: CLASS, SUBCLASS, RACE, CREATURE, GOD, ITEM, MAGIC_ITEM, WEAPON, ARMOR.
 */
@Tag(name = "Картинки мастерской")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workshop/images")
public class ImageWorkshopApiController {
	private final ImageStorageService imageStorageService;
	private final ImageGalleryService imageGalleryService;

	@Operation(summary = "Загрузка картинки в папку раздела: WebP, адрес /s3/5e14/...")
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ImageUploadApi upload(@RequestParam ImageType type, @RequestPart("file") MultipartFile file) {
		return new ImageUploadApi(imageStorageService.upload(type, file));
	}

	@Operation(summary = "Удаление загруженной картинки по адресу")
	@DeleteMapping
	public void delete(@RequestParam String url) {
		imageStorageService.delete(url);
	}

	@Operation(summary = "Галерея записи")
	@GetMapping(value = "/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ImageGalleryApi getGallery(@PathVariable ImageType type, @PathVariable Integer id) {
		return new ImageGalleryApi(imageGalleryService.getGallery(type, id));
	}

	@Operation(summary = "Замена галереи записи: порядок, удаление, добавление по адресу")
	@PutMapping(value = "/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ImageGalleryApi replaceGallery(
		@PathVariable ImageType type,
		@PathVariable Integer id,
		@Valid @RequestBody ImageGalleryApi request
	) {
		return new ImageGalleryApi(imageGalleryService.replaceGallery(type, id, request.images()));
	}

	@Operation(summary = "Загрузка картинки в конец галереи записи")
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(
		value = "/{type}/{id}",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ImageGalleryApi addToGallery(
		@PathVariable ImageType type,
		@PathVariable Integer id,
		@RequestPart("file") MultipartFile file
	) {
		return new ImageGalleryApi(imageGalleryService.addToGallery(type, id, file));
	}

	@Operation(summary = "Обложка класса или расы")
	@GetMapping(value = "/{type}/{id}/cover", produces = MediaType.APPLICATION_JSON_VALUE)
	public ImageCoverApi getCover(@PathVariable ImageType type, @PathVariable Integer id) {
		return imageGalleryService.getCover(type, id);
	}

	@Operation(summary = "Загрузка обложки класса или основной расы (файл перезаписывается)")
	@PostMapping(
		value = "/{type}/{id}/cover",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ImageCoverApi uploadCover(
		@PathVariable ImageType type,
		@PathVariable Integer id,
		@RequestPart("file") MultipartFile file
	) {
		return imageGalleryService.uploadCover(type, id, file);
	}
}
