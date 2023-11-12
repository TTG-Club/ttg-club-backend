package club.dnd5.portal.repository;

import club.dnd5.portal.model.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TokenRepository extends
	JpaRepository<Token, Long>,
	JpaSpecificationExecutor<Token> {
	Collection<Token> findByRefIdAndType(Integer refId, String type);
}
