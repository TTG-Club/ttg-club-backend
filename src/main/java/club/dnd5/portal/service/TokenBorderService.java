package club.dnd5.portal.service;

import club.dnd5.portal.model.token.TokenBorder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TokenBorderService {
	List<TokenBorder> getAllTokenBorders();

	List<TokenBorder> getTokenBordersByType(String type);

	TokenBorder createTokenBorder(TokenBorder tokenBorder);

	TokenBorder updateTokenBorder(TokenBorder tokenBorder);

	void deleteTokenBorderById(Long id);

	String storeTokenBorder(MultipartFile multipartFile);
}
