package club.dnd5.portal.repository.tavern;

import club.dnd5.portal.model.tavern.RaceHabitatChance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceHabitatChanceRepository extends JpaRepository<RaceHabitatChance, Integer> {
}
