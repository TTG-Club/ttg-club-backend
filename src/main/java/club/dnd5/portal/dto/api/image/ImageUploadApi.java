package club.dnd5.portal.dto.api.image;

/**
 * Адрес загруженной картинки.
 * @param url адрес картинки на сайте, {@code /s3/5e14/<путь>}
 */
public record ImageUploadApi(String url) {
}
