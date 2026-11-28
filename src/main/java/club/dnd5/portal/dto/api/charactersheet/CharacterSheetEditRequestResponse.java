package club.dnd5.portal.dto.api.charactersheet;

import club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus;

/**
 * Итог запроса на редактирование: новый запрос ждёт ответа, а уже выданное право так и остаётся.
 * @param status PENDING — запрос ждёт ответа владельца, APPROVED — право уже выдано
 */
public record CharacterSheetEditRequestResponse(CharacterSheetEditorStatus status) {
}
