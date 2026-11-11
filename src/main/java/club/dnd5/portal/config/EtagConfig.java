package club.dnd5.portal.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * ETag по телу ответа для редко меняющихся справочных эндпоинтов. Клиент присылает
 * If-None-Match и, пока ответ не изменился, получает 304 без тела вместо всего JSON.
 * Чтобы браузер хранил ответ и переспрашивал его, контроллер ставит Cache-Control: no-cache,
 * а не no-store, который Spring Security выставляет по умолчанию.
 */
@Configuration
public class EtagConfig {
	@Bean
	public FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
		FilterRegistrationBean<ShallowEtagHeaderFilter> registration = new FilterRegistrationBean<>(
			new ShallowEtagHeaderFilter());
		registration.addUrlPatterns("/api/v1/menu");
		return registration;
	}
}
