package club.dnd5.portal.dto.api.classes;

import club.dnd5.portal.dto.api.SourceApi;
import club.dnd5.portal.model.classes.archetype.Archetype;
import lombok.Getter;

@Getter
public class NamedListApi {
	private final NameApi name;
	private final SourceTypeApi type;
	private final SourceApi source;
	private final String url;

	public NamedListApi(Archetype archetype) {
		name = new NameApi(archetype.getName(), archetype.getEnglishName());
		type = new SourceTypeApi(archetype.getBook().getType().getName(), archetype.getBook().getType().ordinal());
		source = new SourceApi(archetype.getBook());
		url = String.format("/classes/%s/%s", archetype.getHeroClass().getUrl(), archetype.getUrl());
	}
}