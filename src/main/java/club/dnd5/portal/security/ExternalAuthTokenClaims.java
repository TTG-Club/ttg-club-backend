package club.dnd5.portal.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class ExternalAuthTokenClaims {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String subject;
    private final String username;
    private final String email;
    private final List<String> roles;


    public static ExternalAuthTokenClaims fromToken(String token) {
        if (!StringUtils.hasText(token)) {
            return empty();
        }

        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return empty();
        }

        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode claims = OBJECT_MAPPER.readTree(payload);
            return new ExternalAuthTokenClaims(
                    text(claims, "sub"),
                    text(claims, "username"),
                    text(claims, "email"),
                    roles(claims));
        } catch (IllegalArgumentException | IOException exception) {
            return empty();
        }
    }

    private static ExternalAuthTokenClaims empty() {
        return new ExternalAuthTokenClaims(null, null, null, Collections.emptyList());
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static List<String> roles(JsonNode node) {
        JsonNode rolesNode = node.get("roles");
        if (rolesNode == null || !rolesNode.isArray()) {
            return Collections.emptyList();
        }

        List<String> roles = new ArrayList<>();
        rolesNode.forEach(role -> roles.add(role.asText()));
        return roles;
    }
}
