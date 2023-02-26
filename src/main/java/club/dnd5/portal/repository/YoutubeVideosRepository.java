package club.dnd5.portal.repository;

import club.dnd5.portal.model.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YoutubeVideosRepository extends JpaRepository<YoutubeVideo, String> {
	@Query(value = "SELECT * FROM youtube_videos ORDER BY created DESC LIMIT 1", nativeQuery = true)
	Optional<YoutubeVideo> findLastAdded();
}
