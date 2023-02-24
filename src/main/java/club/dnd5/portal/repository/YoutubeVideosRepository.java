package club.dnd5.portal.repository;

import club.dnd5.portal.model.YoutubeVideo;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface YoutubeVideosRepository extends JpaRepository<YoutubeVideo, String> {
	@Query(value = "SELECT * FROM youtube_videos ORDER BY created DESC LIMIT 1", nativeQuery = true)
	YoutubeVideo findLastAdded();

	@Transactional
	@Modifying
	@Query(value = "INSERT INTO youtube_videos (id, user_id, `active`, `order`, created) VALUES (#{#video.id}, #{#video.user.id}, #{#video.active}, #{#video.order}, #{#video.created})", nativeQuery = true)
	void saveNew(@Param("video") @NonNull YoutubeVideo video);
}
