package club.dnd5.portal.security;

import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.user.UserRepository;
import club.dnd5.portal.util.PageAndSortUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth-service.user-migration.enabled", havingValue = "true")
public class AuthServiceUserMigrationRunner implements ApplicationRunner {
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl;

    @Value("${auth-service.user-migration.admin-token:}")
    private String adminToken;

    @Value("${auth-service.user-migration.batch-size:100}")
    private int batchSize;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminToken)) {
            log.warn("Auth-service user migration is enabled, but auth-service.user-migration.admin-token is empty");
            return;
        }
        int migrated = 0;
        for (int i = 0; i < 17; i++) {
            Pageable pageable = PageAndSortUtil.getPageable(i, 1000);

            List<LegacyUserImportRequest> users = userRepository.findAll(pageable).stream()
                    .map(this::toRequest)
                    .filter(this::isMigratable)
                    .collect(Collectors.toList());

            if (users.isEmpty()) {
                log.info("Auth-service user migration skipped: no migratable users found");
                return;
            }

            for (List<LegacyUserImportRequest> batch : batches(users)) {
                importBatch(batch);
                migrated += batch.size();
                log.info("Auth-service user migration progress: {}", migrated);
            }
            log.info("Auth-service user migration finished: {} users sent", migrated);
        }
    }

    private void importBatch(List<LegacyUserImportRequest> batch) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + adminToken);

        try {
            restTemplate.exchange(
                    trimTrailingSlash(authServiceBaseUrl) + "/api/admin/users/import/legacy",
                    HttpMethod.POST,
                    new HttpEntity<>(batch, headers),
                    String.class);
        } catch (HttpStatusCodeException exception) {
            log.error("Auth-service user migration batch failed: status={}, body={}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString());
            throw exception;
        }
    }

    private LegacyUserImportRequest toRequest(User user) {
        LegacyUserImportRequest request = new LegacyUserImportRequest();
        request.setUsername(firstNotBlank(user.getUsername(), user.getName()));
        request.setEmail(user.getEmail());
        request.setPasswordHash(user.getPassword());
        request.setEnabled(user.isEnabled());
        request.setEmailVerified(user.isEnabled());
        request.setRoles(roleNames(user));
        request.setCreatedAt(user.getCreateDate().toInstant(ZoneOffset.UTC));
        return request;
    }

    private boolean isMigratable(LegacyUserImportRequest request) {
        return StringUtils.hasText(request.getUsername())
                && request.getUsername().trim().length() >= 3
                && StringUtils.hasText(request.getEmail())
                && StringUtils.hasText(request.getPasswordHash());
    }

    private List<String> roleNames(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.singletonList("USER");
        }
        return user.getRoles().stream()
                .map(Role::getName)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private List<List<LegacyUserImportRequest>> batches(List<LegacyUserImportRequest> users) {
        int size = Math.max(1, batchSize);
        List<List<LegacyUserImportRequest>> result = new ArrayList<>();
        for (int start = 0; start < users.size(); start += size) {
            result.add(users.subList(start, Math.min(start + size, users.size())));
        }
        return result;
    }

    private String firstNotBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
