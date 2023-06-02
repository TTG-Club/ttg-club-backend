package club.dnd5.portal.repository.datatable;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Weapon;

@Repository
public interface WeaponRepository extends DataTablesRepository<Weapon, Integer>, JpaSpecificationExecutor<Weapon> {
	List<Weapon> findAll();
	Optional<Weapon> findByEnglishName(String name);

	@Query("SELECT c.book FROM Weapon c GROUP BY c.book HAVING c.book.type = :type ORDER BY c.book.year")
	List<Book> findBook(@Param("type") TypeBook type);
}
