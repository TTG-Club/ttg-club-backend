package club.dnd5.portal.repository.dmscreen;

import club.dnd5.portal.model.dmscreen.DmScreen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DmScreenRepository extends JpaRepository<DmScreen, UUID> {

	List<DmScreen> findAllByUserIdOrderByUpdatedAtDesc(Long userId);

	long countByUserId(Long userId);

	Optional<DmScreen> findByIdAndUserId(UUID id, Long userId);
}
