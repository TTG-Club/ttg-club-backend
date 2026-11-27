package club.dnd5.portal.repository.charactersheet;

import club.dnd5.portal.model.charactersheet.CharacterSheetEditor;
import club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CharacterSheetEditorRepository extends JpaRepository<CharacterSheetEditor, UUID> {

	Optional<CharacterSheetEditor> findBySheetIdAndUserId(UUID sheetId, Long userId);

	/**
	 * Запись для решения владельца. Лист в условии: идентификатор записи чужого листа не должен
	 * работать даже как подтверждение её существования.
	 */
	Optional<CharacterSheetEditor> findByIdAndSheetId(UUID id, UUID sheetId);

	boolean existsBySheetIdAndUserIdAndStatus(UUID sheetId, Long userId, CharacterSheetEditorStatus status);

	long countBySheetIdAndStatus(UUID sheetId, CharacterSheetEditorStatus status);

	/** Запросы и редакторы листа, старые первее. */
	List<CharacterSheetEditor> findAllBySheetIdOrderByCreatedAtAsc(UUID sheetId);

	List<CharacterSheetEditor> findAllByUserIdAndSheetIdIn(Long userId, Collection<UUID> sheetIds);

	/**
	 * Неотвеченные запросы по листам одним запросом — для меток в списке листов.
	 * @return пары [id листа, число запросов]; листов без запросов в ответе нет
	 */
	@Query("select e.sheetId, count(e) from CharacterSheetEditor e where e.sheetId in :sheetIds "
			+ "and e.status = club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus.PENDING "
			+ "group by e.sheetId")
	List<Object[]> countPendingBySheetIds(@Param("sheetIds") Collection<UUID> sheetIds);

	/**
	 * Неотвеченные запросы на активные листы владельца. Удалённые не считаются: в истории ответить
	 * на запрос нельзя.
	 */
	@Query("select count(e) from CharacterSheetEditor e, CharacterSheet s where s.id = e.sheetId "
			+ "and s.user.id = :ownerId and s.deletedAt is null "
			+ "and e.status = club.dnd5.portal.model.charactersheet.CharacterSheetEditorStatus.PENDING")
	long countPendingForOwner(@Param("ownerId") Long ownerId);

	@Modifying(flushAutomatically = true)
	@Query("delete from CharacterSheetEditor e where e.sheetId in :sheetIds")
	int deleteAllBySheetIdIn(@Param("sheetIds") Collection<UUID> sheetIds);

	@Modifying(flushAutomatically = true)
	@Query("delete from CharacterSheetEditor e where e.sheetId = :sheetId and e.userId = :userId")
	int deleteBySheetIdAndUserId(@Param("sheetId") UUID sheetId, @Param("userId") Long userId);
}
