package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.PaginatedResponseApi;
import club.dnd5.portal.dto.api.TokenApi;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.model.image.ImageType;
import club.dnd5.portal.model.token.Token;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import club.dnd5.portal.util.SpecificationUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "Токены", description = "API по токена")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tokens")
public class TokenApiController {
	private final TokenRepository tokenRepository;
	private final BestiaryRepository bestiaryRepository;

	@Operation(summary = "Получение и поиск токенов")
	@GetMapping
	public PaginatedResponseApi<TokenApi> getTokens(
		@RequestParam(required = false) final String search,
		@RequestParam(required = false, defaultValue = "false") final Boolean exact,
		@RequestParam(required = false) final String type,
		@RequestParam(required = false, defaultValue = "0") final Integer page,
		@RequestParam(required = false, defaultValue = "-1") final Integer size,
		@RequestParam(required = false) List<String> order
	) {
		Specification<Token> specification = null;

		if (Objects.nonNull(search)) {
			specification = SpecificationUtil.getSearch(search, exact);
		}

		if (Objects.nonNull(type)) {
			specification = SpecificationUtil.getAndSpecification(
				specification, (root, query, cb) -> cb.equal(root.get("type"), type.trim()));
		}

		Pageable pageable = PageAndSortUtil.getPageable(page, size, order);

		List<Token> tokens = tokenRepository.findAll(specification, pageable).toList();

		long total = tokenRepository.count(specification);

        return new PaginatedResponseApi<>(
			tokens
				.stream()
				.map(TokenApi::new)
				.collect(Collectors.toList()),
			total,
			page,
			size
		);
	}

	@Operation(summary = "Получение токена по id существа")
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("{id}")
	public Token getTokenById(@PathVariable Long id) {
		return tokenRepository.findById(id)
			.orElseThrow(PageNotFoundException::new);
	}

	@Operation(summary = "Добавление токена")
	@Secured({"ADMIN"})
	@SecurityRequirement(name = "Bearer Authentication")
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	@PostMapping
	public String addToken(@RequestParam(required = false) final String name,
						 @RequestParam(required = false) final String altName,
						 @RequestParam final String englishName,
						 @RequestParam(required = false) final String type,
						 @RequestParam(required = false) final String url) {
		Creature creature = bestiaryRepository.findByEnglishName(englishName)
				.orElseThrow(PageNotFoundException::new);
		Token token = new Token();
		token.setRefId(creature.getId());
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
			token.setUrl("https://img.ttg.club/tokens/round/" + creature.getUrl()
				.replace("'", "-")
				.replaceAll("[(),]+", "_")
				+ ".webp");
		} else {
			token.setUrl(url);
		}
		tokenRepository.save(token);
		return token.getUrl();
	}

	@Operation(summary = "Удаление токена")
	@SecurityRequirement(name = "Bearer Authentication")
	@Secured("ADMIN")
	@Transactional
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping("{id}")
	public void deleteTokenById(@PathVariable Long id) {
		tokenRepository.deleteById(id);
	}

    @RequiredArgsConstructor
    @Hidden
    @RestController
    public static class ImageController {
        private final ImageRepository repository;
        private final HttpSession session;

        @GetMapping("/image/{type}/{id}")
        public String getImage(@PathVariable ImageType type, @PathVariable Integer id) {
            return repository.findAllByTypeAndRefId(type, id).stream().findFirst().orElse(getDefault(type));
        }

        @GetMapping("/images/{type}/{id}")
        public Collection<String> getImages(@PathVariable ImageType type, @PathVariable Integer id) {
            Collection<String> images = repository.findAllByTypeAndRefId(type, id);
            if (images.isEmpty()) {
                return Collections.singleton(getDefault(type));
            }
            return repository.findAllByTypeAndRefId(type, id);
        }

        private String getDefault(ImageType type) {
            Object themeObject = session.getAttribute("theme");
            String theme;
            if (themeObject == null) {
                theme = "light";
            } else {
                theme = themeObject.toString();
            }
            return "/style/" + theme + "/no_img_best.png";
        }
    }
}
