package club.dnd5.portal.service.image;

import club.dnd5.portal.config.properties.ImageProperties;
import club.dnd5.portal.config.properties.S3Properties;
import club.dnd5.portal.exception.ApiException;
import club.dnd5.portal.model.image.ImageType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.Locale;

/**
 * Загрузка картинок в S3 — общий механизм для всех разделов, как {@code ImageService} на core-api.
 * <p>
 * Картинка проверяется (размер файла, тип), уменьшается до допустимых сторон и
 * перекодируется в WebP. Ключ объекта — {@code <app.s3.prefix>/<путь>}, а наружу
 * отдаётся адрес {@code <app.img.url>/<путь>}: img.ttg.club смотрит в бакет с этим префиксом.
 */
@Service
@RequiredArgsConstructor
public class ImageStorageService {
	private static final String WEBP_CONTENT_TYPE = "image/webp";
	/** Стороны, больше которых картинку даже не декодируем: защита от «бомб» распаковки. */
	private static final int MAX_DECODED_SIDE = 12_000;
	private static final int MAX_NAME_LENGTH = 60;

	private final ObjectProvider<S3Client> s3Client;
	private final S3Properties s3Properties;
	private final ImageProperties imageProperties;

	@Value("${app.img.url}")
	private String imageUrl;

	@PostConstruct
	public void init() {
		ImageIO.scanForPlugins();
	}

	/**
	 * Новая картинка раздела: {@code <папка раздела>/<время>-<имя файла>.webp}.
	 * @param type раздел записи
	 * @param file загруженный файл
	 * @return публичный адрес картинки
	 */
	public String upload(ImageType type, MultipartFile file) {
		String path = type.getFolder() + "/" + Instant.now().getEpochSecond() + "-" + fileSlug(file.getOriginalFilename()) + ".webp";
		put(path, convert(file), null);
		return publicUrl(path);
	}

	/**
	 * Картинка по заданному пути: файл по этому пути перезаписывается.
	 * @param path путь от корня img.ttg.club без ведущего «/»
	 * @param file загруженный файл
	 * @param cacheControl заголовок Cache-Control объекта или {@code null}
	 * @return публичный адрес картинки
	 */
	public String uploadTo(String path, MultipartFile file, String cacheControl) {
		put(path, convert(file), cacheControl);
		return publicUrl(path);
	}

	/**
	 * Удаляет картинку по её публичному адресу. Удаляются только картинки с img.ttg.club.
	 * @param url публичный адрес картинки
	 */
	public void delete(String url) {
		String path = pathOf(url);
		if (path == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Картинка лежит не на " + baseUrl());
		}
		try {
			requireS3Client().deleteObject(request -> request
				.bucket(s3Properties.getBucket())
				.key(objectKey(path)));
		} catch (SdkException e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Не удалось удалить картинку из хранилища");
		}
	}

	/**
	 * Путь картинки от корня img.ttg.club или {@code null}, если адрес чужой.
	 * @param url публичный адрес картинки
	 */
	public String pathOf(String url) {
		String base = baseUrl() + "/";
		if (url == null || !url.startsWith(base)) {
			return null;
		}
		String path = url.substring(base.length());
		int query = path.indexOf('?');
		if (query >= 0) {
			path = path.substring(0, query);
		}
		if (path.isBlank() || path.contains("..")) {
			return null;
		}
		return path;
	}

	/** Проверяет файл и перекодирует его в WebP. */
	byte[] convert(MultipartFile file) {
		byte[] bytes = readBytes(file);
		validateType(bytes, file.getOriginalFilename());
		validateDimensions(bytes);

		ImageConverter.WebpOptions options = imageProperties.isWebpLossless()
			? ImageConverter.WebpOptions.lossless()
			: ImageConverter.WebpOptions.lossy(imageProperties.getWebpQuality());
		options = options.resizeToFit(imageProperties.getMaxWidth(), imageProperties.getMaxHeight());

		try {
			byte[] webp = ImageConverter.toWebp(bytes, file.getContentType(), file.getOriginalFilename(), options).bytes();
			if (webp == null || webp.length == 0) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Не удалось перекодировать картинку в WebP");
			}
			return webp;
		} catch (IOException | IllegalArgumentException e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Не удалось прочитать картинку");
		}
	}

	private byte[] readBytes(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Файл пустой");
		}
		if (file.getSize() > imageProperties.getMaxBytes()) {
			throw new ApiException(HttpStatus.CONTENT_TOO_LARGE, "Файл больше "
				+ imageProperties.getMaxBytes() / (1024 * 1024) + " МБ");
		}
		try {
			return file.getBytes();
		} catch (IOException e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Не удалось прочитать файл");
		}
	}

	private void validateType(byte[] bytes, String filename) {
		ImageConverter.SourceFormat format = ImageConverter.SourceFormat.fromSignature(bytes)
			.orElse(ImageConverter.SourceFormat.UNKNOWN);
		String extension = switch (format) {
			case JPEG -> "jpg";
			case PNG -> "png";
			case BMP -> "bmp";
			case WEBP -> "webp";
			default -> "";
		};
		if (extension.isEmpty() || !imageProperties.getAllowedTypes().contains(extension)) {
			throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Неподдерживаемый тип картинки"
				+ (filename == null ? "" : ": " + filename) + ". Можно: "
				+ String.join(", ", imageProperties.getAllowedTypes()));
		}
	}

	private void validateDimensions(byte[] bytes) {
		try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
			Iterator<ImageReader> readers = input == null ? null : ImageIO.getImageReaders(input);
			if (readers == null || !readers.hasNext()) {
				throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Файл не является картинкой");
			}
			ImageReader reader = readers.next();
			try {
				reader.setInput(input, true, true);
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);
				if (width <= 0 || height <= 0 || width > MAX_DECODED_SIDE || height > MAX_DECODED_SIDE) {
					throw new ApiException(HttpStatus.BAD_REQUEST, "Недопустимый размер картинки: " + width + "×" + height);
				}
			} finally {
				reader.dispose();
			}
		} catch (IOException e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Не удалось прочитать размеры картинки");
		}
	}

	private void put(String path, byte[] webp, String cacheControl) {
		try {
			requireS3Client().putObject(
				request -> {
					request
						.bucket(s3Properties.getBucket())
						.key(objectKey(path))
						.contentType(WEBP_CONTENT_TYPE);
					if (cacheControl != null) {
						request.cacheControl(cacheControl);
					}
				},
				RequestBody.fromBytes(webp));
		} catch (SdkException e) {
			throw new ApiException(HttpStatus.BAD_GATEWAY, "Не удалось сохранить картинку в хранилище");
		}
	}

	private S3Client requireS3Client() {
		S3Client client = s3Client.getIfAvailable();
		if (client == null) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Хранилище картинок не настроено (app.s3.endpoint)");
		}
		return client;
	}

	private String objectKey(String path) {
		String prefix = s3Properties.getPrefix();
		return prefix == null || prefix.isBlank() ? path : prefix + "/" + path;
	}

	private String publicUrl(String path) {
		return baseUrl() + "/" + path;
	}

	private String baseUrl() {
		return imageUrl.endsWith("/") ? imageUrl.substring(0, imageUrl.length() - 1) : imageUrl;
	}

	/** Имя файла латиницей: без расширения, только {@code a-z0-9-}. */
	static String fileSlug(String filename) {
		String name = filename == null ? "" : filename;
		int dot = name.lastIndexOf('.');
		if (dot > 0) {
			name = name.substring(0, dot);
		}
		String slug = name.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]+", "-")
			.replaceAll("^-+|-+$", "");
		if (slug.length() > MAX_NAME_LENGTH) {
			slug = slug.substring(0, MAX_NAME_LENGTH).replaceAll("-+$", "");
		}
		return slug.isEmpty() ? "image" : slug;
	}
}
