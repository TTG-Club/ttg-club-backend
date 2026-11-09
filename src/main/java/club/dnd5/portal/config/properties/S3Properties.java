package club.dnd5.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки S3-совместимого хранилища для загружаемых картинок.
 * <p>
 * Картинки сайта раздаются с {@code app.img.url} (img.ttg.club), который
 * смотрит в бакет с префиксом {@link #prefix}: ключ объекта —
 * {@code <prefix>/<путь из URL>}.
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
