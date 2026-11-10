package club.dnd5.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Ограничения на картинки, загружаемые из мастерской, и параметры WebP.
 * <p>
 * Как на core-api: картинка проверяется и перекодируется в WebP. Отличие —
 * слишком большая по сторонам картинка не отклоняется, а уменьшается до
 * {@link #maxWidth} × {@link #maxHeight}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.image")
public class ImageProperties {
	/** Предел размера исходного файла. */
	private long maxBytes = 10 * 1024 * 1024;
	private int maxWidth = 2048;
	private int maxHeight = 2048;
	private List<String> allowedTypes = List.of("png", "jpg", "jpeg", "webp", "bmp");
	/** Качество WebP с потерями, 0..1. */
	private float webpQuality = 0.82f;
	private boolean webpLossless = false;
}
