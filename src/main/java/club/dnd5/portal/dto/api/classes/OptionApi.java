package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.dto.api.SourceApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.classes.Option;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class OptionApi {
	protected NameApi name;
	protected String url;
	private Boolean homebrew;
	@Schema(description = "источник")
	protected SourceApi source;
	public OptionApi(Option option) {
		name = new NameApi(option.getName(), option.getEnglishName());
		url = String.format("/options/%s", option.getUrlName());
		if (option.getBook().getType() == TypeBook.CUSTOM) {
			homebrew = Boolean.TRUE;
		}
		source = new SourceApi(option.getBook());
	}
}