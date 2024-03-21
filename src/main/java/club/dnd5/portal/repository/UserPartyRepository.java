package club.dnd5.portal.repository;

import club.dnd5.portal.model.user.UserParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface UserPartyRepository extends JpaRepository<UserParty, Long>, JpaSpecificationExecutor<UserParty> {
}
