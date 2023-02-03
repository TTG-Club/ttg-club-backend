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
	@PersistenceContext
	EntityManager entityManager;

	public long getCount(String searchText) {
		Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM full_text_search WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name");
		query.setParameter("name", "%" + searchText.trim() + "%");
		return ((BigInteger) query.getSingleResult()).longValue();
	}

	public List<SearchApi> search(String searchText, Integer page, Integer limit) {
		Query query = entityManager.createNativeQuery("SELECT name, section, url, description FROM full_text_search WHERE name LIKE :name OR alt_name LIKE :name OR english_name LIKE :name");
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
