package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.creature.CreatureRace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TagBestiaryDatatableRepository extends JpaRepository<CreatureRace, Integer> {
	Collection<CreatureRace> findByOrderByName();
}
