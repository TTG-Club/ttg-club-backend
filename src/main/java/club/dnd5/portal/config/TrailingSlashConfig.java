package club.dnd5.portal.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.UrlHandlerFilter;

/**
 * Spring 6+ больше не сопоставляет «/path/» с «/path». Фильтр отрезает слеш в конце до проверки
 * прав и маршрутизации, чтобы старые ссылки и клиенты работали как раньше.
 */
@Configuration
public class TrailingSlashConfig {
	@Bean
	public FilterRegistrationBean<UrlHandlerFilter> trailingSlashFilter() {
		FilterRegistrationBean<UrlHandlerFilter> registration = new FilterRegistrationBean<>(
			UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build());
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
