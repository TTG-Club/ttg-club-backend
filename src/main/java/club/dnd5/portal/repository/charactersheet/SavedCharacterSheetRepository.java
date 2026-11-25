package club.dnd5.portal.repository.charactersheet;

import club.dnd5.portal.model.charactersheet.SavedCharacterSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedCharacterSheetRepository extends JpaRepository<SavedCharacterSheet, UUID> {

	List<SavedCharacterSheet> findAllByUserIdOrderByCreatedAtDesc(Long userId);

	long countByUserId(Long userId);

	Optional<SavedCharacterSheet> findByUserIdAndSheetId(Long userId, UUID sheetId);

	Optional<SavedCharacterSheet> findByIdAndUserId(UUID id, Long userId);
}
