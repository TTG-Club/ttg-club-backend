package club.dnd5.portal.controller.api;

import club.dnd5.portal.dto.api.DirectoryApi;
import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.model.*;
import club.dnd5.portal.model.creature.Condition;
import club.dnd5.portal.model.creature.HabitatType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Различные справочники
 */
@RequiredArgsConstructor
@Tag(name = "Справочники", description = "API для различных справочников")
@RequestMapping("/api/v2/reference/")
@RestController
public class DirectoryControllerApi {
    @Operation(summary = "Дайсы")
    @GetMapping("/dices")
    public DirectoryApi getDices() {
        return new DirectoryApi(
                Arrays.stream(Dice.values())
                        .map(type -> new NameApi(type.getName(), type.name()))
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Типы существ")
    @GetMapping("/beast/types")
    public DirectoryApi getCreatureType() {
        return new DirectoryApi(
                Arrays.stream(CreatureType.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Размеры существ")
    @GetMapping("/size")
    public DirectoryApi getCreatureSize() {
        return new DirectoryApi(
                Arrays.stream(CreatureSize.values())
                .map(size -> new NameApi(size.getCyrillicName(), size.name()))
                .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Типы урона")
    @GetMapping("/damage/types")
    public DirectoryApi getDamageType() {
        return new DirectoryApi(
                Arrays.stream(DamageType.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Состояния")
    @GetMapping("/conditions")
    public DirectoryApi getConditions() {
        return new DirectoryApi(
                Arrays.stream(Condition.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Мировоззрение")
    @GetMapping("/alignments")
    public DirectoryApi getAlignments() {
        return new DirectoryApi(
                Arrays.stream(Alignment.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Места обитания")
    @GetMapping("/environments")
    public DirectoryApi getEnvironments() {
        return new DirectoryApi(
                Arrays.stream(HabitatType.values())
                        .map(type -> new NameApi(type.getName(), type.name()))
                        .collect(Collectors.toList())
        );
    }

    @Operation(summary = "Типы доспехов для существ")
    @GetMapping("/armor-types")
    public DirectoryApi getArmorTypes() {
        return new DirectoryApi(
                Arrays.stream(ArmorType.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList())
        );
    }
}
