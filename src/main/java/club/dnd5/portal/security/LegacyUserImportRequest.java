package club.dnd5.portal.security;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class LegacyUserImportRequest {
    private String username;
    private String email;
    private String passwordHash;
    private boolean enabled;
    private boolean emailVerified;
    private Instant createdAt;
    private List<String> roles;
}
