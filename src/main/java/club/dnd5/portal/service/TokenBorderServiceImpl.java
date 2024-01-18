package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.TokenBorderApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.token.TokenBorder;
import club.dnd5.portal.repository.TokenBorderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenBorderServiceImpl implements TokenBorderService {

	private final TokenBorderRepository tokenBorderRepository;

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
}
