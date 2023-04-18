package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.races.Race;
import club.dnd5.portal.model.splells.Spell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpellRepository extends JpaRepository<Spell, Integer>, JpaSpecificationExecutor<Spell> {
	Optional<Spell> findByEnglishName(String name);

	List<Spell> findByLevelAndBook_type(byte level, TypeBook type);

	@Query("SELECT DISTINCT r FROM Race r JOIN r.spells s WHERE s.id=:spellId")
	List<Race> findAllRaceBySpell(@Param("spellId") int spellId);

	@Query("SELECT s.book FROM Spell s GROUP BY s.book HAVING s.book.type = :type ORDER BY s.book.year")
	List<Book> findBook(@Param("type") TypeBook type);
}
