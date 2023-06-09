package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.background.Background;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, Integer>, JpaSpecificationExecutor<Background> {
	Optional<Background> findByEnglishName(String name);

	@Query("SELECT c.book FROM Background c GROUP BY c.book HAVING c.book.type = :type ORDER BY c.book.year")
	List<Book> findBook(@Param("type") TypeBook type);
}
