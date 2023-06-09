package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.Condition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConditionDatatableRepository extends JpaRepository<Condition, Integer> {
	Optional<Condition> findByEnglishName(@Param("englishName") String englishName);
}
