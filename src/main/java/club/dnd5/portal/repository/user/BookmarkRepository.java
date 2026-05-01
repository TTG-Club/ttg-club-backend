package club.dnd5.portal.repository.user;

import club.dnd5.portal.model.user.Bookmark;
import club.dnd5.portal.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {
	Collection<Bookmark> findByUser(User user);

	Collection<Bookmark> findByUserAndParentIsNull(User user);

	Collection<Bookmark> findByParentUuid(UUID uuid);
}
