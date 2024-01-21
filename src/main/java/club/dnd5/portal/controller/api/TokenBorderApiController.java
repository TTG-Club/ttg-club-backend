package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.TokenBorderApi;
import club.dnd5.portal.service.TokenBorderServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Границы Токенов", description = "API по границам Токенам")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tokens/borders")
public class TokenBorderApiController {
	private final TokenBorderServiceImpl tokenBorderService;

	@GetMapping
	public ResponseEntity<List<TokenBorderApi>> getAllTokenBorders() {
		List<TokenBorderApi> tokenBorders = tokenBorderService.getAllTokenBorders();
		return ResponseEntity.ok(tokenBorders);
	}

	@GetMapping("/by-type/{type}")
	public ResponseEntity<List<TokenBorderApi>> getTokenBordersByType(@PathVariable String type) {
		List<TokenBorderApi> tokenBorders = tokenBorderService.getTokenBordersByType(type);
		return ResponseEntity.ok(tokenBorders);
	}

	@PostMapping
	public ResponseEntity<TokenBorderApi> createTokenBorder(@RequestBody TokenBorderApi tokenBorderApi) {
		TokenBorderApi createdTokenBorder = tokenBorderService.createTokenBorder(tokenBorderApi);
		return ResponseEntity.ok(createdTokenBorder);
	}

	@PutMapping
	public ResponseEntity<TokenBorderApi> updateTokenBorder(@RequestBody TokenBorderApi tokenBorderApi) {
		TokenBorderApi updatedTokenBorder = tokenBorderService.updateTokenBorder(tokenBorderApi);
		return ResponseEntity.ok(updatedTokenBorder);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTokenBorder(@PathVariable Long id) {
		tokenBorderService.deleteTokenBorderById(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/upload")
	public ResponseEntity<String> uploadTokenBorder(@RequestParam("file") MultipartFile multipartFile) {
		String imageUrl = tokenBorderService.uploadTokenBorder(multipartFile);
		return ResponseEntity.ok(imageUrl);
	}

}
