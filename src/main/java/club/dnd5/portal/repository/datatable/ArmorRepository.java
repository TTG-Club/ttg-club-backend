package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.items.Armor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArmorRepository extends JpaRepository<Armor, Integer>, JpaSpecificationExecutor<Armor> {
	Optional<Armor> findByEnglishName(String name);
}
