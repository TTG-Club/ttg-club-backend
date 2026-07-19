package club.dnd5.portal.repository.tracker;

import club.dnd5.portal.model.tracker.InitiativeParticipant;
import club.dnd5.portal.model.tracker.ParticipantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InitiativeParticipantRepository extends JpaRepository<InitiativeParticipant, UUID> {

	List<InitiativeParticipant> findAllByTrackerId(UUID trackerId);

	Optional<InitiativeParticipant> findByIdAndTrackerId(UUID id, UUID trackerId);

	long countByTrackerIdAndType(UUID trackerId, ParticipantType type);

	@Query("SELECT COALESCE(MAX(p.seq), 0) FROM InitiativeParticipant p WHERE p.trackerId = :trackerId")
	int findMaxSeq(@Param("trackerId") UUID trackerId);

	/**
	 * Физически удаляет всех участников трекера — при мягком удалении трекера с владельцем
	 * и при физическом удалении анонимного трекера (FK-каскада на уровне БД тут нет).
	 */
	@Modifying
	@Query("DELETE FROM InitiativeParticipant p WHERE p.trackerId = :trackerId")
	void deleteAllByTrackerId(@Param("trackerId") UUID trackerId);

	/**
	 * Чистит участников анонимных трекеров без активности перед их удалением по TTL —
	 * иначе без FK-каскада участники осиротеют.
	 */
	@Modifying
	@Query("DELETE FROM InitiativeParticipant p WHERE p.trackerId IN "
			+ "(SELECT t.id FROM InitiativeTracker t WHERE t.ownerUsername IS NULL AND t.updatedAt < :cutoff)")
	void deleteParticipantsOfStaleAnonymous(@Param("cutoff") Instant cutoff);
}
