package club.dnd5.portal.dto.api.charactersheet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Хиты сохранённого чужого листа, отмеченные мастером в трекере инициативы.
 * @param current текущие хиты; больше максимума листа — запишется максимум
 * @param temporary временные хиты; 0 — снять, null — не менять
 */
public record SavedCharacterSheetHealthRequest(@NotNull @PositiveOrZero Integer current,
											   @PositiveOrZero Integer temporary) {
}
