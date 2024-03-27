package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.creature.Creature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BestiaryRepository extends JpaRepository<Creature, Integer>, JpaSpecificationExecutor<Creature> {
	List<Creature> findByEnglishName(String name);

	@Query("SELECT c.book FROM Creature c GROUP BY c.book HAVING c.book.type = :type ORDER BY c.book.year")
	List<Book> findBook(@Param("type") TypeBook type);

	@Query("SELECT c FROM Creature c WHERE c.englishName = :englishName AND c.book.source = :source ORDER BY c.book.year")
	Optional<Creature> findByEnglishNameAndSource(
		@Param("englishName") String englishName,
		@Param("source") String source);
}
