package club.dnd5.portal.repository.datatable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.trait.Trait;

@Repository
public interface TraitDatatableRepository extends DataTablesRepository<Trait, Integer> {
	Optional<Trait> findByEnglishName(String name);

	@Query("SELECT t.requirement FROM Trait t GROUP BY t.requirement")
	Collection<String> findAllPrerequisite();

	@Query("SELECT c.book FROM Trait c GROUP BY c.book HAVING c.book.type = :type ORDER BY c.book.year")
	Collection<Book> findBook(@Param("type") TypeBook type);
}
