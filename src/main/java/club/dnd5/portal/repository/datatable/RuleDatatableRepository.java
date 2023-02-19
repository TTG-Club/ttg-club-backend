package club.dnd5.portal.repository.datatable;

import java.util.Set;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.rule.Rule;

@Repository
public interface RuleDatatableRepository extends JpaRepository<Rule, Integer>, JpaSpecificationExecutor<Rule> {
	@Query("SELECT r.type FROM Rule r GROUP BY r.type")
	Set<String> findAllCategories();

	Rule findByEnglishName(String name);
}
