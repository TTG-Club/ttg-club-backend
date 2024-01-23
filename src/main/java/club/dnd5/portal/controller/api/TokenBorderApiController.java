package club.dnd5.portal.controller.api;

import club.dnd5.portal.model.token.TokenBorder;
import club.dnd5.portal.service.TokenBorderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Границы Токенов", description = "API по границам Токенам")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tokens/borders")
public class TokenBorderApiController {
	private final TokenBorderServiceImpl tokenBorderService;

	@Operation(summary = "Получение всех границ токенов")
	@GetMapping
	public List<TokenBorder> getAllTokenBorders() {
		return tokenBorderService.getAllTokenBorders();
	}

	@Operation(summary = "Получение всех границ токенов по типу")
	@GetMapping("/by-type/{type}")
	public List<TokenBorder> getTokenBordersByType(@PathVariable String type) {
		return tokenBorderService.getTokenBordersByType(type);
	}

	@Operation(summary = "Создание границы токена")
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirement(name = "Bearer Authentication")
	@Secured("ADMIN")
	@PostMapping
	public TokenBorder createTokenBorder(@RequestBody TokenBorder tokenBorder) {
		return tokenBorderService.createTokenBorder(tokenBorder);
	}

	@Operation(summary = "Обновление границы токена")
	@ResponseStatus(HttpStatus.OK)
	@SecurityRequirement(name = "Bearer Authentication")
	@Secured("ADMIN")
	@PutMapping
	public TokenBorder updateTokenBorder(@RequestBody TokenBorder tokenBorder) {
		return tokenBorderService.updateTokenBorder(tokenBorder);
	}

	@Operation(summary = "Удаление границы токена по id")
	@ResponseStatus(HttpStatus.OK)
	@SecurityRequirement(name = "Bearer Authentication")
	@Secured("ADMIN")
	@DeleteMapping("/{id}")
	public void deleteTokenBorder(@PathVariable Long id) {
		tokenBorderService.deleteTokenBorderById(id);
	}

	@Operation(summary = "Сохранения файла и получения url")
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirement(name = "Bearer Authentication")
	@Secured("ADMIN")
	@PostMapping(value = "/upload",consumes = {"multipart/form-data"} )
	public String uploadTokenBorder(@RequestParam("file") MultipartFile multipartFile) {
		return tokenBorderService.storeTokenBorder(multipartFile);
	}
}
