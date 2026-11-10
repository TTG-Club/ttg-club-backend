package club.dnd5.portal.controller;

import club.dnd5.portal.config.properties.S3Properties;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UrlPathHelper;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

/**
 * Раздача картинок сайта из S3: {@code GET /s3/<ключ>}, как на новом сайте.
 * <p>
 * Отдаются только объекты под {@code <app.s3.prefix>/} — картинки старого
 * сайта; остальной бакет (новый сайт, аватарки) отсюда не виден.
 */
@Slf4j
@Hidden
@RestController
@RequiredArgsConstructor
public class S3ImageController {
	static final String ROOT = "/s3/";
	/** Картинки по одному адресу не меняются (новая загрузка — новое имя), кроме обложек. */
	static final String DEFAULT_CACHE = "public, max-age=2592000";

	private static final UrlPathHelper PATHS = new UrlPathHelper();

	private final ObjectProvider<S3Client> s3Client;
	private final S3Properties s3Properties;

	@GetMapping("/s3/**")
	public void image(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String key = objectKey(PATHS.getPathWithinApplication(request), s3Properties.getPrefix());
		if (key == null) {
			response.sendError(HttpStatus.NOT_FOUND.value());
			return;
		}
		S3Client client = s3Client.getIfAvailable();
		if (client == null) {
			response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value());
			return;
		}
		String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
		try (ResponseInputStream<GetObjectResponse> object = client.getObject(builder -> {
			builder.bucket(s3Properties.getBucket()).key(key);
			if (ifNoneMatch != null && !ifNoneMatch.isBlank()) {
				builder.ifNoneMatch(ifNoneMatch);
			}
		})) {
			GetObjectResponse meta = object.response();
			response.setContentType(contentType(meta.contentType(), key));
			if (meta.contentLength() != null) {
				response.setContentLengthLong(meta.contentLength());
			}
			response.setHeader(HttpHeaders.CACHE_CONTROL,
				meta.cacheControl() == null || meta.cacheControl().isBlank() ? DEFAULT_CACHE : meta.cacheControl());
			if (meta.eTag() != null) {
				response.setHeader(HttpHeaders.ETAG, meta.eTag());
			}
			response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			object.transferTo(response.getOutputStream());
		} catch (S3Exception e) {
			int status = e.statusCode();
			if (status == HttpStatus.NOT_MODIFIED.value()) {
				response.setStatus(status);
			} else if (status == HttpStatus.NOT_FOUND.value() || status == HttpStatus.FORBIDDEN.value()) {
				// Без права на листинг S3 отвечает 403 и на отсутствующий ключ.
				response.sendError(HttpStatus.NOT_FOUND.value());
			} else {
				log.warn("S3: не удалось отдать {}: {} {}", key, status, e.getMessage());
				response.sendError(HttpStatus.BAD_GATEWAY.value());
			}
		} catch (SdkException e) {
			log.warn("S3: не удалось отдать {}: {}", key, e.getMessage());
			response.sendError(HttpStatus.BAD_GATEWAY.value());
		}
	}

	/**
	 * Ключ объекта по пути запроса или {@code null}, если путь не про картинки
	 * старого сайта: не под префиксом, с {@code ..} или обратным слэшем.
	 * @param path раскодированный путь запроса, {@code /s3/5e14/classes/Bard.webp}
	 * @param prefix папка картинок сайта в бакете
	 */
	static String objectKey(String path, String prefix) {
		if (path == null || !path.startsWith(ROOT)) {
			return null;
		}
		String key = path.substring(ROOT.length());
		if (key.isBlank() || key.endsWith("/") || key.contains("\\") || key.startsWith("/")) {
			return null;
		}
		for (String segment : key.split("/")) {
			if (segment.isEmpty() || segment.equals("..") || segment.equals(".")) {
				return null;
			}
		}
		if (prefix != null && !prefix.isBlank() && !key.startsWith(prefix + "/")) {
			return null;
		}
		return key;
	}

	private static String contentType(String stored, String key) {
		if (stored != null && !stored.isBlank() && !stored.equals(MediaType.APPLICATION_OCTET_STREAM_VALUE)) {
			return stored;
		}
		return MediaTypeFactory.getMediaType(key).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
	}
}
