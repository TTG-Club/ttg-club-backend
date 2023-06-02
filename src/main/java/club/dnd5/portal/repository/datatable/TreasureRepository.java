package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.items.Treasure;
import club.dnd5.portal.model.items.TreasureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TreasureRepository extends JpaRepository<Treasure, Integer>, JpaSpecificationExecutor<Treasure> {
	Optional<Treasure> findByEnglishName(String name);
	List<Treasure> findAllByCostAndType(int cost, TreasureType type);
	List<Treasure> findAllByTypeIn(Set<TreasureType> types);

	@Query("SELECT c.book FROM Treasure c GROUP BY c.book HAVING c.book.type = :type ORDER BY c.book.year")
	List<Book> findBook(@Param("type") TypeBook type);
}
