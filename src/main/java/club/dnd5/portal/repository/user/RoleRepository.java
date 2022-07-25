package club.dnd5.portal.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.user.Role;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	public Optional<Role> findByName(String name);
}
