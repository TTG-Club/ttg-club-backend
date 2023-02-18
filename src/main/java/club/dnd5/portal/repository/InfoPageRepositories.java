package club.dnd5.portal.repository;

import club.dnd5.portal.model.InfoPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoPageRepositories extends JpaRepository<InfoPage, String> {
}
