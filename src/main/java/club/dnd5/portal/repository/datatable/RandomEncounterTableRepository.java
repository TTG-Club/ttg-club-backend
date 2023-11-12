package club.dnd5.portal.repository.datatable;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import club.dnd5.portal.model.creature.HabitatType;
import club.dnd5.portal.model.encounters.RandomEncounterTable;

public interface RandomEncounterTableRepository extends JpaRepository<RandomEncounterTable, Integer> {
	Optional<RandomEncounterTable> findByLevelAndType(int level, HabitatType type);
}
