package club.dnd5.portal.repository;

import club.dnd5.portal.model.NewsBanner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsBannerRepository extends JpaRepository<NewsBanner, Integer> {
	Optional<NewsBanner> findByName(String name);

	Optional<NewsBanner> findByActive(boolean active);

	long countByActive(boolean active);

	Page<NewsBanner> findAll(Specification<NewsBanner> specification, Pageable pageable);
}
