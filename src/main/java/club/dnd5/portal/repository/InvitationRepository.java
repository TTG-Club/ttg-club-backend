package club.dnd5.portal.repository;

import club.dnd5.portal.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
	boolean existsByCode(String code);
}
