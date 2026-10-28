package club.dnd5.portal.config;

import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.security.AuthServiceAuthenticationFilter;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import club.dnd5.portal.security.TokenAuthenticationCache;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import jakarta.servlet.DispatcherType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
@SecurityScheme(
	  name = "Bearer Authentication",
	  type = SecuritySchemeType.HTTP,
	  bearerFormat = "JWT",
	  scheme = "bearer"
)
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig implements WebMvcConfigurer {
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final ExternalAuthClient externalAuthClient;
	private final ExternalAuthUserSynchronizer userSynchronizer;
	private final RedirectToLowerCaseInterceptor redirectToLowerCaseInterceptor;

	@Value("${allowed-origin-patterns}")
	private String[] originPatterns;

	// Как долго провалидированный токен считается свежим и не перепроверяется в SSO.
	@Value("${auth-service.cache.fresh-ttl-ms:60000}")
	private long tokenCacheFreshTtlMs;

	// До какого возраста провалидированный токен можно использовать как fallback при недоступности SSO.
	@Value("${auth-service.cache.stale-ttl-ms:600000}")
	private long tokenCacheStaleTtlMs;

	@Value("${auth-service.cache.max-entries:20000}")
	private int tokenCacheMaxEntries;

    @Bean
    public TokenAuthenticationCache tokenAuthenticationCache() {
        return new TokenAuthenticationCache(tokenCacheFreshTtlMs, tokenCacheStaleTtlMs, tokenCacheMaxEntries);
    }

    @Bean
    public AuthServiceAuthenticationFilter authServiceAuthenticationFilter(){
        return new AuthServiceAuthenticationFilter(externalAuthClient, userSynchronizer, tokenAuthenticationCache());
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.httpBasic(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.csrf(csrf -> csrf
				.ignoringRequestMatchers("/api/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger-ui.html"))
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(authenticationEntryPoint))
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(requests -> requests
				// Внутренние перенаправления на страницу ошибки не проверяем повторно:
				// иначе вместо исходной ошибки клиент получит 401.
				.dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
				// Мастерская и запись в бестиарий — только модераторам и администраторам.
				// Правило на уровне URL срабатывает до разбора и валидации тела запроса:
				// иначе @PreAuthorize проверялся уже после @Valid, и аноним вместо 401
				// получал 400 с разбором своих данных. Должно стоять выше permitAll для POST/PATCH/PUT.
				.requestMatchers("/api/v1/workshop/**").hasAnyRole("MODERATOR", "ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/v2/bestiary").hasAnyRole("MODERATOR", "ADMIN")
				.requestMatchers(HttpMethod.PUT, "/api/v2/bestiary").hasAnyRole("MODERATOR", "ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/online/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/**").permitAll()
				.requestMatchers(HttpMethod.PATCH, "/api/v1/**").permitAll()
				.requestMatchers("/api/v1/auth/**").permitAll()
				// Прокси комментариев (CommentsProxyController): контроль доступа целиком
				// на стороне сервиса comments.ttg.club (он проверяет тот же SSO-JWT).
				// Явное правило нужно ради DELETE — остальные методы уже открыты выше.
				.requestMatchers("/api/v1/comments/**").permitAll()
				// Трекер инициативы: анонимный доступ разрешён (создание анонимного трекера и работа
				// по ключу X-Tracker-Key); контроль доступа — в сервисе (владелец по JWT либо ключ).
				.requestMatchers("/api/v2/tools/initiative/**").permitAll()
				.requestMatchers("/swagger-ui/**").permitAll()
				.requestMatchers("/swagger-resources/**").permitAll()
				.requestMatchers("/swagger-ui.html").permitAll()
				.requestMatchers(HttpMethod.GET, "/**").permitAll()
				.requestMatchers(HttpMethod.HEAD, "/**").permitAll()
				.requestMatchers(HttpMethod.PUT, "/**").permitAll()
				.anyRequest().authenticated())
			.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.addFilterBefore(authServiceAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public WebSecurityCustomizer httpFirewallCustomizer() {
		return web -> web.httpFirewall(new AnnotatingHttpFirewall());
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		 registry
			 .addMapping("/**")
			 .allowedOriginPatterns(originPatterns)
			 .allowedMethods("*")
			 .allowCredentials(true);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(redirectToLowerCaseInterceptor);
	}
}
