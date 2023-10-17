package club.dnd5.portal.controller.api;

import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.token.Token;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Tag(name = "Токены", description = "API по токена")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tokens")
public class TokenApiController {
	private final TokenRepository tokenRepository;
	private final BestiaryRepository bestiaryRepository;
	@Operation(summary = "Получение и поиск токенов")
	@GetMapping
	public List<Token> getTokens(
		@RequestParam(required = false) final String name,
		@RequestParam(required = false) final String altName,
		@RequestParam(required = false) final String englishName,
		@RequestParam(required = false) final String type) {
		Specification<Token> specification = null;
		if (Objects.nonNull(name)) {
			specification = (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%");
		}
		if (Objects.nonNull(altName)) {
			specification = SpecificationUtil.getOrSpecification(
				specification, (root, query, cb) -> cb.like(root.get("alt_name"), "%" + altName + "%"));
		}
		if (Objects.nonNull(englishName)) {
			specification = SpecificationUtil.getOrSpecification(
				specification, (root, query, cb) -> cb.like(root.get("english_name"), "%" + englishName + "%"));
		}
		if (Objects.nonNull(englishName)) {
			specification = SpecificationUtil.getAndSpecification(
				specification, (root, query, cb) -> cb.equal(root.get("type"), type));
		}
		return tokenRepository.findAll(specification);
	}

	@Operation(summary = "Получение токена по id существа")
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("{id}")
	public Token getTokenById(@PathVariable Long id) {
		return tokenRepository.findById(id)
			.orElseThrow(PageNotFoundException::new);
	}

	@Operation(summary = "Добавление токена")
	@Secured("ADMIN")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	@PostMapping
	public void addToken(@RequestParam(required = false) final String name,
						 @RequestParam(required = false) final String altName,
						 @RequestParam(required = true) final String englishName,
						 @RequestParam(required = false) final String type,
						 @RequestParam(required = false) final String url) {
		Creature creature = bestiaryRepository.findByEnglishName(englishName)
				.orElseThrow(PageNotFoundException::new);
		Token token = new Token();
		if (Objects.isNull(name)) {
			token.setName(creature.getName());
		} else {
			token.setName(name);
		}
		if (Objects.isNull(altName)) {
			token.setAltName(creature.getAltName());
		} else {
			token.setAltName(altName);
		}
		token.setEnglishName(englishName);
		if (Objects.isNull(type)) {
			token.setType("круглый");
		} else {
			token.setType(type);
		}
		if (Objects.isNull(url)) {
			token.setUrl(String.format("https://img.ttg.club/tokens/round/%s.webp",
				creature.getUrlName().replace("\'", "_")));
		} else {
			token.setUrl(url);
		}
		tokenRepository.save(token);
	}

	@Operation(summary = "Удаление токена")
	@Secured("ADMIN")
	@Transactional
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping("{id}")
	public void deleteTokenById(@PathVariable Long id) {
		tokenRepository.deleteById(id);
	}
}
