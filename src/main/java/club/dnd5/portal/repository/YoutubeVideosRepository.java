package club.dnd5.portal.repository;

import club.dnd5.portal.model.YoutubeVideo;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YoutubeVideosRepository extends CrudRepository<YoutubeVideo, String>, JpaSpecificationExecutor<YoutubeVideo> {
	long countByActive(boolean active);
}
