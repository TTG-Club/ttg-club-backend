package club.dnd5.portal.controller.api.bestiary;

import club.dnd5.portal.dto.api.bestiary.BeastApi;
import club.dnd5.portal.dto.api.bestiary.BeastDetailApi;
import club.dnd5.portal.dto.api.bestiary.BeastRequesApi;
import club.dnd5.portal.dto.api.bestiary.request.BeastDetailRequest;
import club.dnd5.portal.exception.PageNotFoundException;
import club.dnd5.portal.model.creature.Creature;
import club.dnd5.portal.repository.ImageRepository;
import club.dnd5.portal.repository.TokenRepository;
import club.dnd5.portal.repository.datatable.BestiaryRepository;
import club.dnd5.portal.service.BestiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Tag(name = "Бестиарий v2", description = "API для сущест из бестиария")
@RequestMapping("/api/v2/")
@RestController
public class Bestiary2ApiController {
    private final BestiaryService bestiaryService;
    private final BestiaryRepository beastRepository;
    private final ImageRepository imageRepository;
    private final TokenRepository tokenRepository;

    @Operation(summary = "Получение краткого списка сушеств")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/bestiary")
    public List<BeastApi> getBestiary(@ParameterObject BeastRequesApi request) {
        return bestiaryService.findAll(request);
    }

    @Operation(summary = "Получение сушества по английскому имени")
    @GetMapping(value = "/bestiary/{englishName}")
    public BeastDetailApi getBeast(@PathVariable String englishName) {
        return bestiaryService.findOne(englishName);
    }

    @Operation(summary = "Добавление существа в бестиарий")
    @SecurityRequirement(name = "Bearer Authentication")
    @Secured({"ADMIN"})
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/v2/bestiary")
    public void createBeast(@RequestBody BeastDetailRequest request) {
        bestiaryService.create(request);

    }

    @Operation(summary = "Обнавление существа из бестиарии")
    @SecurityRequirement(name = "Bearer Authentication")
    @Secured({"ADMIN"})
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/api/v2/bestiary")
    public void updateBeast(@RequestBody BeastDetailRequest request) {
        bestiaryService.update(request);

    }
}
