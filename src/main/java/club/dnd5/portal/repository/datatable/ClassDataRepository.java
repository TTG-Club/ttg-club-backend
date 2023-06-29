package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.classes.HeroClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassDataRepository extends JpaRepository<HeroClass, String> {

}
