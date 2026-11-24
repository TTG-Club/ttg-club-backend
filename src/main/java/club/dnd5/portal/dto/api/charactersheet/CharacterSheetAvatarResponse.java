package club.dnd5.portal.dto.api.charactersheet;

/**
 * Загруженный портрет персонажа.
 * @param url адрес картинки с меткой версии: файл листа перезаписывается на месте
 */
public record CharacterSheetAvatarResponse(String url) {
}
