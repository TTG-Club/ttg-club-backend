package club.dnd5.portal.repository.tavern;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.races.Sex;
import club.dnd5.portal.model.tavern.OwnerWeakness;

@Repository
public interface OwnerWeaknessRepository extends JpaRepository<OwnerWeakness, Integer> {
	List<OwnerWeakness> findBySexOrSexIsNull(Sex sex);
}
