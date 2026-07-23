package club.dnd5.portal.controller.api.user;

import club.dnd5.portal.dto.user.DisplayNameDto;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.security.ExternalAuthClient;
import club.dnd5.portal.security.ExternalAuthUser;
import club.dnd5.portal.service.CommentsRenameClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Профиль пользователя: отображаемое имя для комментариев. Хранится локально в {@code users}
 * (колонка {@code display_name}); {@code null} — показывается логин.
 * <p>
 * Смена имени и публикация комментария синхронизируют снимок имени в сервисе комментариев:
 * тот стамперит логин из токена, а {@link CommentsRenameClient} приводит его к отображаемому
 * имени в комментариях этого сайта (скоуп по платформе).
 */
@Tag(name = "Профиль пользователя", description = "Отображаемое имя для комментариев")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
@Validated
public class UserProfileController {
    private static final String USER_TOKEN_COOKIE = "dnd5_user_token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final ExternalAuthClient externalAuthClient;
    private final CommentsRenameClient commentsRenameClient;

    @GetMapping("/display-name")
    public ResponseEntity<DisplayNameDto> getDisplayName() {
        return currentUser()
                .map(user -> ResponseEntity.ok(new DisplayNameDto(user.getDisplayName())))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PatchMapping("/display-name")
    public ResponseEntity<DisplayNameDto> updateDisplayName(
            @Valid @RequestBody DisplayNameDto request) {
        Optional<User> current = currentUser();
        if (!current.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String displayName = request.getDisplayName().trim();

        User user = current.get();
        user.setDisplayName(displayName);
        userRepository.save(user);

        // Сразу приводим имя в уже оставленных комментариях к новому (best-effort).
        syncCommentsName(displayName);

        return ResponseEntity.ok(new DisplayNameDto(displayName));
    }

    /**
     * Приводит имя автора в комментариях к текущему отображаемому имени. Зовётся фронтом
     * после публикации комментария: сервис стамперит логин, этот вызов заменяет его на имя.
     * Без заданного имени синхронизировать нечего.
     */
    @PostMapping("/comments/sync-name")
    public ResponseEntity<Map<String, Boolean>> syncCommentsName() {
        Optional<User> current = currentUser();
        if (!current.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String displayName = current.get().getDisplayName();
        if (!StringUtils.hasText(displayName)) {
            return ResponseEntity.ok(Collections.singletonMap("synced", false));
        }

        boolean synced = syncCommentsName(displayName);
        return ResponseEntity.ok(Collections.singletonMap("synced", synced));
    }

    /**
     * Дёргает рену comments-service с UUID автора из токена. UUID (клейм {@code sub}) в
     * SecurityContext не лежит — там только логин, — поэтому берём его из auth-service по
     * токену запроса. Best-effort: без токена или при ошибке — false, имя подхватится позже.
     */
    private boolean syncCommentsName(String displayName) {
        String token = extractToken(currentRequest());
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            ExternalAuthUser authUser = externalAuthClient.me(token);
            return commentsRenameClient.rename(authUser.getId(), displayName);
        } catch (RuntimeException error) {
            return false;
        }
    }

    /** Текущий пользователь из локальной таблицы по имени принципала (логин/email). */
    private Optional<User> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return userRepository.findByEmailOrUsername(authentication.getName(), authentication.getName());
    }

    private static HttpServletRequest currentRequest() {
        return ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder
                        .currentRequestAttributes()).getRequest();
    }

    /** Токен из заголовка Authorization, иначе из куки — как в AuthServiceAuthenticationFilter. */
    private static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (USER_TOKEN_COOKIE.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
