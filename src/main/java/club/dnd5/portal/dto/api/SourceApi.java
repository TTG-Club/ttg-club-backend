package club.dnd5.portal.dto.api;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class SourceApi {
	@NotNull
	private String shortName;
	@NotNull
	private String name;
	private Boolean homebrew;
	private Short page;

	public SourceApi(String  shortName, String name) {
		this.shortName = shortName;
		this.name = name;
		homebrew = Boolean.FALSE;
	}

	public SourceApi(String  shortName, String type, String name) {
		this.shortName = shortName;
		this.name = name;
		homebrew = TypeBook.valueOf(type) == TypeBook.CUSTOM;
	}

	public SourceApi(Book book) {
		name = book.getName();
		shortName = book.getSource();
		if (book.getType() == TypeBook.CUSTOM) {
			homebrew = Boolean.TRUE;
		}
	}

	public SourceApi(Book book, Short page) {
		this(book);
		if (page != null) {
			this.page = page;
		}
	}
}
