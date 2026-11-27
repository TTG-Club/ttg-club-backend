package club.dnd5.portal.dto.api.charactersheet;

import java.util.List;

/**
 * Права на редактирование сохранённых листов текущего пользователя.
 * @param sheets сохранённые листы, по которым запрашивалось редактирование
 */
public record CharacterSheetEditAccessListResponse(List<CharacterSheetEditAccessResponse> sheets) {
}
