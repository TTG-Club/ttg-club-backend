package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.Option;
import club.dnd5.portal.model.classes.Option.OptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Integer>, JpaSpecificationExecutor<Option> {
	Optional<Option> findByEnglishName(String name);

	@Query("SELECT o.prerequisite FROM Option o WHERE o.prerequisite IS NOT NULL GROUP BY o.prerequisite ORDER BY o.prerequisite")
	Collection<String> findAlldPrerequisite();

	@Query("SELECT o.prerequisite FROM Option o JOIN o.optionTypes t WHERE o.prerequisite IS NOT NULL AND t =:type GROUP BY o.prerequisite ORDER BY o.prerequisite")
	Collection<String> findAlldPrerequisite(@Param("type") OptionType type);

	@Query("SELECT c.book FROM Option c GROUP BY c.book HAVING c.book.type = :type ORDER BY c.book.year")
	List<Book> findBook(@Param("type") TypeBook type);

	@Query("SELECT o.level FROM Option o JOIN o.optionTypes t WHERE o.prerequisite IS NOT NULL AND t =:type GROUP BY o.level ORDER BY o.level")
	List<String> findAllLevel(@Param("type") OptionType type);

	@Query("SELECT o.level FROM Option o GROUP BY o.level ORDER BY o.level")
	List<String> findAllLevels();
}
