package club.dnd5.portal.repository.tavern;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.model.tavern.OwnerTrait;

@Repository
public interface OwnerTraitRepository extends JpaRepository<OwnerTrait, Integer> {
	List<OwnerTrait> findBySexOrSexIsNull(Sex sex);
}
