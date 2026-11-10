package club.dnd5.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки S3-совместимого хранилища для загружаемых картинок.
 * <p>
 * Картинки сайта лежат под {@link #prefix} и раздаются самим сайтом по
 * {@code /s3/<prefix>/<путь>} (см. {@code S3ImageController}).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties
{
    private String endpoint = "";
    private String region = "";
    private String bucket = "";
    private String accessKey = "";
    private String secretKey = "";
    /** Папка картинок сайта 2014 внутри бакета. */
    private String prefix = "5e14";
}
