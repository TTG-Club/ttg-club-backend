package club.dnd5.portal.repository.datatable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {
	Optional<Book> findByEnglishName(String name);

	Optional<Book> findBySource(String source);

	Optional<Book> findFirstByType(TypeBook type);

	List<Book> findAllByTypeOrderByYearAscSourceAsc(TypeBook type);
}
