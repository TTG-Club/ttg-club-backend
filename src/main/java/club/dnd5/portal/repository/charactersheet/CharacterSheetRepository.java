package club.dnd5.portal.repository.charactersheet;

import club.dnd5.portal.model.charactersheet.CharacterSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CharacterSheetRepository extends JpaRepository<CharacterSheet, UUID> {

	List<CharacterSheet> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

	long countByUserId(Long userId);

	Optional<CharacterSheet> findByIdAndUserId(UUID id, Long userId);

	Optional<CharacterSheet> findByShareToken(UUID shareToken);

	List<CharacterSheet> findAllByIdIn(List<UUID> ids);

	/**
	 * Меняет секрет ссылки, не трогая версию листа: включение доступа не должно ломать
	 * автосохранение открытого листа (иначе оно получило бы 409).
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update CharacterSheet s set s.shareToken = :token where s.id = :id")
	int updateShareToken(@Param("id") UUID id, @Param("token") UUID token);
}
