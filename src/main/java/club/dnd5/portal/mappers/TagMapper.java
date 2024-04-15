package club.dnd5.portal.mappers;

import club.dnd5.portal.dto.api.bestiary.TagApi;
import club.dnd5.portal.model.creature.CreatureRace;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TagMapper {
	TagMapper INSTANCE = Mappers.getMapper(TagMapper.class);

	@Named("mapTags")
	default List<CreatureRace> mapTags(Collection<TagApi> tags) {
		return tags.stream()
			.map(this::mapTagToCreatureRace)
			.collect(Collectors.toList());
	}

	default CreatureRace mapTagToCreatureRace(TagApi tag) {
		CreatureRace creatureRace = new CreatureRace();
		creatureRace.setName(tag.getName());
		creatureRace.setDescription(tag.getDescription());
		return creatureRace;
	}

}
