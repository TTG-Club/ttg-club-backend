package club.dnd5.portal.repository;

import club.dnd5.portal.dto.api.SearchApi;
import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.model.book.Book;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Repository
public class SearchRepository {
	@PersistenceContext
	EntityManager entityManager;

	public long getCount(String searchText) {
		Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM full_text_search " +
			"WHERE LOWER(name) LIKE :name OR LOWER(alt_name) LIKE :name OR LOWER(english_name) LIKE :name");
		query.setParameter("name", "%" + searchText.trim().toLowerCase(Locale.ROOT) + "%");
		return ((BigInteger) query.getSingleResult()).longValue();
	}

	public List<SearchApi> search(String searchText, Integer page, Integer limit) {
		Query query = entityManager.createNativeQuery(
			"SELECT fts.name, fts.english_name, fts.section, fts.url, fts.description, b.source, b.type, b.name book_name FROM full_text_search fts " +
					"JOIN books b ON fts.source = b.source " +
					"WHERE LOWER(fts.name) LIKE :name OR LOWER(fts.alt_name) LIKE :name OR LOWER(fts.english_name) LIKE :name");
		query.setParameter("name", "%" + searchText.trim().toLowerCase(Locale.ROOT) + "%");

		if (limit != null) {
			query.setMaxResults(limit);
		} else {
			limit = 10;
		}
		if (page != null) {
			query.setFirstResult(page * limit);
		}
		List<Object[]> result = query.getResultList();
		return result.stream().map(row -> new SearchApi(row[0], row[1], row[2], row[3], shortDescription(row[4]),
			new SourceApi(row[5].toString(), row[6].toString(), row[7].toString()))).collect(Collectors.toList());
	}

	public SearchApi findByIndex(int index) {
		Query query = entityManager.createNativeQuery("SELECT fts.name, fts.english_name, fts.section, fts.url, fts.description, b.source, b.type, b.name book_name " +
			"FROM full_text_search fts " +
			"JOIN books b ON fts.source = b.source");
		query.setFirstResult(index);
		query.setMaxResults(1);
		Object[] row = (Object[]) query.getSingleResult();
		return new SearchApi(row[0], row[1], row[2], row[3], shortDescription(row[4]), new SourceApi(row[5].toString(), row[6].toString(), row[7].toString()));
	}

	private String shortDescription(Object description) {
		if (description == null) {
			return null;
		}
		String text = Jsoup.clean(
			description.toString()
				.replace("&nbsp;", " ")
				.replaceAll("</(.+?)><(\\w)", "</$1> <$2"),
			Safelist.none()
		);
		if (text.length() > 200){
			text = String.format("%s...", text.substring(0, 200).trim())
				.replaceAll("\\s+", " ")
				.replaceAll("\\.{4,}", "...");
		}
		return text;
	}
}
