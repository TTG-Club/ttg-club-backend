package club.dnd5.portal.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.image.Image;
import club.dnd5.portal.model.image.ImageType;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long>{
	/** Адреса картинок галереи в порядке добавления: первая — главная. */
	@Query("SELECT i.url FROM Image i WHERE i.type=:type AND i.refId=:refId ORDER BY i.id")
	Collection<String> findAllByTypeAndRefId(@Param("type") ImageType type, @Param("refId") Integer refId);

	Collection<Image> findAllByType(ImageType type);

	@Modifying
	@Query("DELETE FROM Image i WHERE i.type=:type AND i.refId=:refId")
	void deleteAllByTypeAndRefId(@Param("type") ImageType type, @Param("refId") Integer refId);
}
