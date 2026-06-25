package club.dnd5.portal.repository;

import club.dnd5.portal.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Integer> {
	List<Language> findByNameIn(Collection<String> names);
}
