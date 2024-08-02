package club.dnd5.portal.repository.classes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import club.dnd5.portal.model.classes.ClassFeature;

@Repository
public interface HeroClassTraitRepository extends JpaRepository<ClassFeature, Integer>{

}
