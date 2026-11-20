package club.dnd5.portal.dto.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.dto.api.classes.NameApi;
import club.dnd5.portal.dto.api.item.TypeApi;
import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class BookApi {
	private NameApi name;
	private String url;
	private TypeApi type;
	/** Блоки разметки core-api. */
	private List<String> description;
	private Integer year;
	private SourceApi source;
	/** Обложка источника, если она загружена. */
	private String image;
	public BookApi(Book book) {
		name = new NameApi(book.getName(), book.getEnglishName());
		url = String.format("/books/%s", book.getUrlName());
		type = new TypeApi(book.getType().getName(), book.getType().ordinal());
		year = book.getYear();
		source = new SourceApi(book);
		image = book.getCover();
	}
}