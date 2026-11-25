package club.dnd5.portal.config;

import org.springframework.boot.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Настройки коннектора Tomcat.
 *
 * <p>Tomcat 11.0.27 отклоняет запрос, если в заголовке Accept-Language больше 10 языков
 * ({@code maxLocaleCount}): {@code request.getLocale()} в {@code RequestContextFilter} бросает
 * IllegalArgumentException, и любой запрос такого клиента получает 500. Браузеры с длинным списком
 * языков и боты присылают больше — лимит поднят. Язык ответа от заголовка не зависит.
 */
@Configuration
public class TomcatConfig {
	/** Сколько языков из Accept-Language разбирает Tomcat (по умолчанию 10). */
	private static final int MAX_LOCALE_COUNT = 100;

	@Bean
	public TomcatConnectorCustomizer maxLocaleCountCustomizer() {
		return connector -> connector.setMaxLocaleCount(MAX_LOCALE_COUNT);
	}
}
