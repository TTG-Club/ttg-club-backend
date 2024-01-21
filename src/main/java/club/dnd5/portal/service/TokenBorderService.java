package club.dnd5.portal.service;

import club.dnd5.portal.dto.api.TokenBorderApi;
import club.dnd5.portal.model.token.TokenBorder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TokenBorderService {

	List<TokenBorderApi> getAllTokenBorders();

	List<TokenBorderApi> getTokenBordersByType(String type);

	TokenBorderApi createTokenBorder(TokenBorderApi tokenBorderApi);

	TokenBorderApi updateTokenBorder(TokenBorderApi tokenBorderApi);

	void deleteTokenBorder(TokenBorder tokenBorder);

	String uploadTokenBorder(MultipartFile multipartFile);
}
