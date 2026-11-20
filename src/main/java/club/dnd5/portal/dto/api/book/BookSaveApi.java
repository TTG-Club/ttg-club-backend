package club.dnd5.portal.dto.api.book;

import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;
import club.dnd5.portal.util.markup.MarkupBlocksDeserializer;
import club.dnd5.portal.model.book.Book;
import club.dnd5.portal.model.book.TypeBook;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Правка источника в мастерской. Аббревиатура источника — ключ книги, на неё ссылаются
 * все записи, поэтому она не меняется.
 */
@NoArgsConstructor
@Getter
@Setter
public class BookSaveApi {
	@NotBlank
	private String name;
	@NotBlank
	private String englishName;
	private String altName;
	@NotNull
	private TypeBook type;
	private Integer year;
	/** Блоки разметки core-api; HTML-строку переводит десериализатор. */
	@JsonDeserialize(using = MarkupBlocksDeserializer.class)
	private List<@NotBlank String> description;

	public BookSaveApi(Book book) {
		name = book.getName();
		englishName = book.getEnglishName();
		altName = book.getAltName();
		type = book.getType();
		year = book.getYear();
		description = book.getDescription();
	}
}
