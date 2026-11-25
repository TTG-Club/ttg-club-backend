package club.dnd5.portal.dto.api.charactersheet;

import java.util.List;

/**
 * «Другие листы» пользователя.
 * @param items сохранённые листы, последние сохранённые первее
 * @param used сколько записей занято (недоступные тоже занимают место)
 * @param limit лимит пользователя
 * @param subscriberLimit лимит с подпиской
 */
public record SavedCharacterSheetListResponse(List<SavedCharacterSheetResponse> items, int used, int limit,
											  int subscriberLimit) {
}
