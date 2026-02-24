package club.dnd5.portal.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ratelimit")
public class RateLimitProperties
{
    private long capacity = 200;
    private Duration window = Duration.ofMinutes(1);
    private Duration bucketIdleTtl = Duration.ofMinutes(30);
}