package club.dnd5.portal.config;

import club.dnd5.portal.interceptor.RedirectToLowerCaseInterceptor;
import club.dnd5.portal.security.JwtAuthenticationEntryPoint;
import club.dnd5.portal.security.JwtAuthenticationFilter;
import club.dnd5.portal.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
	private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final JwtTokenProvider tokenProvider;
	private final UserDetailsService customUserDetailsService;
	private final RedirectToLowerCaseInterceptor redirectToLowerCaseInterceptor;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(){
        return new JwtAuthenticationFilter(tokenProvider, customUserDetailsService);
    }

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic();
        http.cors()
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
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
	}

	@Bean(name = BeanIds.AUTHENTICATION_MANAGER)
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		 registry
			 .addMapping("/**")
			 .allowedOrigins(
				 "https://ttg.club/",
				 "https://dev.ttg.club/"
			 )
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
