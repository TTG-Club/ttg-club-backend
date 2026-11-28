package club.dnd5.portal.dto.api.charactersheet;

import club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus;

/**
 * Запрос на редактирование или выданное право — для владельца листа. Логин и почта не отдаются:
 * владельцу хватает отображаемого имени, которое и так публично в комментариях.
 * @param id идентификатор записи: им владелец разрешает, отклоняет или отзывает право
 * @param displayName отображаемое имя; без имени — «Пользователь без имени»
 * @param status PENDING — ждёт ответа, APPROVED — может редактировать
 * @param requestedAt когда отправлен запрос (ISO-8601)
 */
public record CharacterSheetEditorResponse(String id, String displayName, CharacterSheetEditorStatus status,
										   String requestedAt) {
}
