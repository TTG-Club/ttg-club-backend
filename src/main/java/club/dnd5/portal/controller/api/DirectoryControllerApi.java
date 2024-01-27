package club.dnd5.portal.controller.api;

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
import java.util.Collection;
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
    public Collection<NameApi> getDices() {
        return Arrays.stream(Dice.values())
                        .map(type -> new NameApi(type.getName(), type.name()))
                        .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Типы существ")
    @GetMapping("/beast/types")
    public Collection<NameApi> getCreatureType() {
        return Arrays.stream(CreatureType.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Размеры существ")
    @GetMapping("/size")
    public Collection<NameApi> getCreatureSize() {
        return Arrays.stream(CreatureSize.values())
                .map(size -> new NameApi(size.getCyrillicName(), size.name()))
                .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Типы урона")
    @GetMapping("/damage/types")
    public Collection<NameApi> getDamageType() {
        return Arrays.stream(DamageType.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Состояния")
    @GetMapping("/conditions")
    public Collection<NameApi> getConditions() {
        return Arrays.stream(Condition.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Мировоззрение")
    @GetMapping("/alignments")
    public Collection<NameApi> getAlignments() {
        return Arrays.stream(Alignment.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Места обитания")
    @GetMapping("/environments")
    public Collection<NameApi> getEnvironments() {
        return Arrays.stream(HabitatType.values())
                        .map(type -> new NameApi(type.getName(), type.name()))
                        .collect(Collectors.toList()
        );
    }

    @Operation(summary = "Типы доспехов для существ")
    @GetMapping("/armor/types")
    public Collection<NameApi> getArmorTypes() {
        return Arrays.stream(ArmorType.values())
                        .map(type -> new NameApi(type.getCyrillicName(), type.name()))
                        .collect(Collectors.toList()
        );
    }
}
