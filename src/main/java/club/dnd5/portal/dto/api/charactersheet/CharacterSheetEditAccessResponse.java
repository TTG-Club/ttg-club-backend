package club.dnd5.portal.dto.api.charactersheet;

import club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus;

/**
 * Право на редактирование сохранённого чужого листа — лёгкая сводка без документа: по ней клиент
 * сразу узнаёт, что владелец ответил на запрос.
 * @param savedId идентификатор сохранённой записи
 * @param sheetId идентификатор листа
 * @param name название листа из сохранённой записи
 * @param status PENDING — ждёт ответа владельца, APPROVED — можно редактировать
 */
public record CharacterSheetEditAccessResponse(String savedId, String sheetId, String name,
											   CharacterSheetEditorStatus status) {
}
