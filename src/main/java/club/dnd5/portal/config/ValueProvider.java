package club.dnd5.portal.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ValueProvider {
	@Value("${ttg.url}")
	private String ttgUrl;
	@Value("${ttg.dev.url}")
	private String devUrl;
}
