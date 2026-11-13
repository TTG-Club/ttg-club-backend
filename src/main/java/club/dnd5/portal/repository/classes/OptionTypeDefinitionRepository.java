package club.dnd5.portal.repository.classes;

import club.dnd5.portal.model.classes.OptionTypeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OptionTypeDefinitionRepository extends JpaRepository<OptionTypeDefinition, String> {
	@Query("SELECT t FROM OptionTypeDefinition t ORDER BY COALESCE(t.ordering, 2147483647), t.code")
	List<OptionTypeDefinition> findAllOrdered();
}
