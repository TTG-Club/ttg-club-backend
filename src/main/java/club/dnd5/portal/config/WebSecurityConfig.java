package club.dnd5.portal.config;

import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.security.AuthServiceAuthenticationFilter;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUserSynchronizer;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final ExternalAuthClient externalAuthClient;
	private final ExternalAuthUserSynchronizer userSynchronizer;
	private final RedirectToLowerCaseInterceptor redirectToLowerCaseInterceptor;

	@Value("${allowed-origin-patterns}")
	private String[] originPatterns;

    @Bean
    public AuthServiceAuthenticationFilter authServiceAuthenticationFilter(){
        return new AuthServiceAuthenticationFilter(externalAuthClient, userSynchronizer);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic().disable()
		.cors()
		.and()
        	.csrf().disable()
        	.exceptionHandling()
        	.authenticationEntryPoint(authenticationEntryPoint)
        .and()
        	.sessionManagement()
        	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        	.authorizeRequests()
        	.antMatchers(HttpMethod.POST, "/api/v1/**").permitAll()
        	.antMatchers(HttpMethod.PATCH, "/api/v1/**").permitAll()
        	.antMatchers("/api/v1/auth/**").permitAll()
        	.antMatchers("/swagger-ui/**").permitAll()
        	.antMatchers("/swagger-resources/**").permitAll()
        	.antMatchers("/swagger-ui.html").permitAll()
        	.antMatchers(HttpMethod.GET, "/**").permitAll()
        	.antMatchers(HttpMethod.HEAD, "/**").permitAll()
        	.antMatchers(HttpMethod.PUT, "/**").permitAll()
        .anyRequest()
        .authenticated();
		http.headers().frameOptions().disable();
        http.addFilterBefore(authServiceAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
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
	public final void configure(final WebSecurity web) throws Exception {
		super.configure(web);
//		web.ignoring().antMatchers("/resources/**");
		web.httpFirewall(new AnnotatingHttpFirewall());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(redirectToLowerCaseInterceptor);
	}
}
