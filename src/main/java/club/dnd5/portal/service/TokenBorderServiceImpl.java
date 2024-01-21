package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.TokenBorderApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.token.TokenBorder;
import club.dnd5.portal.repository.TokenBorderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenBorderServiceImpl implements TokenBorderService {

	private final TokenBorderRepository tokenBorderRepository;

	private final static String TOKEN_BORDERS_PATH = "/resources/token-borders/";

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
	public void deleteTokenBorder(TokenBorder tokenBorder) {
		tokenBorderRepository.delete(tokenBorder);
	}

	@Override
	public String uploadTokenBorder(MultipartFile multipartFile) {
		String fileName = multipartFile.getName();
		String filePath = TOKEN_BORDERS_PATH + fileName + ".webp";
		try {
			saveFile(multipartFile, filePath);
			return constructImageUrl(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String constructImageUrl(String fileName) {
		return "https://img.ttg.club/tokens/borders/" + fileName + ".webp";
	}

	private void saveFile(MultipartFile multipartFile, String filePath) throws IOException {
		byte[] bytes = multipartFile.getBytes();
		Path path = Paths.get(filePath);
		Files.write(path, bytes);
	}
}
