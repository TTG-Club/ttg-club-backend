package club.dnd5.portal.dto.api.image;

/**
 * Круглый токен существа: первая картинка в карточке существа.
 * @param url адрес токена или {@code null}, если токена нет
 */
public record ImageTokenApi(String url) {
}
