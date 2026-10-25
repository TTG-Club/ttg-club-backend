package club.dnd5.portal.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Тело сохранения источников по умолчанию: книги, снятые в фильтре источников.
 * Пустой список возвращает фильтр к общему виду, где выбраны все книги.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExcludedSourcesDto {
    @NotNull
    private List<String> excluded;
}
