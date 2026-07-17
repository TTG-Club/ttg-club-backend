package club.dnd5.portal.repository;

import club.dnd5.portal.model.audit.RevisionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RevisionHistoryRepository extends JpaRepository<RevisionHistory, Long> {

	List<RevisionHistory> findByEntityTypeAndEntityIdOrderByRevisionDesc(String entityType, Integer entityId);

	Optional<RevisionHistory> findByEntityTypeAndEntityIdAndRevision(String entityType, Integer entityId, Integer revision);

	@Query("SELECT COALESCE(MAX(r.revision), 0) FROM RevisionHistory r WHERE r.entityType = :entityType AND r.entityId = :entityId")
	int findMaxRevision(@Param("entityType") String entityType, @Param("entityId") Integer entityId);
}
