package club.dnd5.portal.repository.tavern;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.tavern.Atmosphere;

@Repository
public interface AtmosphereRepository extends JpaRepository<Atmosphere, Integer> {
}
