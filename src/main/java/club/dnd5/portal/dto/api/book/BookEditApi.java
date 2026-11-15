package club.dnd5.portal.dto.api.book;

import club.dnd5.portal.model.book.Book;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BookEditApi extends BookSaveApi {
	/** Аббревиатура источника: ключ книги. */
	private String source;
	/** Адрес обложки или {@code null}: меняется отдельной ручкой картинок мастерской. */
	private String cover;

	public BookEditApi(Book book) {
		super(book);
		source = book.getSource();
		cover = book.getCover();
	}
}
