package club.dnd5.portal.controller.api;

import club.dnd5.portal.model.splells.Spell;
import club.dnd5.portal.repository.datatable.SpellRepository;
import club.dnd5.portal.service.LssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/lss")
@Tag(name = "API для интеграции с LSS", description = "LSS Integration API")
@RequiredArgsConstructor
public class LssApiController {
	private final LssService lssService;
	private final SpellRepository spellRepository;

	@Operation(summary = "Получение заклинание в LSS формате по айди")
	@GetMapping(value = "/spell")
	public ResponseEntity<Resource> findById(@RequestParam("id") Integer id) {
		byte[] jsonBytes = lssService.findById(id).getBytes();
		HttpHeaders headers = createResponseHeaders(id);
		Resource resource = new ByteArrayResource(jsonBytes);
		return new ResponseEntity<>(resource, headers, HttpStatus.OK);
	}

	private HttpHeaders createResponseHeaders(Integer id) {
		String spellName;
		Optional<Spell> spellOptional = spellRepository.findById(id);

		if (spellOptional.isPresent()) {
			Spell spell = spellOptional.get();
			spellName = spell.getEnglishName();
		} else {
			spellName = "spell_" + id;
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		String file = String.format("attachment; filename=\"%s.json\"", spellName);
		responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, file);
		responseHeaders.setContentType(MediaType.APPLICATION_JSON);
		return responseHeaders;
	}
}
