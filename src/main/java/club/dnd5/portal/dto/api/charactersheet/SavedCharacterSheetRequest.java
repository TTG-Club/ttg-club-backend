package club.dnd5.portal.dto.api.charactersheet;

import jakarta.validation.constraints.NotBlank;

/**
 * Сохранение чужого листа по ссылке.
 * @param shareToken секрет ссылки
 */
public record SavedCharacterSheetRequest(@NotBlank String shareToken) {
}
