package club.dnd5.portal.dto.api.charactersheet;

import club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus;

import java.util.Map;

/**
 * Чужой лист, сохранённый по ссылке.
 * @param id идентификатор записи
 * @param sheetId идентификатор листа
 * @param shareToken секрет ссылки, по которой лист сохранили
 * @param name имя персонажа: актуальное, а у недоступного листа — на момент сохранения
 * @param data документ листа; null, если лист недоступен
 * @param available ссылка ещё открывает лист
 * @param editStatus право на редактирование: null — не запрашивали, PENDING — ждёт ответа, APPROVED — выдано
 */
public record SavedCharacterSheetResponse(String id, String sheetId, String shareToken, String name,
										  Map<String, Object> data, boolean available,
										  CharacterSheetEditorStatus editStatus) {
}
