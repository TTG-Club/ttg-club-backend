package club.dnd5.portal.config;

import club.dnd5.portal.config.properties.TrackerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TrackerProperties.class)
public class TrackerConfig {
}
