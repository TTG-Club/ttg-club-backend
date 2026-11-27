package club.dnd5.portal.dto.api.charactersheet;

/**
 * Число неотвеченных запросов на редактирование листов пользователя — для точки у меню.
 * @param count неотвеченные запросы на активные листы текущего пользователя
 */
public record CharacterSheetEditRequestCountResponse(long count) {
}
