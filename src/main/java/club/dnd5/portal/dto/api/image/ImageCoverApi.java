package club.dnd5.portal.dto.api.image;

/**
 * Обложка класса или расы.
 * @param url адрес обложки, с которого её показывают карточки
 * @param editable можно ли загрузить обложку этой записи (у подрас своей обложки нет)
 */
public record ImageCoverApi(String url, boolean editable) {
}
