package club.dnd5.portal.repository;

import club.dnd5.portal.model.token.TokenBorder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenBorderRepository extends JpaRepository<TokenBorder, Long> {
	List<TokenBorder> findAllByType(String type);
}
