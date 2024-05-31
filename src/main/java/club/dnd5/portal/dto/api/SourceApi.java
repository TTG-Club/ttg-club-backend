package club.dnd5.portal.dto.api;

import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class SourceApi {
	@NotNull
	private String shortName;
	@NotNull
	private String name;
	private NameValueApi group;
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
		group = getGroupType(TypeBook.valueOf(type));
	}

	public SourceApi(Book book) {
		name = book.getName();
		shortName = book.getSource();
		if (book.getType() == TypeBook.CUSTOM) {
			homebrew = Boolean.TRUE;
		}
		group = getGroupType(book.getType());
	}

	public SourceApi(Book book, Short page) {
		this(book);
		if (page != null) {
			this.page = page;
		}
	}
	private NameValueApi getGroupType(TypeBook bookType) {
		switch (bookType) {
			case THIRD_PARTY:
				return NameValueApi.builder().shortName("3rd").name("Контент от третьих лиц").build();
			case TEST:
				return NameValueApi.builder().shortName("UA").name("Тестовый материал").build();
			case CUSTOM:
				return NameValueApi.builder().shortName("HB").name("Хомбрю ").build();
			default:
				return NameValueApi.builder().shortName("Basic").name("Официальные источники").build();
		}
	}
}
