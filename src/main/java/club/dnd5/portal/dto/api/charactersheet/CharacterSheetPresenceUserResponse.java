package club.dnd5.portal.dto.api.charactersheet;

/**
 * Пользователь, у которого лист сейчас открыт.
 * @param displayName отображаемое имя; без имени — «Пользователь без имени»
 */
public record CharacterSheetPresenceUserResponse(String displayName) {
}
