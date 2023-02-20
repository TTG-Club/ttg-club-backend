package club.dnd5.portal.repository.datatable;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.book.Book;

import java.util.Optional;

@Repository
public interface BookDatatableRepository extends DataTablesRepository<Book, String> {
	Optional<Book> findByEnglishName(String name);
}
