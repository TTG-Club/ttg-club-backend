package club.dnd5.portal.repository;

import club.dnd5.portal.model.NewsBanner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsBannerRepository extends JpaRepository<NewsBanner, Integer> {
}
