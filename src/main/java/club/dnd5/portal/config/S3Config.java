package club.dnd5.portal.config;

import club.dnd5.portal.config.properties.ImageProperties;
import club.dnd5.portal.config.properties.S3Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Клиент S3-совместимого хранилища. Без адреса хранилища (локальная
 * разработка) клиента нет, а загрузка картинок отвечает ошибкой.
 */
@Configuration
@EnableConfigurationProperties({S3Properties.class, ImageProperties.class})
public class S3Config
{
    @Bean
    @ConditionalOnExpression("!'${app.s3.endpoint:}'.isBlank()")
    public S3Client s3Client(S3Properties properties)
    {
        return S3Client.builder()
            .endpointOverride(URI.create(properties.getEndpoint()))
            .region(Region.of(properties.getRegion().isBlank() ? "us-east-1" : properties.getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
            .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
            .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
            .forcePathStyle(true)
            .build();
    }
}
