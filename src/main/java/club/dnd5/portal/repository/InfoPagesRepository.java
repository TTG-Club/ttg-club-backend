package club.dnd5.portal.repository;

import club.dnd5.portal.model.InfoPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfoPagesRepository extends JpaRepository<InfoPage, String> {
	InfoPage findByUrl(String url);
	Optional<InfoPage> findOneByUrl(String url);
}
