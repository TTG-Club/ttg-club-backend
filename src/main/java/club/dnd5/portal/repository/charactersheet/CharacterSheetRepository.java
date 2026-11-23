package club.dnd5.portal.repository.charactersheet;

import club.dnd5.portal.model.charactersheet.CharacterSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CharacterSheetRepository extends JpaRepository<CharacterSheet, UUID> {

	List<CharacterSheet> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

	long countByUserId(Long userId);

	Optional<CharacterSheet> findByIdAndUserId(UUID id, Long userId);
}
