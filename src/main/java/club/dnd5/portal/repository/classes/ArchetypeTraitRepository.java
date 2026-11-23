package club.dnd5.portal.repository.classes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.classes.archetype.ArchetypeTrait;

import java.util.List;

@Repository
public interface ArchetypeTraitRepository extends JpaRepository<ArchetypeTrait, Integer>{
	List<ArchetypeTrait> findAllByArchetypeId(Integer archetypeId);

	/** Умение архетипа, в котором правятся варианты типа (db/changelog 0048). */
	java.util.Optional<ArchetypeTrait> findFirstByOptionsType(String optionsType);
}
