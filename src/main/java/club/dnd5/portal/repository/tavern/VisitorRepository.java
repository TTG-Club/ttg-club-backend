package club.dnd5.portal.repository.tavern;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.tavern.Visitor;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Integer> {
}
