package club.dnd5.portal.repository;

import club.dnd5.portal.model.token.TokenBorder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenBorderRepository extends JpaRepository<TokenBorder, Long> {
	@Query(value = "SELECT * FROM token_border WHERE type = :type", nativeQuery = true)
	List<TokenBorder> getTokenBordersByType(String type);
}
