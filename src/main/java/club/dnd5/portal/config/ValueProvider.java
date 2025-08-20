package club.dnd5.portal.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ValueProvider {
	@Value("${app.url}")
	private String appUrl;
}
