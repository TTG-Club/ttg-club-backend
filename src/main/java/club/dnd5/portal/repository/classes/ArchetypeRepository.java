package club.dnd5.portal.repository.classes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.archetype.Archetype;

public interface ArchetypeRepository extends JpaRepository<Archetype, Integer>{
	Optional<Archetype> findByHeroClassIdAndEnglishNameIgnoreCase(Integer classId, String englishName);
	@Query("SELECT c.book FROM Archetype c GROUP BY c.book HAVING c.book.type = :type ORDER BY c.book.year")
	List<Book> findBook(@Param("type") TypeBook type);
}
