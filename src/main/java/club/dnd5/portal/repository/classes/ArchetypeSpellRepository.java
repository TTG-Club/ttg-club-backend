package club.dnd5.portal.repository.classes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import club.dnd5.portal.model.classes.archetype.Archetype;
import club.dnd5.portal.model.classes.archetype.ArchetypeSpell;

public interface ArchetypeSpellRepository extends JpaRepository<ArchetypeSpell, Integer>{
	// Порядок как в MySQL: по строке archetype_spell (выборка шла по индексу spell_id).
	@Query("SELECT a FROM Archetype a JOIN a.spells s WHERE s.spell.id = :spellId GROUP BY a ORDER BY MIN(s.id)")
	List<Archetype> findAllBySpell(@Param("spellId") int spellId);
}