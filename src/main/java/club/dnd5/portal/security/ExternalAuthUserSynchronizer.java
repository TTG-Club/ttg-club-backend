package club.dnd5.portal.security;

import club.dnd5.portal.model.user.Role;
import club.dnd5.portal.model.user.User;
import club.dnd5.portal.repository.user.RoleRepository;
import club.dnd5.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ExternalAuthUserSynchronizer {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void sync(ExternalAuthUser externalUser) {
        if (externalUser == null || !StringUtils.hasText(externalUser.getEmail())) {
            return;
        }

        User user = userRepository.findByEmailOrUsername(externalUser.getEmail(), externalUser.getUsername())
                .orElseGet(User::new);
        user.setEmail(externalUser.getEmail());
        user.setUsername(externalUser.getUsername());
        user.setName(externalUser.getUsername());
        user.setEnabled(externalUser.isEnabled());
        if (user.getPassword() == null) {
            user.setPassword("");
        }

        List<Role> roles = resolveRoles(externalUser.getRoles());
        if (!roles.isEmpty()) {
            user.setRoles(roles);
        }

        userRepository.save(user);
    }

    /**
     * Роли приходят из auth-service — он источник правды. Строки справочника
     * {@code roles} заводятся по мере появления ролей: на пустой базе (новый
     * стенд, восстановление) иначе ни одна роль не находилась, и даже
     * администратор оставался без прав.
     */
    private List<Role> resolveRoles(List<String> roleNames) {
        List<Role> roles = new ArrayList<>();
        if (roleNames != null) {
            for (String roleName : roleNames) {
                String name = stripRolePrefix(roleName);
                if (StringUtils.hasText(name)) {
                    roles.add(findOrCreateRole(name.trim()));
                }
            }
        }
        if (roles.isEmpty()) {
            roles.add(findOrCreateRole("USER"));
        }
        return roles;
    }

    private Role findOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private String stripRolePrefix(String roleName) {
        if (roleName != null && roleName.startsWith("ROLE_")) {
            return roleName.substring("ROLE_".length());
        }
        return roleName;
    }
}
