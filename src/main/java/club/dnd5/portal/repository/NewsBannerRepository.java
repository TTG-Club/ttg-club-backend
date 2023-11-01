package club.dnd5.portal.repository;

import club.dnd5.portal.model.NewsBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsBannerRepository extends JpaRepository<NewsBanner, Integer> {
	Optional<NewsBanner> findByName(String name);
}
