package club.dnd5.portal.repository;

import club.dnd5.portal.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Short> {
}
