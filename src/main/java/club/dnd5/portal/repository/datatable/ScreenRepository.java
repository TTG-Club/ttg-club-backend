package club.dnd5.portal.repository.datatable;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.screen.Screen;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, Integer>, JpaSpecificationExecutor<Screen> {
	Optional<Screen> findByEnglishName(@Param("englishName") String englishName);

	Collection<Screen> findAllByParentIsNullOrderByOrdering();
}
