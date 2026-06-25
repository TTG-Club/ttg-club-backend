package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.races.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RaceFeatureRepository extends JpaRepository<Feature, Integer> {
	@Query(value = "SELECT * FROM race_features WHERE race_id = :raceId", nativeQuery = true)
	List<Feature> findAllByRaceId(@Param("raceId") Integer raceId);

	@Modifying
	@Query(value = "DELETE FROM race_features WHERE race_id = :raceId", nativeQuery = true)
	void deleteByRaceId(@Param("raceId") Integer raceId);

	@Modifying
	@Query(value = "DELETE FROM race_features WHERE race_id = :raceId AND id NOT IN (:ids)", nativeQuery = true)
	void deleteByRaceIdAndIdNotIn(@Param("raceId") Integer raceId, @Param("ids") Collection<Integer> ids);
}
