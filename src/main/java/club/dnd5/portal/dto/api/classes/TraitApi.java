package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.dto.api.SourceApi;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import club.dnd5.portal.model.book.TypeBook;
import club.dnd5.portal.model.trait.Trait;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)

@NoArgsConstructor
@Getter
@Setter
public class TraitApi {
	protected NameApi name;
	protected String url;
	private String requirements ;
	private Boolean homebrew;
	@Schema(description = "источник")
	protected SourceApi source;
	public TraitApi(Trait feat) {
		name = new NameApi(feat.getName(), feat.getEnglishName());
		url = String.format("/feats/%s", feat.getUrl());
		if (feat.getRequirement() != null) {
			requirements = feat.getRequirement();
		} else {
			requirements = "Нет";
		}
		if (feat.getBook().getType() == TypeBook.CUSTOM) {
			homebrew = Boolean.TRUE;
		}
		source = new SourceApi(feat.getBook());
	}
}
