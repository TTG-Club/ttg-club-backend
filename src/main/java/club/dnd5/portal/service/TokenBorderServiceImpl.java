package club.dnd5.portal.service;

import club.dnd5.portal.config.properties.S3Properties;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.exception.StorageException;
import club.dnd5.portal.model.token.TokenBorder;
import club.dnd5.portal.repository.TokenBorderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Рамки токенов. Файлы рамок лежат в S3 (бакет картинок, папка
 * {@code app.s3.prefix}) и раздаются с {@code app.img.url}.
 */
@Service
@RequiredArgsConstructor
public class TokenBorderServiceImpl implements TokenBorderService {
	private final TokenBorderRepository tokenBorderRepository;
	private final ObjectProvider<S3Client> s3Client;
	private final S3Properties s3Properties;

	@Value("${images.token.borders}")
	private String tokenBorders;

	@Value("${app.img.url}")
	private String ttgWebsiteUrl;

	@Override
	public List<TokenBorder> getAllTokenBorders() {
		return tokenBorderRepository.findAll();
	}

	@Override
	public List<TokenBorder> getTokenBordersByType(String type) {
		return tokenBorderRepository.findAllByType(type);
	}

	@Override
	public TokenBorder createTokenBorder(TokenBorder tokenBorder) {
		TokenBorder newTokenBorder = new TokenBorder();
		newTokenBorder.setName(tokenBorder.getName());
		newTokenBorder.setType(tokenBorder.getType());
		newTokenBorder.setUrl(tokenBorder.getUrl());
		newTokenBorder.setUserId(tokenBorder.getUserId());
		return tokenBorderRepository.save(newTokenBorder);
	}

	@Override
	public TokenBorder updateTokenBorder(TokenBorder newTokenBorder) {
		Optional<TokenBorder> optionalTokenBorder = tokenBorderRepository.findById(newTokenBorder.getId());

		return optionalTokenBorder
			.map(existingTokenBorder -> {
				existingTokenBorder.setName(newTokenBorder.getName());
				existingTokenBorder.setType(newTokenBorder.getType());
				existingTokenBorder.setUrl(newTokenBorder.getUrl());
				existingTokenBorder.setUserId(newTokenBorder.getUserId());
				return tokenBorderRepository.save(existingTokenBorder);
			})
			.orElseThrow(PageNotFoundException::new);
	}

	@Override
	public void deleteTokenBorderById(Long id) {
		Optional<TokenBorder> optionalTokenBorder = tokenBorderRepository.findById(id);

		optionalTokenBorder.ifPresent(tokenBorder -> {
			// Delete the associated file
			deleteFile(tokenBorder);

			// Delete the TokenBorder entity from the repository
			tokenBorderRepository.deleteById(id);
		});
	}

	private void deleteFile(TokenBorder tokenBorder) {
		// Extract the file name from the URL
		String url = tokenBorder.getUrl();
		String fileName = extractFileNameFromUrl(url);

		try {
			requireS3Client().deleteObject(request -> request
				.bucket(s3Properties.getBucket())
				.key(objectKey(fileName)));
		} catch (SdkException e) {
			throw new StorageException("Failed to delete file", e);
		}
	}

	private String extractFileNameFromUrl(String url) {
		// Extract the file name from the URL
		int lastSlashIndex = url.lastIndexOf('/');
		if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
			return url.substring(lastSlashIndex + 1);
		} else {
			// Handle the case where the URL format is unexpected
			throw new StorageException("Invalid URL format: " + url);
		}
	}

	@Override
	public String storeTokenBorder(MultipartFile multipartFile) {
		try {
			if (multipartFile.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}

			String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename());
			// sanitizeFileName убирает «/», «\» и «..», поэтому имя не выйдет за папку рамок.
			String uniqueFileName = generateUniqueFileName(fileName);

			try (InputStream inputStream = multipartFile.getInputStream()) {
				requireS3Client().putObject(
					request -> request
						.bucket(s3Properties.getBucket())
						.key(objectKey(uniqueFileName))
						.contentType(multipartFile.getContentType()),
					RequestBody.fromInputStream(inputStream, multipartFile.getSize()));
			}

			return constructImageUrl(uniqueFileName);
		} catch (IOException | SdkException e) {
			throw new StorageException("Failed to store file", e);
		}
	}

	private S3Client requireS3Client() {
		S3Client client = s3Client.getIfAvailable();
		if (client == null) {
			throw new StorageException("S3 storage is not configured (app.s3.endpoint)");
		}
		return client;
	}

	/** Ключ в бакете повторяет путь из URL картинки: {@code <prefix>/tokens/borders/<file>}. */
	private String objectKey(String fileName) {
		return s3Properties.getPrefix() + tokenBorders + "/" + fileName;
	}

	private String constructImageUrl(String fileName) {
		return ttgWebsiteUrl + tokenBorders + "/" + fileName;
	}

	private String generateUniqueFileName(String fileName) {
		String sanitizedFileName = sanitizeFileName(fileName);
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
		String timestamp = now.format(formatter);

		return timestamp + "_" + sanitizedFileName;
	}

	private String sanitizeFileName(String fileName) {
		// Remove any path components ("/" or "\") and ".." sequences from the file name
		String sanitizedFileName = fileName.replaceAll("[/\\\\]+|\\.\\.", "");

		// Ensure that the file has a valid and expected extension
		if (!sanitizedFileName.matches("^[a-zA-Z0-9_-]+\\.(jpg|png|gif|webp)$")) {
			throw new SecurityException("Invalid file extension.");
		}

		return sanitizedFileName;
	}
}
