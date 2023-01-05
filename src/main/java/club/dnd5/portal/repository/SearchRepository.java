package club.dnd5.portal.repository;

import club.dnd5.portal.dto.api.SearchApi;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class SearchRepository {
	@PersistenceContext
	EntityManager entityManager;

	public List search(String searchText) {
		Query query = entityManager.createNativeQuery(
			"SELECT name as name, 'Заклинания' as section, CONCAT('/spells/', REPLACE(LOWER(english_name), ' ', '_')) url FROM spells WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name " +
			"UNION ALL " +
			"SELECT name as name, 'Бестиарий>' as section, CONCAT('/bestiary/', REPLACE(LOWER(english_name), ' ', '_')) url FROM creatures WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name " +
			"UNION ALL " +
			"SELECT name as name, 'Черты' as section, CONCAT('/traits/', REPLACE(LOWER(english_name), ' ', '_')) url FROM traits WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name " +
			"UNION ALL " +
			"SELECT name as name, 'Особенности классов' as section, CONCAT('/options/', REPLACE(LOWER(english_name), ' ', '_')) url FROM options WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name " +
			"UNION ALL " +
			"SELECT name as name, 'Предыстории и происхождения' as section, CONCAT('/backgrounds/', REPLACE(LOWER(english_name), ' ', '_')) url FROM backgrounds WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name ");
		query.setParameter("name", "%" + searchText.trim() + "%");
		return query.getResultList();
	}
}
