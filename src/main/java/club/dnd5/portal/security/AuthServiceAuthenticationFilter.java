package club.dnd5.portal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class AuthServiceAuthenticationFilter extends OncePerRequestFilter {
    private final ExternalAuthClient externalAuthClient;
    private final ExternalAuthUserSynchronizer userSynchronizer;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token) && !isAuthenticated()) {
            try {
                ExternalAuthUser user = externalAuthClient.me(token);
                userSynchronizer.sync(user);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        principalName(user), null, getAuthorities(user.getRoles()));
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (RuntimeException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        if (request.getCookies() != null) {
            for (javax.servlet.http.Cookie cookie : request.getCookies()) {
                if ("dnd5_user_token".equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                && !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
    }

    private String principalName(ExternalAuthUser user) {
        return StringUtils.hasText(user.getEmail()) ? user.getEmail() : user.getUsername();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return AuthorityUtils.createAuthorityList("ROLE_USER");
        }

        return AuthorityUtils.createAuthorityList(roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role).toArray(String[]::new));
    }
}
