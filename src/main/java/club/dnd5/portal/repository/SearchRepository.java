package club.dnd5.portal.repository;

import club.dnd5.portal.dto.api.SearchApi;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SearchRepository {
	private String SQL_SEARCH;

	@PersistenceContext
	EntityManager entityManager;

	@PostConstruct
	public void init(){
		SQL_SEARCH =
				"SELECT name, 'Заклинания' as section, CONCAT('/spells/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM spells WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name" +
				" UNION ALL " +
				"SELECT name, 'Классы' as section, CONCAT('/classes/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM classes WHERE name LIKE :name OR english_name LIKE :name" +
				" UNION ALL " +
				"SELECT a.name, 'Архетипы классов' as section, CONCAT('/classes/', REPLACE(LOWER(c.english_name), ' ', '_'), '/', REPLACE(LOWER(a.english_name), ' ', '_')) url, a.description FROM archetypes a JOIN classes c ON c.id = a.class_id WHERE a.name LIKE :name OR a.english_name LIKE :name" +
				" UNION ALL " +
				"SELECT r.name, 'Расы и происхождения ' as section, CASE WHEN p.name IS NOT NULL THEN CONCAT('/races/', REPLACE(LOWER(p.english_name), ' ', '_'), '/',REPLACE(LOWER(r.english_name), ' ', '_')) ELSE CONCAT('/races/', REPLACE(LOWER(r.english_name), ' ', '_1')) END AS url, r.description FROM races r LEFT JOIN races p ON r.parent_id = p.id WHERE r.name LIKE :name OR r.alt_name LIKE :name OR r.english_name LIKE :name" +
				"  UNION ALL " +
				"SELECT name, 'Бестиарий' as section, CONCAT('/bestiary/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM creatures WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name" +
				"  UNION ALL " +
				"SELECT name, 'Магические предметы' as section, CONCAT('/items/magic/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM artifactes WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name" +
				"  UNION ALL " +
				"SELECT name, 'Снаряжение' as section, CONCAT('/items/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM equipments WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name" +
				"  UNION ALL " +
				"SELECT name, 'Оружие' as section, CONCAT('/weapons/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM weapons WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name" +
				"  UNION ALL " +
				"SELECT name, 'Доспехи' as section, CONCAT('/armors/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM armors WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name" +
				" UNION ALL " +
				"SELECT name, 'Ширма Мастера' as section, CONCAT('/screens/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM screens WHERE name LIKE :name OR english_name LIKE :name OR alt_name LIKE :name" +
				" UNION ALL " +
				"SELECT name, 'Правила и термины' as section, CONCAT('/rules/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM rules WHERE name LIKE :name OR english_name LIKE :name" +
				" UNION ALL " +
				"SELECT name, 'Черты' as section, CONCAT('/traits/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM traits WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name" +
				" UNION ALL " +
				"SELECT name, 'Особенности классов' as section, CONCAT('/options/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM options WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name" +
				" UNION ALL " +
				"SELECT name, 'Предыстории и происхождения' as section, CONCAT('/backgrounds/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM backgrounds WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name"+
				" UNION ALL " +
				"SELECT name, 'Боги' as section, CONCAT('/gods/', REPLACE(LOWER(english_name), ' ', '_')) url, description FROM gods WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name";
	}

	public long getCount(String searchText) {
		Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM(" + SQL_SEARCH + ") countResult");
		query.setParameter("name", "%" + searchText.trim() + "%");
		return ((BigInteger) query.getSingleResult()).longValue();
	}

	public List<SearchApi> search(String searchText, Integer page, Integer limit) {
		Query query = entityManager.createNativeQuery(SQL_SEARCH);
		query.setParameter("name", "%" + searchText.trim() + "%");

		if (limit != null) {
			query.setMaxResults(limit);
		} else {
			limit = 10;
		}
		if (page != null) {
			query.setFirstResult(page * limit);
		}
		List<Object[]> result = query.getResultList();
		return result.stream().map(row -> new SearchApi(row[0], row[1], row[2], shortDescription(row[3]))).collect(Collectors.toList());
	}

	private String shortDescription(Object description) {
		if (description == null) {
			return "...";
		}
		String text = Jsoup.clean(description.toString().replace("&nbsp;", " "), Safelist.none());
		if (text.length() > 200){
			text = String.format("%s...",text.substring(0, 200).trim());
		}
		return text;
	}
}
