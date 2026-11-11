package club.dnd5.portal.dto.api.image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Галерея записи: адреса картинок по порядку, первая — главная.
 * @param images адреса картинок
 */
public record ImageGalleryApi(
	@NotNull
	@Size(max = 50)
	List<@NotBlank @Size(max = 255) String> images
) {
}
