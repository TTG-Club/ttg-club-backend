package club.dnd5.portal.repository.charactersheet;

import club.dnd5.portal.model.charactersheet.CharacterSheetPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CharacterSheetPresenceRepository extends JpaRepository<CharacterSheetPresence, UUID> {

	/**
	 * Ставит или освежает отметку одним запросом. Не «найти и сохранить»: две вкладки одного
	 * пользователя шлют отметки одновременно, и вторая вставка упала бы на уникальном индексе.
	 * Идентификаторы строками: колонки char(36).
	 */
	@Modifying
	@Query(value = "insert into character_sheet_presence (id, sheet_id, user_id, seen_at) "
			+ "values (:id, :sheetId, :userId, :seenAt) "
			+ "on conflict (sheet_id, user_id) do update set seen_at = excluded.seen_at",
			nativeQuery = true)
	void upsert(@Param("id") String id,
				@Param("sheetId") String sheetId,
				@Param("userId") Long userId,
				@Param("seenAt") Instant seenAt);

	List<CharacterSheetPresence> findAllBySheetIdAndUserIdNotAndSeenAtAfter(UUID sheetId, Long userId,
																			 Instant seenAfter);

	@Modifying
	@Query("delete from CharacterSheetPresence p where p.sheetId = :sheetId and p.seenAt < :seenBefore")
	int deleteStale(@Param("sheetId") UUID sheetId, @Param("seenBefore") Instant seenBefore);

	@Modifying
	@Query("delete from CharacterSheetPresence p where p.sheetId = :sheetId and p.userId = :userId")
	int deleteBySheetIdAndUserId(@Param("sheetId") UUID sheetId, @Param("userId") Long userId);

	@Modifying
	@Query("delete from CharacterSheetPresence p where p.sheetId in :sheetIds")
	int deleteAllBySheetIdIn(@Param("sheetIds") Collection<UUID> sheetIds);
}
