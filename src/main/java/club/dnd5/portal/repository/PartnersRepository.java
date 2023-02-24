package club.dnd5.portal.repository;

import club.dnd5.portal.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnersRepository extends JpaRepository<Partner, Short> {
}
