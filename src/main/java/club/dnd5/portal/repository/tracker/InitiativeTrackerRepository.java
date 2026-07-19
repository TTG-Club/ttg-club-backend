package club.dnd5.portal.repository.tracker;

import club.dnd5.portal.model.tracker.InitiativeTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InitiativeTrackerRepository extends JpaRepository<InitiativeTracker, UUID> {

	long countByOwnerUsernameAndDeletedFalse(String ownerUsername);

	List<InitiativeTracker> findAllByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

	List<InitiativeTracker> findAllByOwnerUsernameAndDeletedFalseOrderByCreatedAtDesc(String ownerUsername);

	/** Удалённые трекеры пользователя, свежие первее — для ограничения размера истории. */
	List<InitiativeTracker> findAllByOwnerUsernameAndDeletedTrueOrderByUpdatedAtDesc(String ownerUsername);

	/**
	 * Отметка активности трекера для TTL-очистки анонимных: операции только с участниками
	 * (добавление/правка/удаление) не меняют строку трекера, поэтому updated_at обновляется явно.
	 */
	@Modifying
	@Query("UPDATE InitiativeTracker t SET t.updatedAt = :now WHERE t.id = :id")
	void touch(@Param("id") UUID id, @Param("now") Instant now);

	/**
	 * Удаляет анонимные трекеры без активности (владельца нет, клиент мог потерять ключ).
	 *
	 * @return количество удалённых трекеров
	 */
	@Modifying
	@Query("DELETE FROM InitiativeTracker t WHERE t.ownerUsername IS NULL AND t.updatedAt < :cutoff")
	int deleteStaleAnonymous(@Param("cutoff") Instant cutoff);
}
