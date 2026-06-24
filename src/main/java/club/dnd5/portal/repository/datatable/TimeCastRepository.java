package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.splells.TimeCast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeCastRepository extends JpaRepository<TimeCast, Integer> {
	@Modifying
	@Query(value = "delete from spells_times where spell_id = :spellId", nativeQuery = true)
	void deleteBySpellId(@Param("spellId") Integer spellId);
}
