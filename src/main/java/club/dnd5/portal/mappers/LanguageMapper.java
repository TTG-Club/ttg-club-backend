package club.dnd5.portal.mappers;

import club.dnd5.portal.model.Language;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface LanguageMapper {
	LanguageMapper INSTANCE = Mappers.getMapper(LanguageMapper.class);

	default List<Language> mapLanguages(Collection<String> languages) {
		return languages.stream()
			.map(this::createLanguageFromName)
			.collect(Collectors.toList());
	}

	default Language createLanguageFromName(String languageName) {
		Language language = new Language();
		language.setName(languageName);
		return language;
	}
}

