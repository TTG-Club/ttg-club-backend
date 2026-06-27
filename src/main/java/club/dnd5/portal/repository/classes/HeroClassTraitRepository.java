package club.dnd5.portal.repository.classes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.classes.HeroClassTrait;

import java.util.Collection;
import java.util.List;

@Repository
public interface HeroClassTraitRepository extends JpaRepository<HeroClassTrait, Integer>{
	List<HeroClassTrait> findAllByHeroClassIdAndArchitypeFalse(Integer heroClassId);

	@Modifying
	@Query("DELETE FROM HeroClassTrait t WHERE t.heroClass.id = :heroClassId AND t.architype = false AND t.id NOT IN :ids")
	void deleteClassTraitsNotIn(@Param("heroClassId") Integer heroClassId, @Param("ids") Collection<Integer> ids);

	@Modifying
	@Query("DELETE FROM HeroClassTrait t WHERE t.heroClass.id = :heroClassId AND t.architype = false")
	void deleteClassTraits(@Param("heroClassId") Integer heroClassId);

}
