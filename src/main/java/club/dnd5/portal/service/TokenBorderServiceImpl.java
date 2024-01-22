package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.TokenBorderApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.exception.StorageException;
import club.dnd5.portal.model.token.TokenBorder;
import club.dnd5.portal.repository.TokenBorderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenBorderServiceImpl implements TokenBorderService {

	private final TokenBorderRepository tokenBorderRepository;

	private static final String rootLocation = "src/main/resources/tokens/borders/";

	private TokenBorderApi mapToApi(TokenBorder tokenBorder) {
		return TokenBorderApi.builder()
			.id(tokenBorder.getId())
			.name(tokenBorder.getName())
			.type(tokenBorder.getType())
			.url(tokenBorder.getUrl())
			.build();
	}

	@Override
	public List<TokenBorderApi> getAllTokenBorders() {
		List<TokenBorder> tokenBorders = tokenBorderRepository.findAll();
		return tokenBorders.stream()
			.map(this::mapToApi)
			.collect(Collectors.toList());
	}

	@Override
	public List<TokenBorderApi> getTokenBordersByType(String type) {
		List<TokenBorder> tokenBorders = tokenBorderRepository.getTokenBordersByType(type);
		return tokenBorders.stream()
			.map(this::mapToApi)
			.collect(Collectors.toList());
	}

	@Override
	public TokenBorderApi createTokenBorder(TokenBorderApi tokenBorderApi) {
		TokenBorder tokenBorder = new TokenBorder();
		tokenBorder.setName(tokenBorderApi.getName());
		tokenBorder.setType(tokenBorderApi.getType());
		tokenBorder.setUrl(tokenBorderApi.getUrl());

		TokenBorder savedTokenBorder = tokenBorderRepository.save(tokenBorder);

		return mapToApi(savedTokenBorder);
	}

	@Override
	public TokenBorderApi updateTokenBorder(TokenBorderApi tokenBorderApi) {
		Optional<TokenBorder> optionalTokenBorder = tokenBorderRepository.findById(tokenBorderApi.getId());

		if (optionalTokenBorder.isPresent()) {
			TokenBorder tokenBorder = optionalTokenBorder.get();
			tokenBorder.setName(tokenBorderApi.getName());
			tokenBorder.setType(tokenBorderApi.getType());
			tokenBorder.setUrl(tokenBorderApi.getUrl());

			TokenBorder updatedTokenBorder = tokenBorderRepository.save(tokenBorder);

			return mapToApi(updatedTokenBorder);
		} else {
			throw new PageNotFoundException();
		}
	}

	@Override
	public void deleteTokenBorderById(Long id) {
		tokenBorderRepository.deleteById(id);
	}

	@Override
	public String storeTokenBorder(MultipartFile multipartFile) {
		try {
			if (multipartFile.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}

			String fileName = multipartFile.getOriginalFilename();
			String uniqueFileName = generateUniqueFileName(fileName);

			Path path = Paths.get(rootLocation);
			Path destinationFile = path.resolve(Paths.get(uniqueFileName))
				.normalize().toAbsolutePath();

			if (!destinationFile.getParent().equals(path.toAbsolutePath())) {
				throw new StorageException("Cannot store file outside current directory.");
			}

			try (InputStream inputStream = multipartFile.getInputStream()) {
				Files.copy(inputStream, destinationFile,
					StandardCopyOption.REPLACE_EXISTING);
			}

			return constructImageUrl(uniqueFileName);
		} catch (IOException e) {
			throw new PageNotFoundException();
		}
	}

	private String constructImageUrl(String fileName) {
		return "https://img.ttg.club/tokens/borders/" + fileName;
	}

	private String generateUniqueFileName(String fileName) {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
		String timestamp = now.format(formatter);

		return timestamp + "_" + fileName;
	}
}
