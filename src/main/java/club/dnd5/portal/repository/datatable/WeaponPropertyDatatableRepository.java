package club.dnd5.portal.repository.datatable;

import club.dnd5.portal.model.items.WeaponProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeaponPropertyDatatableRepository extends JpaRepository<WeaponProperty, Integer> {
	List<WeaponProperty> findAll();

	WeaponProperty findByEnglishName(String englishName);
}
