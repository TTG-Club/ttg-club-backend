package club.dnd5.portal.repository;

import club.dnd5.portal.model.user.UserParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPartyRepository extends JpaRepository<UserParty, Long> {
	Optional<UserParty> findByGroupName(String name);
}
