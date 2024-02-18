package club.dnd5.portal.repository;

import club.dnd5.portal.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
	boolean existsByCode(String code);
	boolean existsByLink(String link);
	Optional<Invitation> findByLink(String link);
	Optional<Invitation> findByUserPartyId(Long groupId);
}
