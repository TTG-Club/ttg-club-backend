package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.trait.Trait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface FeatRepository extends JpaRepository<Trait, Integer>, JpaSpecificationExecutor<Trait> {
	Optional<Trait> findByEnglishName(String name);

	@Query("SELECT c.book FROM Trait c GROUP BY c.book HAVING c.book.type = :type ORDER BY c.book.year")
	Collection<Book> findBook(@Param("type") TypeBook type);
}
