package club.dnd5.portal.security;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ExternalAuthUser {
    private String id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean emailVerified;
    private List<String> roles = new ArrayList<>();
}
